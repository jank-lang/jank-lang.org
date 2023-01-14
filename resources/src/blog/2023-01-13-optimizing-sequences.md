Title: jank development update - Optimizing sequences
Date: Jan 13, 2023
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Description: A deep dive into Clojure's sequence API and what it takes for a
             native Clojure dialect to compete, in performance, with the JVM.

In this episode of jank's development updates, we follow an exciting few
weekends as I was digging deep into Clojure's sequence implementation,
building jank's equivalent, and then benchmarking and profiling in a dizzying
race to the bottom.

## Introduction
Not expecting a rabbit hole, I was originally surprised at how many allocations
are involved in a normal sequence iteration in Clojure and thought to optimize
that in jank. In fact, Clojure allocates a new sequence for every element over
which it iterates!

Clojure's interface for sequences looks like this
([link](https://github.com/clojure/clojure/blob/9e362e2ee4bf1feb942d31dac821e9d2917789b6/src/jvm/clojure/lang/ISeq.java)):
```java
public interface ISeq extends IPersistentCollection
{
  /* Returns the current front element of the sequence. */
  Object first();
  /* Returns a *new* sequence where the first is the next element, or nil. */
  ISeq next();
  /* Returns a *new* sequence where the first is the next element, or (). */
  ISeq more();
  /* Returns a *new* sequence where o is placed before the current first. */
  ISeq cons(Object o);
}
```

In particular, we're interested in `next`. To look at how this is implemented,
let's see an example from Clojure's `APersistentVector` sequence
([link](https://github.com/clojure/clojure/blob/56d37996b18df811c20f391c840e7fd26ed2f58d/src/jvm/clojure/lang/APersistentVector.java#L473-L477)):

```java
public ISeq next()
{
  /* This seq has a member for the vector itself, `v`, and the current offset
     into it, `i`. Each iteration makes a new sequence with `i` incremented. */
  if(i + 1 < v.count())
  { return new APersistentVector.Seq(v, i + 1); }
  return null;
}
```

This really surprised me, and I figured there must be a lot of cases where a
sequence is only referenced in one place, so it can be changed in place in order
to avoid allocations. This could **potentially save millions of allocations in a typical program**.
For example, with something like:

```clojure
(apply str [1 2 3 4 5 6 7 8 9 10])
```

The exact `APersistenVector.Seq` from above will be used here, resulting in 10
allocations as `apply` iterates through the sequence to build the arguments for
`str`. So I built something like that in jank's sequence API. It looks like
this:

```cpp
struct sequence : virtual object, seqable
{
  using sequence_ptr = detail::box_type<sequence>;

  virtual object_ptr first() const = 0;
  virtual sequence_ptr next() const = 0;
  /* Each call to next() allocates a new sequence_ptr, since it's polymorphic. When iterating
   * over a large sequence, this can mean a _lot_ of allocations. However, if you own the
   * sequence_ptr you have, typically meaning it wasn't a parameter, then you can mutate it
   * in place using this function. No allocations will happen.
   *
   * If you don't own your sequence_ptr, you can call next() on it once, to get one you
   * do own, and then next_in_place() on that to your heart's content. */
  virtual sequence_ptr next_in_place() = 0;

  /* Note, no cons here, since that's not implemented yet. */
};
```

The usage of `next_in_place` for all sequence traversals in jank meant that, **at most, one allocation was needed for an iteration of any length**.
In jank's case, that meant the same `(apply str [1 2 3 4 5 6 7 8 9 10])` went
from 32 allocations to only 3.

That's a huge win. Right?

## The rabbit hole
So then I benchmarked. How long does jank take to `apply` that same vector of
numbers to `str`? How much did I save?

### jank
Note, this benchmark fn in jank is using
[nanobench](https://nanobench.ankerl.com/). Since jank doesn't have working
macros yet, the benchmark also includes invoking the function, which is not the
case for Clojure.
```clojure
(benchmark "apply"
           (fn* []
             (apply str [1 2 3 4 5 6 7 8 9 10])))
```

Before the `next_in_place` change (`ns/op` is the primary value of interest):

|               ns/op |                op/s |    err% |          ins/op |         branch/op |   miss% |     total | benchmark |
| ------------------: | ------------------: | ------: | --------------: | -------------: | ------: | --------: | :--------- |
|            7,215.69 |          138,586.80 |    0.3% |       30,505.02 |       8,329.00 |    0.4% |      0.04 | `apply` |

After the `next_in_place` change:

|               ns/op |                op/s |    err% |          ins/op |         branch/op |   miss% |     total | benchmark |
|--------------------:|--------------------:|--------:|----------------:|---------------:|--------:|----------:|:---------- |
|            6,191.03 |          161,523.93 |    0.2% |       25,375.02 |       7,220.00 |    0.4% |      0.04 | `apply` |

Nice! That's about 1100 ns we trimmed off there, by removing the extra
allocations. I'm curious, though, how long does Clojure take to do the same
thing?

### Clojure
```clojure
user=> (quick-bench (apply str [1 2 3 4 5 6 7 8 9 10]))
; Evaluation count : 629958 in 6 samples of 104993 calls.
;              Execution time mean : 938.749444 ns
;     Execution time std-deviation : 27.891701 ns
;    Execution time lower quantile : 923.094673 ns ( 2.5%)
;    Execution time upper quantile : 987.172459 ns (97.5%)
;                    Overhead used : 14.193132 ns
```

**Oh no**. Clojure takes about 939 ns, while jank, even with the optimized interface,
takes 6,191 ns. We're not even close!

## Profile, change, benchmark, repeat
Firstly, let's compare the actual code being benchmarked here.

### Generated code
#### Clojure
There is an excellent tool, which has proved useful so many times during
jank's development, called
[clojure-goes-fast/clj-java-decompiler](https://github.com/clojure-goes-fast/clj-java-decompiler).
With just the following:
```clojure
user=> (require '[clj-java-decompiler.core :refer [decompile]])
user=> (decompile (apply str [1 2 3 4 5 6 7 8 9 10]))
```

We get:
```java
public class cjd__init
{
  public static final Var const__0;
  public static final Var const__1;
  public static final AFn const__12;
  
  public static void load()
  {
    ((IFn)cjd__init.const__0.getRawRoot()).invoke
    (
      cjd__init.const__1.getRawRoot(),
      cjd__init.const__12
    );
  }
  
  public static void __init0()
  {
    const__0 = RT.var("clojure.core", "apply");
    const__1 = RT.var("clojure.core", "str");
    const__12 = (AFn)RT.vector(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
  }
  
  static
  {
    __init0();
    // A bit more redacted here ...
}
```

So, to understand this, note that our expression `(apply str [1 2 3 4 5 6 7 8 9 10])` was turned
into a Java class. The constants for `apply` and `str`, which are vars, were
lifted, and our vector constant was also lifted. Those are the three `const__`
members of the class, which are statically initialized. The actual code which
does our `apply` is in `load`. We can see, it basically does the following, if
we sanitize the lifted constants:

```java
apply.getRawRoot().invoke(str.getRawRoot(), vec);
```

Clojure's generated code seems optimal. The vars and vector are both lifted and
the `load` function only gets their roots and invokes (roots can't reasonably be
cached, especially during interactive programming, since vars can be redefined
at any time, including from other threads). Let's see what jank is generating
for this, to ensure it's equally optimized.

#### jank
```cpp
struct gen166 : jank::runtime::object,
                jank::runtime::pool_item_base<gen166>,
                jank::runtime::behavior::callable,
                jank::runtime::behavior::metadatable {
  // Some bits redacted ...

  jank::runtime::context &__rt_ctx;
  jank::runtime::var_ptr const str155;
  jank::runtime::var_ptr const apply154;
  jank::runtime::object_ptr const const165;
  jank::runtime::object_ptr const const164;
  jank::runtime::object_ptr const const163;
  jank::runtime::object_ptr const const162;
  jank::runtime::object_ptr const const161;
  jank::runtime::object_ptr const const160;
  jank::runtime::object_ptr const const159;
  jank::runtime::object_ptr const const158;
  jank::runtime::object_ptr const const157;
  jank::runtime::object_ptr const const156;

  gen166(jank::runtime::context &__rt_ctx)
      : __rt_ctx{__rt_ctx},
        str155{__rt_ctx.intern_var("clojure.core", "str").expect_ok()},
        apply154{__rt_ctx.intern_var("clojure.core", "apply").expect_ok()},
        const165{jank::runtime::make_box<jank::runtime::obj::integer>(10)},
        const164{jank::runtime::make_box<jank::runtime::obj::integer>(9)},
        const163{jank::runtime::make_box<jank::runtime::obj::integer>(8)},
        const162{jank::runtime::make_box<jank::runtime::obj::integer>(7)},
        const161{jank::runtime::make_box<jank::runtime::obj::integer>(6)},
        const160{jank::runtime::make_box<jank::runtime::obj::integer>(5)},
        const159{jank::runtime::make_box<jank::runtime::obj::integer>(4)},
        const158{jank::runtime::make_box<jank::runtime::obj::integer>(3)},
        const157{jank::runtime::make_box<jank::runtime::obj::integer>(2)},
        const156{jank::runtime::make_box<jank::runtime::obj::integer>(1)}
  { }

  jank::runtime::object_ptr call() const override
  {
    using namespace jank;
    using namespace jank::runtime;
    object_ptr call167;
    {
      auto const &vec168(jank::runtime::make_box<jank::runtime::obj::vector>(
          const156, const157, const158, const159, const160, const161, const162,
          const163, const164, const165));
      call167 = jank::runtime::dynamic_call(apply154->get_root(), str155->get_root(), vec168);
    }
    return call167;
  }
};
```

The outline here is similar. jank generates a struct from the expression. We
have constants lifted to members, and we initialize those in the struct's
constructor. Then we have a `call` function which does our work. But, looking at
our `call` function, we can see it's creating our vector, too; jank only lifted the
numbers, not the whole vector! Let's change that.

The changes:
[2a8014dfae6e57273983cee8f2c7f78a2be7fe73](https://github.com/jank-lang/jank/commit/2a8014dfae6e57273983cee8f2c7f78a2be7fe73)

|               ns/op |                op/s |    err% |          ins/op |         branch/op |   miss% |     total | benchmark |
|--------------------:|--------------------:|--------:|----------------:|---------------:|--------:|----------:|:---------- |
|            4,671.71 |          214,054.61 |    0.2% |       23,798.02 |       6,721.00 |    0.3% |      0.03 | `apply` |

Nice! We've gone from 6,191 ns to 4,671 ns by ensuring we lift the vector out.
Our generated `call` function just looks like this now:

```cpp
jank::runtime::object_ptr call() const override
{
  using namespace jank;
  using namespace jank::runtime;
  object_ptr call169 = jank::runtime::dynamic_call
  (
    apply154->get_root(),
    str155->get_root(),
    const166
  );
  return call169;
}
```

Very similar to the generated Clojure `load` function! But still over 4x slower.
We know the generated code is good, so let's dig deeper into what's going on
when we call these functions.

### Sequence lengths
If we follow how `apply` works on the C++ side, it looks like this:

```cpp
object_ptr apply_to(object_ptr const &source, object_ptr const &args)
{
  auto const &s(args->as_seqable()->seq());
  auto const length(detail::sequence_length(s, max_params + 1));
  switch(length)
  {
    case 0:
      return dynamic_call(source);
    case 1:
      return dynamic_call(source, s->first());
    case 2:
      return dynamic_call(source, s->first(), s->next_in_place()->first());
    // more redacted ...
  }
}
```

We need to know how many arguments we're calling the function with, by getting
the sequence length, and then we build out the correct call accordingly. Clojure
does the same thing
[here](https://github.com/clojure/clojure/blob/9e362e2ee4bf1feb942d31dac821e9d2917789b6/src/jvm/clojure/lang/AFn.java#L147). Right now, `detail::sequence_length` is O(n), but our sequences know their length. Let's use that and add a `Countable` behavior to get an O(1) length check here. The new function looks like:

```cpp
size_t sequence_length(behavior::sequence_ptr const &s, size_t const max)
{
  if(s == nullptr)
  { return 0; }
  /* This is allow us to be O(1). */
  else if(auto const * const c = s->as_countable())
  { return c->count(); }

  size_t length{ 1 };
  for(auto i(s->next()); i != nullptr && length < max; i = i->next_in_place())
  { ++length; }
  return length;
}
```

The changes:
[0ec065d8ed6a986690c1055ab29d91cc50680921](https://github.com/jank-lang/jank/commit/0ec065d8ed6a986690c1055ab29d91cc50680921)

|               ns/op |                op/s |    err% |          ins/op |         branch/op |   miss% |     total | benchmark |
|--------------------:|--------------------:|--------:|----------------:|---------------:|--------:|----------:|:---------- |
|            4,320.42 |          231,459.17 |    0.4% |       21,956.02 |       6,150.00 |    0.4% |      0.03 | `apply` |

From 4,671 ns to 4,320 ns. That's good progress, but we're still a long way off
from Clojure's 939 ns.

## Packed variadic args
Once we get into `dynamic_call`, after `apply_to`, we need to check if the
function is variadic and then pack some args accordingly. Let's take a look at
our `str` function, so we can see which path will be taken.

```clojure
(def str
  (fn*
    ([]
     "")
    ([o]
     (native/raw "__value = make_box(#{ o }#->to_string());"))
    ([o & args]
     (native/raw "std::string ret(#{ o }#->to_string().data);
                  auto const * const l(#{ args }#->as_list());
                  for(auto const &elem : l->data)
                  { ret += elem->to_string().data; }
                  __value = make_box(ret);"))))
```

Ok, so when we apply `[1 2 3 4 5 6 7 8 9 10]` to this, we'll use the variadic
arity. The `o` param will be `1` and then `args` will be `(2 3 4 5 6 7 8 9 10)`. The
current implementation passes in a list for `args`, which means that packing
those 9 numbers requires 9 allocations. However, we can see that Clojure uses an
`ArraySeq` for packed arguments instead,
[here](https://github.com/clojure/clojure/blob/9e362e2ee4bf1feb942d31dac821e9d2917789b6/src/jvm/clojure/lang/RestFn.java#L816). Let's do that.

The changes:
[6e8a63ebc98c041ba86e7a1ad6839902d1ead939](https://github.com/jank-lang/jank/commit/6e8a63ebc98c041ba86e7a1ad6839902d1ead939)

|               ns/op |                op/s |    err% |          ins/op |         branch/op |   miss% |     total | benchmark |
|--------------------:|--------------------:|--------:|----------------:|---------------:|--------:|----------:|:---------- |
|            2,533.89 |          394,649.35 |    0.3% |       12,233.02 |       3,471.00 |    0.1% |      0.01 | `apply` |

Yeah! From 4,320 ns down to 2,533 ns. **Building that list was slow!**

## String formatting
If we look at the new implementation of `str`, we're looping over our args and
calling `to_string()` for each.

```clojure
(native/raw "std::string ret(#{ o }#->to_string().data);
             auto const &seq(#{ args }#->as_seqable()->seq());
             ret += seq->first()->to_string();
             for(auto it(seq->next_in_place()); it != nullptr; it = it->next_in_place())
             { ret += it->first()->to_string().data; }
             __value = make_box(ret);")
```

But that means we need to allocate a new `std::string` for every argument, then
concatenate that into our accumulator, which likely requires yet another
allocation. Let's use [fmt](https://github.com/fmtlib/fmt)'s string building to
do this all in place. That means jank's base runtime `object` expands its
interface to have two `to_string` functions:

```cpp
struct object : virtual pool_item_common_base
{
  // redacted ...

  virtual detail::string_type to_string() const = 0;
  virtual void to_string(fmt::memory_buffer &buffer) const;

  // redacted ...
};
```

If we look at the implementation of this for `integer`, we can see a neat usage
of `FMT_COMPILE`. This allows us to compile our format string ahead of time,
leading to very efficient rendering at run-time.

```cpp
void integer::to_string(fmt::memory_buffer &buff) const
{ fmt::format_to(std::back_inserter(buff), FMT_COMPILE("{}"), data); }
```

The changes:
[819e1a178c3be549c894e9386e9dc54513800fe8](https://github.com/jank-lang/jank/commit/819e1a178c3be549c894e9386e9dc54513800fe8)

|               ns/op |                op/s |    err% |          ins/op |         branch/op |   miss% |     total | benchmark |
|--------------------:|--------------------:|--------:|----------------:|---------------:|--------:|----------:|:---------- |
|            2,375.56 |          420,952.59 |    0.3% |       10,751.02 |       3,070.00 |    0.2% |      0.01 | `apply` |

From 2,533 ns to 2,375 ns.

## Further sequence interface optimizations
To kick things off, we added the `next_in_place` function to the sequence
interface, but there are two things which can easily be identified by looking at
the previous `apply_to` snippet:

```cpp
object_ptr apply_to(object_ptr const &source, object_ptr const &args)
{
  auto const &s(args->as_seqable()->seq());
  auto const length(detail::sequence_length(s, max_params + 1));
  switch(length)
  {
    // redacted some ...
    case 2:
      return dynamic_call(source, s->first(), s->next_in_place()->first());
    case 3:
      return dynamic_call
      (
        source,
        s->first(),
        s->next_in_place()->first(),
        s->next_in_place()->first()
      );
    case 4:
      return dynamic_call
      (
        source,
        s->first(),
        s->next_in_place()->first(),
        s->next_in_place()->first(),
        s->next_in_place()->first()
      );
    // more redacted ...
  }
}
```

1\. We're very often following up `next_in_place` with a call to `first`, which
   is another virtual call

  **Solution:** Also add a `next_in_place_first`, which does both. It's not a
  pretty interface, but for cases like `apply_to` with 10 arguments, that's an
  extra 10 virtual calls we save. Worth it.

2\. We have `next_in_place` returning a smart ptr, but for in-place updates, we
   don't need to keep updating reference counts

  **Solution:** Just return a raw pointer from `next_in_place` and stop with all the
  reference counting.

The changes: [c1e8da91bfe65bc6b8b02a9c8636de87a24f6110](https://github.com/jank-lang/jank/commit/c1e8da91bfe65bc6b8b02a9c8636de87a24f6110) and [09ac8e31ef2337b7fb5d046a85302d2755d570f3](https://github.com/jank-lang/jank/commit/09ac8e31ef2337b7fb5d046a85302d2755d570f3)

|               ns/op |                op/s |    err% |          ins/op |         branch/op |   miss% |     total | benchmark |
|--------------------:|--------------------:|--------:|----------------:|---------------:|--------:|----------:|:---------- |
|            2,015.26 |          496,213.24 |    0.1% |        8,978.00 |       2,518.00 |    0.2% |      0.12 | `apply` |

We're nearly under 2k ns now! From 2,375 ns to 2,015 ns was a good win.

## Faster synchronization
At this point, profiling isn't pointing out anything to do with sequences
anymore. Instead, all of the time is spent doing three things:

1. Building strings from integers
2. Getting var roots
3. Updating reference counts

Only one of those is what we really care about, so let's see if we can trim down
the var root accessing. I mentioned earlier that Clojure/jank vars can be
redefined at any time, from any thread, so they require synchronization. I was
using [libguarded](https://github.com/copperspice/cs_libguarded), but I gave
[folly's synchronization](https://github.com/facebook/folly/blob/main/folly/docs/Synchronized.md)
a shot and it was a big win.

The changes: [059e828c789b7782595007dcb5a389fe1db442ac](https://github.com/jank-lang/jank/commit/059e828c789b7782595007dcb5a389fe1db442ac)

|               ns/op |                op/s |    err% |          ins/op |         branch/op |   miss% |     total | benchmark |
|--------------------:|--------------------:|--------:|----------------:|---------------:|--------:|----------:|:---------- |
|            1,776.69 |          562,843.97 |    0.3% |        7,942.00 |       2,214.00 |    0.2% |      0.11 | `apply` |

Oh yeah. We're down from 2,015 ns to 1,776 ns. Under 2x of Clojure's benchmark
time. Profiling still shows that we're spending our time:

1. Building strings from integers
2. Updating reference counts

Since jank is using `boost::intrusive_ptr` and a custom arena memory pool for
object lifecycle managements, for the benefit of deterministic object lifetimes,
there's not much more we can do there. In fact, Clojure's cheating a bit, since
it's not cleaning up any of its garbage in its benchmark, while jank is leaving
everything as it found it.

## Garbage collector?
In truth, I paused for a while here, thinking about what could be done.
For jank, I have so far intentionally chosen not to use a GC. The
C++ part of my brain grasps for
[RAII](https://en.cppreference.com/w/cpp/language/raii) and deterministic object lifetimes.

But, as I thought about the benefits of RAII in Clojure, they became harder to
justify. Here are some of the things I considered.

1. Constructors and destructors make sense for allocating and deallocating
   resources, specifically mutable resources like a DB connection or file handle
2. Deterministic object lifetimes especially matter when objects contain these
   resources, since we can't have them lingering beyond when we need them
3. Clojure handles resource management in an entirely different way and its
   object system is not used for this; it's just used for the polymorphic
   treatment of various Clojure runtime types
4. Nearly all of the Clojure runtime types are just immutable values;
   knowing when they are cleaned up doesn't matter, outside of ensuring memory
   usage doesn't grow too much. They don't have destructors that are doing
   anything important.

So, the only arguments remaining for not using a GC for a Clojure dialect were:

1. GC pauses can limit the language's use cases
2. Deterministic object lifetimes grant a better idea of how much memory will be
   used at any given time, which is saner for environments such as embedded
   systems

For the first point, the [Boehm GC](https://github.com/ivmai/bdwgc) supports an
incremental mode, which does small amounts of freeing work during allocations,
rather than pausing. For the second point, I had to consider if this ever
mattered to me when I was writing a Clojure application, or if it would matter
to me with what I have planned for jank. I figured I'd see how much the GC moved
the needle first, before deciding anything.

The changes: [995bec7377fec04ddc5744eed5f1d1149ccc3019](https://github.com/jank-lang/jank/commit/995bec7377fec04ddc5744eed5f1d1149ccc3019)

|               ns/op |                op/s |    err% |          ins/op |         branch/op |   miss% |     total | benchmark |
|--------------------:|--------------------:|--------:|----------------:|---------------:|--------:|----------:|:---------- |
|              966.15 |        1,035,032.47 |    3.2% |        5,250.10 |       1,353.26 |    0.2% |      0.09 | `apply` |

Ok, **that's a huge win**. Clojure was at 939 ns and we're now at 966 ns. I had
said in [#jank](https://clojurians.slack.com/archives/C03SRH97FDK/p1671038588821099?thread_ts=1671030141.013459&cid=C03SRH97FDK), when someone asked if jank was going to use a GC, that I'd rather have determinism, if it means jank is marginally slower. However, the difference here is not marginal. To make matters worse, the current reference counting approach isn't even thread-safe; I'd need to use an atomic size_t for that, which would slow things down even more. This GC is thread-safe.

But we're so close to actually *beating* Clojure, why not try?

## A couple more wins
Since we brought in folly for synchronization, we might as well use its
[fbstring](https://github.com/facebook/folly/blob/main/folly/docs/FBString.md),
which is compliant with `std::string` and is more heavily optimized.

The changes: [f323d5f4e7854c029a79cc28865dabd5e9e063d0](https://github.com/jank-lang/jank/commit/f323d5f4e7854c029a79cc28865dabd5e9e063d0)

Also, if we get really picky in the profiler, we can see that the jank sequence
we were using for iterating over the `[1 2 3 4 5 6 7 8 9 10]` vector is showing
up, since we're incrementing an dereferencing
[immer](https://github.com/arximboldi/immer) iterators. What if we just use an
index, like with our `array_sequence`?

The changes: [42132be331862688e09c42c15e4af8285b0f3591](https://github.com/jank-lang/jank/commit/42132be331862688e09c42c15e4af8285b0f3591)

|               ns/op |                op/s |    err% |          ins/op |         branch/op |   miss% |     total | benchmark |
|--------------------:|--------------------:|--------:|----------------:|---------------:|--------:|----------:|:---------- |
|              911.60 |        1,096,970.83 |    0.9% |        5,316.51 |       1,350.48 |    0.2% |      0.01 | `apply` |

We can now run that Clojure expression over a million times per second and, in
this micro benchmark, jank is faster than Clojure. **We can do in 912 ns what Clojure takes a paltry 939 ns to do**.
More, even, since remember that our benchmark also includes calling the function wrapping the code.

## Closing notes
This was a lot of fun to do, and I did not have the intention of starting down
this rabbit hole when I was just looking to implement jank's sequence API.
Still, we've ended up with a much better understanding of Clojure, much more
optimized sequence, format, synchronization, and string systems in jank, and,
well, a garbage collector.

It's important to call out that this is a micro benchmark, which means it
doesn't mean that much in practice. The JVM, and thus Clojure, has an optimizing
JIT compiler, which jank does not have. Each of these languages are now cheating
by not cleaning up their garbage during this benchmark, but how they do and when
they do has a huge impact on the usability of the runtime. Neither of these
programs were AOT compiled, since benchmarking was done in a REPL. Furthermore,
there's a lot more to most programs than just what's been covered here. Though
I'm very excited about the progress, I can't reasonably claim jank is faster
than Clojure for anything but this micro benchmark.

That is, until I make larger benchmarks and spend weeks optimizing those.

## Would you like to join in?
1. Join the community on [Slack](https://clojurians.slack.com/archives/C03SRH97FDK)
2. Join the design discussions on [Github](https://github.com/jank-lang/jank/discussions)
3. Considering becoming a [Sponsor](https://github.com/sponsors/jeaye)
