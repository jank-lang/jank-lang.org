Title: Starting on seamless interop in jank
Date: Mar 28, 2025
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Draft: true
Description: jank is the first Lisp to be able to seamlessly reach into C++.
             Check it out!

Howdy, folks! We're one month into me working on jank's seamless C++ interop and
I have some exciting progress to share. This quarter's work is being sponsored
not only by all of my individual Github sponsors, but also by Clojurists Together.
Thank you for the support!

## Seamless interop
Clojure's interop with Java isn't sold as being "seamless", but it really is.
Using Clojure's existing syntax, we can create Java objects, access their
members, call their methods, and there's no ceremony involved. This is a super
power of Clojure and I think it's critical that each Clojure dialect reproduces
it for its host. In fact, we've seen how not doing this seamlessly can impact
the user experience, in earlier ClojureScript days. For jank, I want to do
this right from the beginning.

However, jank's host is C++. How many languages can you name which have seamless
C++ interop? Only [Swift](https://www.swift.org/documentation/cxx-interop/) comes to mind.
Even worse, doing this from a dynamically
typed and JIT compiled language? This is new ground we're treading. I'll
confidently say jank is the first Lisp to ever do this. So, one month in, what
can jank do?

## C++ values
Let's start with a simple example. We'll say there's an existing global C++
value which we want to access.

<!--
(cpp/raw "#include string")
cpp/std.string.npos
-->

<div class="wide-figure">
  <figure>
    <img src="/img/blog/2025-05-02-starting-seamless-interop/npos.png"></img>
  </figure>
</div>

The [npos](https://en.cppreference.com/w/cpp/string/basic_string/npos) of C++'s
string is used to represent the index of something which isn't present. Its
value is `-1`, but since it's in an unsigned type, we get the largest possible
value (all bits are `1`).

Now, in order to access this from jank, we need to first include the C++
standard `<string>` header. We use `cpp/raw` for this, which allows us to
globally issue C++ declarations. We can do this from anywhere and it will only
affect the global C++ JIT compiler. jank also supports `-I` and `-l` CLI flags
for locating headers and linking to libs, in case we want to use our own C++
libs.

Once we've included this header, we have access to all declarations and
definitions within it. The next line, `cpp/std.string.npos` accesses the value
`std::string::npos`. We use a `.` instead of `::` here since the latter wouldn't
be valid Clojure. Note, also, everything is still under the `cpp/` namespace,
which is a special namespace reserved for C++ interop in jank.

However, once we've accessed the value, we need to return it through the jank
runtime so we can show the value in the REPL client. This is where some more magic
happens. jank's runtime works with its own object model. There is a "base"
object of sorts and then a bunch of typed objects. However, `std::string::npos`
isn't one of those. It's just a `size_t`, which is a native `unsigned long`.
jank has a conversion [trait](https://www.internalpointers.com/post/quick-primer-type-traits-modern-cpp)
which can be specialized for any type, to provide conversions to/from jank's
objects. By default, jank has some built-in type conversions already
specialized.

When the jank compiler sees we're trying to return a `size_t`, it'll try to
JIT instantiate the `jank::runtime::convert<size_t>` template, to see if we have
a valid conversion trait. If that instantiation fails, we get a compiler error.
For example:

<!--
(cpp/raw "struct foo{}; foo f;")
cpp/f
-->

<div class="wide-figure">
  <figure>
    <img src="/img/blog/2025-05-02-starting-seamless-interop/invalid-conversion.png"></img>
  </figure>
</div>

So, accessing a single value from the REPL is not as simple as it may seem. This
is easier in Clojure, since both Clojure and Java share the same base `Object`.
Since every class instance is an `Object`, we can freely pass any Java value through
any Clojure function or store it in a Clojure data structure. In the native
world, we don't have such niceties. Each top-level type is concretely separate.
This means that, in order to cross function boundaries, conversions or boxing must be
done.

## Constructors
In jank, we can now construct stack-allocated C++ objects directly. There is
no additional allocation, boxing, or conversions done by the jank compiler
unless that value is returned from a jank function or passed as a parameter to a
jank function.

<!--
(cpp/raw "struct bar{ bar(size_t s) { printf(\"bar ctor with %zu\\n\", s); } };")
(let [b (cpp/bar. cpp/std.string.npos)])
-->

<div class="wide-figure">
  <figure>
    <img src="/img/blog/2025-05-02-starting-seamless-interop/ctor-1.png"></img>
  </figure>
</div>

In this case, there are three different constructor overloads for `bar`.

* `bar(bar const &)` => The default copy constructor.
* `bar(bar &&)` => The default move constructor.
* `bar(size_t)` => Our user-defined constructor.

When we try to create `b`, we need to match the correct overload, given the
arguments. jank will determine the argument types, find all of the overloads,
and them ask Clang to find the best match. Clang will consider the argument
types, implicit conversions, default arguments, etc. However, let's say we provide a jank
value instead of `std::string::npos`.

<!--
(cpp/raw "struct bar{ bar(size_t s) { printf(\"bar ctor with %zu\\n\", s); } };")
(let [b (cpp/bar. 17)])
-->

<div class="wide-figure">
  <figure>
    <img src="/img/blog/2025-05-02-starting-seamless-interop/ctor-2.png"></img>
  </figure>
</div>

In this case, `17` is a `jank::runtime::object_ref`. Clang will not find any
matching constructor overload. Instead of giving up, jank will do a second pass, where
it checks each argument type to see if an automatic jank conversion can happen.
If multiple overloads can support a jank conversion for the same argument, we have an
ambiguity and jank will fail. However, if we can narrow down a matching
overload, jank will choose that and automatically do the conversions for us.

## Casting
Finally, we can explicitly cast a value to another type. This works both as the
equivalent of a C++ `static_cast` and also as a way of opting into jank's
conversion trait. A helpful trick here is to cast in order to disambiguate an
overloaded call. Let's consider this example. Note that I haven't actually built
out the error messages for these yet, so it shows as an internal analysis error
and the message itself is lacking helpful details. It's sufficient for me to
know that the right error has been triggered, though.

<!--
(cpp/raw "struct bar{ bar(float f){ } bar(size_t s){ } };")
(let [b (cpp/bar. 17)]
  )
-->

<div class="wide-figure">
  <figure>
    <img src="/img/blog/2025-05-02-starting-seamless-interop/cast-1.png"></img>
  </figure>
</div>

We can see that `bar` has two user-defined constructors. One takes a `float` and
another takes a `size_t`. Both of those are already specialized for jank's
conversion trait, so jank can't know which constructor to choose. Now, if we add a cast,
we can clear up this ambiguity.

<!--
(cpp/raw "struct bar{ bar(float f){ } bar(size_t s){ } };")
(let [b (cpp/bar. (cpp/cast cpp/size_t 17))]
  )
-->

<div class="wide-figure">
  <figure>
    <img src="/img/blog/2025-05-02-starting-seamless-interop/cast-2.png"></img>
  </figure>
</div>

### Subscribe
Before I go into exactly how we're stitching together JIT compiled C++ code with
jank's LLVM IR, please consider subscribing to jank's mailing list. This is
going to be the best way to make sure you stay up to date with jank's releases,
jank-related talks, workshops, and so on.

<div style="margin: auto; text-align: center;">
  <form method="post" target="blank_" action="https://listmonk.jank-lang.org/subscription/form" class="listmonk-form">
    <div>
      <input type="hidden" name="nonce" />
      <p><input type="email" name="email" required placeholder="E-mail" /></p>
      <p><input type="hidden" name="name" placeholder="Name (optional)" /></p>
      <p><input id="a132c" type="hidden" name="l" value="a132cb7d-6dc0-450c-8789-41d4fd880548" /></p>
      <p><button type="submit" class="subscribe-button">Subscribe</button></p>
    </div>
  </form>
</div>

## LLVM IR
Let's go back to our simple example of just evaluating `cpp/std.string.npos`
from the terminal REPL client. The REPL client will automatically wrap many
expressions we give it in a function, which is then JIT compiled, and
immediately invoked.
[IIFE](https://en.wikipedia.org/wiki/Immediately_invoked_function_expression) is
the term for this. Since our C++ value is wrapped in a function, we end up with
the implicit conversion being required, since the function is going to return
our value. So, the function needs to:

1. Look up the value from C++ land
2. Convert the value into a jank object
3. Return the value

Let's take these one at a time.

### Looking up the value
Reaching into C++ requires an understanding of C++ structs, classes, templates,
padding, alignment, and so much more. There's no way we can reasonably do all of
that ourselves, so we rely on Clang to do it for us. To do this, we generate
some C++ code at run-time and give it to Clang to JIT compile. To get our value,
the C++ code will look _something_ like this:

```cpp
inline void jank_generated_helper_0(void *, int, void **, void *ret)
{ new (ret) size_t{ std::string::npos }; }
```

Ignore all of the parameters except for the last, for right now.
When we call this function, we pass in a pointer to a buffer which is large enough to
hold a `size_t`. The function then initializes that memory with the value held in `std::string::npos`.
We can generate this knowing the type of `std::string::npos` ahead of time.
Clang will compile this to a C function, which we can then call from our IR. We
pass a pointer to a buffer rather than dealing with return values, since that's
an [ABI nightmare](https://yorickpeterse.com/articles/the-mess-that-is-handling-structure-arguments-and-returns-in-llvm/).

Given the C++ code above, Clang gives an IR function like this:

```llvm
define linkonce_odr dso_local void @jank_generated_helper_0(
          ptr noundef %0,
          i32 noundef %1,
          ptr noundef %2,
          ptr noundef nonnull %3) #0 comdat {
  %5 = alloca ptr, align 8
  %6 = alloca i32, align 4
  %7 = alloca ptr, align 8
  %8 = alloca ptr, align 8
  store ptr %0, ptr %5, align 8
  store i32 %1, ptr %6, align 4
  store ptr %2, ptr %7, align 8
  store ptr %3, ptr %8, align 8
  %9 = load ptr, ptr %8, align 8
  store i64 -1, ptr %9, align 8
  ret void
}
```

There's a whole lot of hoopla and then ultimately a `store i64 -1, ptr %9`,
which is really all we care about.

Given this helper, we can start building our own function to wrap our C++
value. We need to (stack) allocate space which is large enough to hold a
`size_t` and then we can call our C++ helper in order to initialize the memory.

```llvm
define ptr @clojure_core_cpp_value_7603_0() {
entry:
  %0 = alloca i8, i32 8, align 8
  call void @jank_generated_helper_0(ptr null, i32 0, ptr null, ptr %0)
```

This is a good start. After this, we'll have `-1` in the memory allocated.
Now, however, we need to convert this into a jank object. In order to do that,
we'll generate another C++ helper. This one will do the necessary conversion
using jank's trait. It'll look something like this:

```cpp
inline void jank_generated_helper_1(void *, int, void **args, void *ret)
{
  auto const arg1{ *(size_t*)args[0] };
  new (ret) obj::integer_ref{ convert<size_t>::into_object(arg1) };
}
```

That'll generate into a rather large IR function which I'm not going to paste
here. You'll see we end up using another of the parameters, though, which is our
`args`. The `args` parameter is a pointer to an array of pointers to arguments.
Again, when generating this C++ helper, we know the type that everything will
be, so we can do some dirty casting from `void*` to get it there.

Now we can extend our IR function to call into this second helper.

```llvm
define ptr @clojure_core_cpp_value_7603_0() {
entry:
  ; Store our -1 in %0.
  %0 = alloca i8, i32 8, align 8
  call void @jank_generated_helper_0(ptr null, i32 0, ptr null, ptr %0)

  ; Now convert our -1 into a jank object.
  %into_object.args = alloca [1 x ptr], align 8
  %"into_object.args[0]" = getelementptr inbounds [1 x ptr], ptr %into_object.args, i32 0, i32 0
  store ptr %0, ptr %"into_object.args[0]", align 8
  %into_object.ret_alloc = alloca i8, i32 8, align 8
  call void @jank_generated_helper_1(ptr null, i32 0, ptr %into_object.args, ptr %into_object.ret_alloc)
  %ret = load ptr, ptr %into_object.ret_alloc, align 8
```

I've tried to put some helpful names into the generated IR, to aid in reading.
We can see here that we generate the args array for our call, we store `%0` (our
`size_t` which we read earlier) into the zeroth spot of the array, we allocate
enough space for the return value, and then we call our second helper. 

There's one last catch, though. The `convert<size_t>::into_object` function
returns a typed jank object, which is `obj::integer_ref`. This is great for
taking advantage of type information, but we're crossing jank function
boundaries here, so everything needs to be a type-erased `object_ref`. We can do
this by just shifting the pointer based on the correct offset to the base from
`obj::integer`. Fortunately, jank will do for us automatically when it detects
that we need a type-erased object and we have a typed object. Our final IR function
looks like this:

```llvm
define ptr @clojure_core_cpp_value_7603_0() {
entry:
  ; Store our -1 in %0.
  %0 = alloca i8, i32 8, align 8
  call void @jank_generated_helper_0(ptr null, i32 0, ptr null, ptr %0)

  ; Now convert our -1 into a jank object.
  %into_object.args = alloca [1 x ptr], align 8
  %"into_object.args[0]" = getelementptr inbounds [1 x ptr], ptr %into_object.args, i32 0, i32 0
  store ptr %0, ptr %"into_object.args[0]", align 8
  %into_object.ret_alloc = alloca i8, i32 8, align 8
  call void @jank_generated_helper_0(ptr null, i32 0, ptr %into_object.args, ptr %into_object.ret_alloc)
  %ret = load ptr, ptr %into_object.ret_alloc, align 8

  ; Adjust the pointer to the base.
  %ret_base = getelementptr inbounds i8, ptr %ret, i32 8
  ret ptr %ret_base
}
```

This ends up being a lot of work for one line of jank code, but there's a great
deal of magic going on behind the scenes. What's amazing, though, is that all of
this gets completely optimized away. With `-O2` settings on our IR
optimizations, LLVM will turn our function into this:

```llvm
define nonnull ptr @clojure_core_cpp_value_7603_0() {
entry:
  %0 = tail call ptr @_ZN4jank7runtime7convertImE11into_objectEm(i64 noundef -1)
  %ret_base = getelementptr inbounds i8, ptr %0, i64 8
  ret ptr %ret_base
}
```

The `-1`, which came from `std::string::npos`, was completely inlined. Then we
just call our conversion function and shift our pointer to cast from
`obj::integer_ref` to `object_ref`. Phew, that's it!

### What's next?
We're one month into the quarter and I'm pleased with the progress so far.
However, there's a lot remaining work to do. I still need to tackle free/static
function calls, member access, member function calls, operators, dynamic
allocations, complex type support, and automatic destructors for locals with the
same guarantees C++ provides. This will definitely keep me busy for the quarter!
Stay tuned for my next update in a month.

## Would you like to help out?
1. Join the community on [Slack](https://clojurians.slack.com/archives/C03SRH97FDK)
2. Join the design discussions or pick up a ticket on [GitHub](https://github.com/jank-lang/jank)
3. Considering becoming a [Sponsor](https://github.com/sponsors/jeaye) <span class="icon mr-1" style="color: rgb(201, 97, 152);"> <i class="gg-heart"></i></span>
4. **Better yet, reach out to discuss corporate sponsorship!**
