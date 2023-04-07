Title: jank development update - Optimizing a ray tracer
Date: April 07, 2023
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Description: A deep dive into the internals of Clojure and jank to optimize
             a ray tracer.
Image: /img/2023-04-07-big.png

After the last post, which focused on optimizing jank's sequences, I knew I
wanted to get jank running a ray tracer I had previously written in Clojure. In
this post, I document what was required to start ray tracing in jank and, more
importantly, how I chased down the run time in a fierce battle with Clojure's
performance.

## Missing Clojure functions
Coming out of the last blog post, there were quite a few functions which the ray
tracer required that jank did not yet have. A lot of this was tedium, but there
are some interesting points.

### Polymorphic arithmetic
In Clojure JVM, since everything can be an object, and Clojure's dynamically
typed, we can't know what something like `(+ a b)` actually does. For example,
it's possible that either `a` or `b` is not a number, but it's also possible
that they're an unboxed `long` or `double`, or a boxed `Long` or a `Double`, or
maybe even a `BigInteger` or `Ratio`. Each of these will handle `+` slightly
differently. Clojure (and now jank) handles this using a neat polymorphic
design. In jank, it starts with this `number_ops` interface:

```cpp
struct number_ops
{
  virtual number_ops const& combine(number_ops const&) const = 0;
  virtual number_ops const& with(integer_ops const&) const = 0;
  virtual number_ops const& with(real_ops const&) const = 0;

  virtual object_ptr add() const = 0;
  virtual object_ptr subtract() const = 0;
  virtual object_ptr multiply() const = 0;
  virtual object_ptr divide() const = 0;
  virtual object_ptr remainder() const = 0;
  virtual object_ptr inc() const = 0;
  virtual object_ptr dec() const = 0;
  /* ... and so on ... */
};
```

jank then has different implementations of this interface, like `integer_ops`
and `real_ops`. The trick here is to use the correct "ops" for the combination of
left and right. By left and right, I mean, when looking at the expression
`(+ a b)`, we see the left side, `a`, and the right side, `b`. So if they're both
integers, we can use the `integer_ops`, which returns more integers. But if one
is an integer and the other is a real, we need need to return a real. You can
see this in Clojure, since `(+ 1 2)` is `3`, but `(+ 1 2.0)` is `3.0`.

The way this comes together is something like this:

```cpp
object_ptr add(object_ptr const l, object_ptr const r)
{ return with(left_ops(l), right_ops(r)).add(); }
```

You can see the Clojure source for this
[here](https://github.com/clojure/clojure/blob/master/src/jvm/clojure/lang/Numbers.java)
and the jank source for this
[here](https://github.com/jank-lang/jank/blob/main/src/cpp/jank/runtime/obj/number.cpp#L99).

## Running the ray tracer
This ray tracer is a partial port of the very fun
[Ray tracing in one weekend](https://raytracing.github.io/books/RayTracingInOneWeekend.html)
project. It's not meant to be fast; it's a learning tool. However, it was a pure
Clojure project I had laying around and seemed like a goal next goal for jank.
The source code for both the jank and the Clojure versions are in this
[gist](https://gist.github.com/jeaye/77e1d8874c8e76e7335ccf71ef53785c). They
vary only in the math functions used; each one uses its host interop for them.
Note that this is not the most idiomatic Clojure; it was written to work with
the limitations of a previous iteration of jank, then somewhat upgraded. jank
still doesn't support some idiomatic things like using keywords as functions, so
there are many calls to `get`.

After giving that code a tolerant scan, let's take a look at the initial numbers.

### First timing results
I'm ray tracing a tiny image. It's only 10x6 pixels. However, in order to create
this tiny image, we need to cast 265 rays. Here's the image (there's a larger
one at the end of the post):

<figure>
  <img src="/img/2023-04-07-small.png"></img>
</figure>

Now, the initial timing for this (using
[nanobench](https://github.com/martinus/nanobench)) for jank is here:

|               ms/op |                op/s |    err% |          ins/op |         bra/op |   miss% |     total | benchmark |
|--------------------:|--------------------:|--------:|----------------:|---------------:|--------:|----------:|:---------- |
|              797.49 |                1.25 |    2.1% |4,864,568,466.00 | 873,372,774.00 |    1.3% |      8.61 | `ray` |

Just shy of 800 milliseconds. It's less than a second, yes, but it's also a
trivially tiny image. Let's see how Clojure does with the same code (using
[criterium](https://github.com/hugoduncan/criterium/).

```clojure
; (out) Evaluation count : 12 in 6 samples of 2 calls.
; (out)              Execution time mean : 69.441424 ms
; (out)     Execution time std-deviation : 8.195639 ms
; (out)    Execution time lower quantile : 63.812357 ms ( 2.5%)
; (out)    Execution time upper quantile : 83.135203 ms (97.5%)
; (out)                    Overhead used : 11.626368 ns
```

**Yikes.** Clojure only takes 69 ms. jank takes 11.6x as long as Clojure to
render the same image. That's ok, though. We didn't want this to be easy.

## Profile, change, benchmark, repeat
My general understanding of the ray tracer was that we would spend most of our
time:

* Crunching numbers (using that polymorphic arithmetic I outlined above)
* Creating maps (for rays, mainly)
* Looking up data in maps (for rays, mainly)

### Keywords with identity for equal and hash
Going into this, I knew one thing I could tackle right out of the gate. Map
lookups were just using equality checks, but keywords can be compared using
identity (pointer value), since they're interned.

The changes:
[067e0ee400bffea010cf8bfad0e81a75134f7771](https://github.com/jank-lang/jank/commit/067e0ee400bffea010cf8bfad0e81a75134f7771)

|               ms/op |                op/s |    err% |          ins/op |         bra/op |   miss% |     total | benchmark |
|--------------------:|--------------------:|--------:|----------------:|---------------:|--------:|----------:|:---------- |
|              559.31 |                1.79 |    2.2% |3,317,176,052.00 | 613,115,971.00 |    1.7% |      6.10 | `ray` |

Very nice. We're coming out swinging. From 797.49 ms to 559.31 ms is a big win.

#### Compare generated code
After this, I didn't have other optimizations planned, so the next reasonable
step was to just take a look at the generated code from both jank and Clojure
and try to spot some differences. For decompiling Clojure code, I'll use the
excellent [clj-java-decompiler](https://github.com/clojure-goes-fast/clj-java-decompiler).

##### `vec3-scale`
Given the three expected time sinks I listed above, I chose a function which had
map creation, map lookups, and arithmetic. Should be the exact sort of thing we
need to optimize. Here's the function:

```clojure
(defn vec3-scale [l n]
  {:r (* (get l :r) n)
   :g (* (get l :g) n)
   :b (* (get l :b) n)})
```

Let's see the jank code for this first. I've annotated it for readability.

```cpp
/* Our function was turned into a struct which implements
   some jank interfaces. */
struct vec3_scale579 : jank::runtime::object,
                       jank::runtime::behavior::callable,
                       jank::runtime::behavior::metadatable {
  jank::runtime::context &__rt_ctx;

  /* Vars referenced within the fn are lifted to members.
     In this case, * and get. */
  jank::runtime::var_ptr const _STAR_595;
  jank::runtime::var_ptr const get596;

  /* Constants are lifted to members. */
  jank::runtime::object_ptr const const600;
  jank::runtime::object_ptr const const594;
  jank::runtime::object_ptr const const598;

  /* Constructor which initializes all lifted vars and constants. */
  vec3_scale579(jank::runtime::context &__rt_ctx)
      : __rt_ctx{__rt_ctx},
        _STAR_595{__rt_ctx.intern_var("clojure.core", "*").expect_ok()},
        get596{__rt_ctx.intern_var("clojure.core", "get").expect_ok()},
        const600{__rt_ctx.intern_keyword("", "b", true)},
        const594{__rt_ctx.intern_keyword("", "r", true)},
        const598{__rt_ctx.intern_keyword("", "g", true)}
  { }

  /* This is where the actual jank code we wrote runs. */
  jank::runtime::object_ptr call
  (
    jank::runtime::object_ptr l,
    jank::runtime::object_ptr n
  ) const override {
    /* First we call (* (get l :r) n). We can see the calls to get and * here. */
    object_ptr call607;
    {
      object_ptr call608;
      { call608 = jank::runtime::dynamic_call(get596->get_root(), l, const594); }
      call607 = jank::runtime::dynamic_call(_STAR_595->get_root(), call608, n);
    }

    /* Same thing for calling (* (get l :g) n). */
    object_ptr call609;
    {
      object_ptr call610;
      { call610 = jank::runtime::dynamic_call(get596->get_root(), l, const598); }
      call609 = jank::runtime::dynamic_call(_STAR_595->get_root(), call610, n);
    }

    /* Same thing for calling (* (get l :b) n). */
    object_ptr call611;
    {
      object_ptr call612;
      { call612 = jank::runtime::dynamic_call(get596->get_root(), l, const600); }
      call611 = jank::runtime::dynamic_call(_STAR_595->get_root(), call612, n);
    }

    /* Finally, we create a map from all of this and return it. */
    auto const map613
    (
      jank::make_box<jank::runtime::obj::map>
      (std::in_place, const594, call607, const598, call609, const600, call611)
    );
    return map613;
  }
};
```

No big surprises there. Each var is dereferenced (with `->get_root()`) when it's
used, since vars can change at any time, from any thread. Let's see what Clojure
generates for the same function.

```java
/* Just like in jank, a class was generated for this function. */
public final class core$vec3_scale extends AFunction
{
    /* Just like in jank, the constants were lifted up to be members. */
    public static final Keyword const__0;
    public static final Keyword const__3;
    public static final Keyword const__4;
    
    public static Object invokeStatic(final Object l, final Object n)
    {
      /* Wait... no var derefs. Clojure didn't generate calls to clojure.core/*
         at all. It replaced them with calls to `Numbers.multiply`. Same with
         `RT.get`.*/
      return RT.mapUniqueKeys
      (
        const__0, Numbers.multiply(RT.get(l, const__0), n),
        const__3, Numbers.multiply(RT.get(l, const__3), n),
        const__4, Numbers.multiply(RT.get(l, const__4), n)
      );
    }
    
    @Override
    public Object invoke(final Object l, final Object n)
    { return invokeStatic(l, n); }
    
    /* We initialize the constants here, instead of in the function's
       constructor. This is a small performance win, since it only needs to
       happen once. */
    static {
      const__0 = RT.keyword(null, "r");
      const__3 = RT.keyword(null, "g");
      const__4 = RT.keyword(null, "b");
    }
}
```

Ok, that's cheating! Clojure skipped past the actual implementation of
`clojure.core/*` and generated something else in its place. This not only skips
the var dereference, but it allows for calling right into a Java function which
is going to be faster than something written in Clojure. It also skipped past
the var for `clojure.core/get` and instead generated in a call to `RT.get`.
While what I said about vars changing all the time is true, Clojure makes an
exception for some functions within `clojure.core`.

Fine. I can cheat, too. The changes:

* Optimize calls to get: [7258ddd3c58debf09070a71a4a1149bd1170d440](https://github.com/jank-lang/jank/commit/7258ddd3c58debf09070a71a4a1149bd1170d440)
* Optimize math calls: [cde89658cec801ae54a4858d75e99ad9fde4bd2a](https://github.com/jank-lang/jank/commit/cde89658cec801ae54a4858d75e99ad9fde4bd2a)

But before we look at the new time, there was another thing Clojure is doing. Clojure
unboxes numbers whenever possible. Unboxing means to use automatic memory,
rather than dynamic memory for storing something; this is frequently referred to
as using "the stack". Take a look at this function here:

```clojure
(defn reflectance [cosine ref-idx]
  (let [r (/ (- 1.0 ref-idx)
             (+ 1.0 ref-idx))
        r2 (* r r)]
    (* (+ r2 (- 1.0 r2))
       (Math/pow (- 1.0 cosine) 5.0))))
```

Because `(- 1.0 ref-idx)` contains a `double`, Clojure can know that the whole
expression will return a `double`. By then tracking how `r` is used, we can see
if it requires boxing at all. In this case, `r` is only used with `*`, which
doesn't require boxing. So `r` can actually just be a `double` instead of a
`Double`. The same applies for everything else. Take a look at the generated code.

```java
public final class core$reflectance extends AFunction
{
  public static Object invokeStatic(final Object cosine, final Object ref_idx)
  {
    final double r = Numbers.minus(1.0, ref_idx) / Numbers.add(1.0, ref_idx);
    final double r2 = r * r;
    return Numbers.multiply(r2 + (1.0 - r2), Math.pow(Numbers.minus(1.0, cosine), 5.0));
  }
}
```

One last point about this: you may note that we're not boxing the `double` we're
returning. The call to `Numbers.multiply` with two `double` inputs returns a
`double`, but our function returns an `Object`. This works because Java supports
auto-boxing and unboxing. In short, it will allow you to implicitly treat boxed
and unboxed objects the same, injecting in the necessary code when it compiles.
So, don't be fooled, the final return value here is boxed.

To do a similar thing in jank, I broke it into some steps:

##### Generate more type info
Changes: [3e18c1025f3a6db2028d55f819594208197e1a78](https://github.com/jank-lang/jank/commit/3e18c1025f3a6db2028d55f819594208197e1a78)

The goal here is to use boxed types (`integer_ptr`, `keyword_ptr`, etc) during
codegen, whenever we have them, rather than just `object_ptr` everywhere. Also,
whenever possible, use `auto` to allow the propagation of richer types.

##### Add boxed typed overloads for math fns
Changes: [083f08374dd371dacf6b854decbada83d336fbd6](https://github.com/jank-lang/jank/commit/083f08374dd371dacf6b854decbada83d336fbd6)

Even without unboxing, if we can know we're adding a `real_ptr` and a
`real_ptr`, for example, we can skip the polymorphic dance and get right at
their internal data. We don't do that yet, here, but we add the right overloads.

##### Extend the codegen to convey, for any expression, whether a box is needed
Changes: [626680ecdb09d9007378e5f17bd06f8bcfd285ff](https://github.com/jank-lang/jank/commit/626680ecdb09d9007378e5f17bd06f8bcfd285ff)

This sets the stage for unboxed math, `if` conditions, `let` bindings, etc.

##### Remove polymorphism from boxed math ops, when possible
Changes: [87715f9a711e0e0bd667afce221ccbe4a85b5501](https://github.com/jank-lang/jank/commit/87715f9a711e0e0bd667afce221ccbe4a85b5501)

This utilizes the typed overloads to optimize the calls where we have a typed
box like `integer_ptr` or `real_ptr`.

##### Add unboxed math overloads
Changes: [d6d97e9a58f6a40dcc425b8da80c9bd69faa0886](https://github.com/jank-lang/jank/commit/d6d97e9a58f6a40dcc425b8da80c9bd69faa0886)

Wrapping everything together, this allows expressions like `(/ 1.0 (+ n 0.5))`
to avoid boxing, at least for the `(+ n 0.5)`. Conditions for `if` don't
require boxing, so something like `(if (< y (/ 1.0 (+ n 0.5))) ...)` wouldn't box at
all.

I didn't implement unboxed `let` bindings, since that requires tracking binding
usages to know if each one requires a box and I'm lazy. Still, let's see
what we won.

|               ms/op |                op/s |    err% |          ins/op |         bra/op |   miss% |     total | benchmark |
|--------------------:|--------------------:|--------:|----------------:|---------------:|--------:|----------:|:---------- |
|              312.78 |                3.20 |    3.4% |1,817,289,100.00 | 350,618,740.00 |    1.4% |      3.39 | `ray` |

Nice! That was a lot of work, but it gets us down from 559.31 ms to 312.78 ms.

#### Profile results
At this point, inspecting the generated code wasn't showing any clear
opportunities, so I profiled again and looked for the primary culprits. They
were:

* `get`
* `mul`, `add`, `sub`
* map constructor

Hm, strikingly similar to the expectations set when we started. Sometimes it's
unfortunate not to find surprises. Still, let's see what we can do about those
maps.

#### Faster map
jank's array map implementation was using a vector of pairs. Note that both jank
and Clojure distinguish between array maps and hash maps. Array maps are a
specialization for short maps (i.e. few keys) which don't have the overhead of
trees. Rather than `O(log32 n)` access with some constant overhead, they have `O(n)`
access with very little overhead. When `n` is small enough, it ends up being
faster. Once array maps get too big, they convert automatically into hash maps.
For this ray tracer, all of the maps are very small, so we're only looking at
the array map implementation. jank's array map looked like this:

```cpp
template <typename K, typename V>
struct map_type_impl
{
  /* Storing a vector of key/value pairs. */
  using value_type = native_vector<std::pair<K, V>>;
  using iterator = typename value_type::iterator;
  using const_iterator = typename value_type::const_iterator;

  map_type_impl() = default;
  map_type_impl(map_type_impl const &s) = default;
  map_type_impl(map_type_impl &&s) noexcept = default;
  map_type_impl(in_place_unique, value_type &&kvs)
    : data{ std::move(kvs) }
  { }
  ~map_type_impl() = default;

  /* ... insert fns ... */

  /* Note the linear search. */
  V find(K const &key) const
  {
    if(auto const kw = key->as_keyword())
    {
      for(auto const &kv : data)
      {
        if(kv.first == key)
        { return kv.second; }
      }
    }
    else
    {
      for(auto const &kv : data)
      {
        if(kv.first->equal(*key))
        { return kv.second; }
      }
    }
    return nullptr;
  }

  /* ... hashing, iterators, size, etc ... */

  value_type data;
  mutable size_t hash{};
};
```

This is using [folly's vector](https://github.com/facebook/folly/blob/main/folly/docs/FBVector.md),
a more optimized version of `std::vector`. Turns out it's still very slow to
create, compared to just an array. I ended up benchmarking both vector types,
`std::array`, and finally C arrays, each with pairs and without. C arrays,
without pairs, clearly won. Since there are no pairs, keys and values are
interleaved. Turns out this is exactly what Clojure does in [PersistentArrayMap.java](https://github.com/clojure/clojure/blob/master/src/jvm/clojure/lang/PersistentArrayMap.java#L34).

The new array map is quite similar:

```cpp
template <typename KV>
struct map_type_impl
{
  /* Just a C array and a length. */
  using value_type = KV*;

  map_type_impl() = default;
  map_type_impl(map_type_impl const &s) = default;
  map_type_impl(map_type_impl &&s) noexcept = default;
  map_type_impl(in_place_unique, value_type const kvs, size_t const length)
    : data{ kvs }, length{ length }
  { }
  ~map_type_impl() = default;

  /* ... insert fns ... */

  KV find(KV const key) const
  {
    if(auto const kw = key->as_keyword())
    {
      /* Interleaved key/values changes iteration. */
      for(size_t i{}; i < length; i += 2)
      {
        if(data[i] == key)
        { return data[i + 1]; }
      }
    }
    else
    {
      for(size_t i{}; i < length; i += 2)
      {
        if(data[i]->equal(*key))
        { return data[i + 1]; }
      }
    }
    return nullptr;
  }

  /* Custom iteration is needed, due to the interleaving. */
  struct iterator
  {
    using iterator_category = std::input_iterator_tag;
    using difference_type = std::ptrdiff_t;
    using value_type = std::pair<KV, KV>;
    using pointer = value_type*;
    using reference = value_type&;

    value_type operator *() const
    { return { data[index], data[index + 1] }; }
    iterator& operator ++()
    {
      index += 2;
      return *this;
    }
    bool operator !=(iterator const &rhs) const
    { return data != rhs.data || index != rhs.index; }
    bool operator ==(iterator const &rhs) const
    { return !(*this != rhs); }
    iterator& operator=(iterator const &rhs)
    {
      if(this == &rhs)
      { return *this; }

      data = rhs.data;
      index = rhs.index;
      return *this;
    }

    KV const* data{};
    size_t index{};
  };
  using const_iterator = iterator;

  value_type data{};
  size_t length{};
  mutable size_t hash{};
};
```

Let's take a look at the new numbers.

|               ms/op |                op/s |    err% |          ins/op |         bra/op |   miss% |     total | benchmark |
|--------------------:|--------------------:|--------:|----------------:|---------------:|--------:|----------:|:---------- |
|              199.31 |                5.02 |    4.7% |1,000,518,223.00 | 241,479,412.00 |    1.2% |      2.20 | `ray` |

From 312.78 ms to 199.31 ms. This actually optimized both map creation and map
lookup. We're still in the 3x territory, compared to Clojure, though. We need
some more big wins.

## Compiler flags
Big wins in the code were getting tough to come by, at this point, so I
started looking elsewhere. What flags are we using for the compiler? What about
the JIT compiler? We had `-O1` for both, due to some
[Cling](https://github.com/root-project/cling) issues which
prevented me from going higher. I spent some time working around those and was
able to get jank compiling with `-O3`. This alone was a huge win, but I was able
to tweak Cling's flags as well, to balance the trade off between run time and compile
time. We can't go to `-O3` for Cling, but we can do better than just `-O1`. That
can mean `-ffast-math`, `-march=native`, etc. More importantly, we can make this
configurable with a command line flag.

|               ms/op |                op/s |    err% |          ins/op |         bra/op |   miss% |     total | benchmark |
|--------------------:|--------------------:|--------:|----------------:|---------------:|--------:|----------:|:---------- |
|              124.46 |                8.03 |    0.7% |  737,637,716.35 | 155,270,134.60 |    1.5% |     30.11 | `ray` |

Very nice. This gets us within the 2x range from Clojure, so it's time to pull
out a card I've been hanging onto for a while.

## Disable incremental GC
I've been running all of this with Boehm's GC set to incremental mode, which
actually does some garbage collection on every allocation. This is sometimes
preferable for more deterministic frame rate, for example, but jank needs to
provide this as a run-time option rather than hard-coding it. The default Boehm
GC mode, which is what the JVM is doing for Clojure, collects far less
frequently, but it "stops the world" for longer in order to do so. If we switch
back to that mode:

|               ms/op |                op/s |    err% |          ins/op |         bra/op |   miss% |     total | benchmark |
|--------------------:|--------------------:|--------:|----------------:|---------------:|--------:|----------:|:---------- |
|               73.07 |               13.69 |    0.7% |  492,708,225.76 | 101,461,032.29 |    0.2% |     17.89 | `ray` |

Oh yeah. We're about 4 ms away from beating Clojure and we haven't changed a
single line of the ray tracer.

## Final stretch
I tried a lot of things here. Profiling wasn't giving me much, mostly due to
issues with Cling, which don't allow me to profile with optimizations above
`-O1`. Though I'm confident that unboxing those `let` bindings would likely do
the trick, I was still too lazy, so I looked elsewhere.

Starting at 73.07 ms, I ran through some optimizations I could see:

1. Use `final` everywhere, to encourage devirtualization ([changes](https://github.com/jank-lang/jank/commit/86e0ff8512c07720f735b40c772074158a2e9661)): 72.32 ms
2. Unbox constants wherever possible ([changes](https://github.com/jank-lang/jank/commit/b557dcb24b48d947f83f4effdac09ba3508392d0)): 70.72 ms
3. Unboxed, direct fn calls when possible ([changes](https://github.com/jank-lang/jank/commit/6f26fdda89e385c9edb43b8f8696e9aded8795e7)): no change
4. Unboxed `integer_range` instead of polymorphic `range` (didn't commit): no change
5. Remove excess `option<T>` use, showing up in profiler ([changes](https://github.com/jank-lang/jank/commit/5b34e9dd57229efe9959f332602c57f0614fddd0)): no change

Finally, a noticed through more careful profiling that `reduce*` was showing up.
Let's see how it was written.

```clojure
(defn reduce*
  ; ... Ignoring the binary version ...
  ([f val coll]
    (let [s (seq coll)]
      (if s
        (recur f (f val (first s)) (next s))
        val))))
```

Ah, this was one of the few jank functions in `clojure.core` which didn't yet
have a native implementation. Also, let's take a look at some key points here:

1. Each iteration, we call `seq` on `coll`
2. We call `next` on the sequence, since we don't have `next-in-place` exposed
   in jank; this would only be safe if we know it's a fresh sequence, though

In the last optimization post, we added the `next_in_place` function to the
`sequence` object, which allows us to iterate without another allocation. By
default, Clojure's sequences allocate a new seq for each iteration. But
this in-place update can only be done if we're 100% certain we have a fresh
sequence which nobody else is hanging onto. Right now, this can be known in
three ways:

1. Implicitly, if we're in a variadic function, since we know that the packed
   args will be a sequence created just for this function
2. Also implicitly, if we have the result of calling `next` on a sequence, which
   is required to return a fresh sequence
3. Explicitly, if we just made the sequence ourselves

I think we need another explicit way to do this and that we should remove the second
implicit way by allowing `next` to not return fresh sequences. This would allow
a `range` to pre-allocate sequences, for example. So, I added a `fresh_seq`
function to the `seqable` behavior, which must explicitly return a fresh
sequence which can then be updated in place. Both `fresh-seq` and
`next-in-place` were added to `clojure.core`, too, though they may end up in a
different namespace eventually. The new version of `reduce*` only has one
allocation (the fresh seq), regardless of the size of `coll`, and looks
like this:

```clojure
(defn reduce*
  ; ... Ignoring the binary version ...
  ([f val coll]
    (native/raw "__value = #{ val }#;
                 for
                 (
                   auto it(#{ coll }#->as_seqable()->fresh_seq());
                   it != nullptr;
                   it = it->next_in_place()
                 )
                 { __value = dynamic_call(f, __value, it->first()); }")))
```

The changes: [93626ac79919d9280985e90746fbc229554e5788](https://github.com/jank-lang/jank/commit/93626ac79919d9280985e90746fbc229554e5788)

|               ms/op |                op/s |    err% |          ins/op |         bra/op |   miss% |     total | benchmark |
|--------------------:|--------------------:|--------:|----------------:|---------------:|--------:|----------:|:---------- |
|               69.00 |               14.49 |    0.6% |  468,098,714.62 |  95,721,617.90 |    0.4% |     16.70 | `ray` |

Phew. From 70.72 ms down to 69.00 ms with this change. We have successfully beat
Clojure's 69.44 ms, albeit by less than half of a millisecond.

### Summary
jank is getting faster and faster and it's also getting more and more
functionality. Still, I need to note some things. This was all benchmarked in a
REPL. Clojure supports AOT compilation, direct linking, type hints, etc, which
would make it even faster. I will benchmark against those when jank supports
them. For this exercise, I was only focused on how Clojure and jank ran the same
code out of the box.

There's a lot more which jank could do in order to drop this time further. We
already mentioned tracking `let` bindings so we could unbox them. Each
jank-defined function, right now, is limited to accepting `object_ptr` inputs and
returning one `object_ptr` output. Also, my benchmarking has shown that trying
to mimic Clojure's Java hierarchy in C++ is just not going to pan out well, so a
different solution will be required. Early prototyping of that has been *very*
promising, but more will be shared in a future post.

I'm confident jank can consistently be faster than Clojure, and I will work to
make that happen. But I also realize that speed is only one metric; jank needs
to be easily usable, be as simple as possible, have great tooling, and embrace
the culture of stability which Clojure has. Future posts will also be focusing
on these metrics.

<figure>
  <img src="/img/2023-04-07-big.png" width="800px"></img>
</figure>
