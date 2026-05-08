Title: jank now has its own custom IR
Date: May 08, 2026
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Description: The jank compiler has a new super power. With that, we push
             jank to the limits to see who has the fastest fibonacci in the west.

Good news, everyone! jank has a new custom intermediate representation (IR) and we're
using it to optimize jank to compete with the JVM. We'll dive into more of that
today, but first I want to say thank you to my [Github sponsors](https://github.com/sponsors/jeaye)
and to Clojurists Together for sponsoring me this whole year. You all are
helping a great deal. I am still searching for a way to continue working on jank
full-time with an income which will cover rent and groceries, so if you've not
yet chipped in a sponsorship, now's a great time!

## What is an intermediate representation (IR)?
Compilers often represent programs as a more abstract set of instructions than a
target CPU instruction set can afford. This has a few added benefits. Firstly,
the program can be represented in a way which could later be lowered to
different CPU architectures, such as x86_64 or arm64. Since intermediate
representations are often higher level than CPU architectures, they can
generally be more portable. Secondly, IRs can be specifically designed to
represent the program in a way which makes writing certain optimizations easier,
such as single static assigment (SSA) form. Finally, IR designers get to choose
the level of abstraction of the IR to match the semantics they're aiming to
represent, which can either make an IR more general or more specific to a
certain language.

There are many common popular IRs, such as the JVM's bytecode, the CLR's common
intermediate language (CIL), GCC's GIMPLE, LLVM's IR, and so on. Some
compilers may move the program through multiple IRs during compilation.

## Custom IR rationale
Historically, jank has not been an optimizing compiler. We've delegated
basically all of that work to LLVM, based on the C++ or LLVM IR which we would
generate. However, LLVM IR works at a very low level, compared to Clojure. It
has no concept of Clojure's vars, transients, persistent data structures, lazy
sequences, and so on. Clojure's dynamism is granted by a great deal of both
polymorphism and indirection, but this means LLVM has very few optimization
opportunities when it's dealing with the LLVM IR from jank.

The optimization work done previously on jank helped optimize its runtime, and
the compiler itself, but less so the code being compiled by the compiler. In the past
two months, I have sought to change this.

I wanted an IR which operated at the level of Clojure's semantics. This would be
much higher level than LLVM IR and even much higher level than JVM's bytecode.
Since I'm not building a general virtual machine (VM) or compiler platform, I
don't need to generalize the IR for different languages. I can make jank's IR
specifically tailored to jank, which gives us even more power for optimizations.
As far as I know, no Clojure dialects have taken this step.

## Custom IR details
I have written a reference for jank's IR in the jank book
[here](https://book.jank-lang.org/dev/ir.html). This reference is targeted at
people who're working on jank itself, since I'm making no promises on the
stability of jank's IR at this point. However, I will copy some of that here to
illustrate jank's IR and help provide a mental model for what's to come. Let's
examine this simple Clojure function.

```clojure
(defn greet [name]
  (if (= "jeaye" name)
    (println "Are you me?!")
    (println (str "Hello, " name "!"))))
```

jank's IR is stored in memory as C++ data structures, but it is renderable to
Clojure data for debugging and testing. This is not full serialization, since it
cannot round-trip back into the jank compiler from the IR, due to all of the
Clang AST internal data we have on hand. Let's take a look at the jank IR module
for this function.

```clojure
{:name user_greet_82687
 :lifted-vars {clojure_core_SLASH_str_82694 clojure.core/str
               clojure_core_SLASH_println_82691 clojure.core/println
               clojure_core_SLASH__EQ__82689 clojure.core/=}
 :lifted-constants {const_82693 "!"
                    const_82692 "Hello, "
                    const_82690 "Are you me?!"
                    const_82688 "jeaye"}
 :functions [{:name user_greet_82687_1
              :blocks [{:name entry
                        :instructions [{:name greet :op :parameter :type "jank::runtime::object_ref"}
                                       {:name name :op :parameter :type "jank::runtime::object_ref"}
                                       {:name v3 :op :literal :value "jeaye" :type "jank::runtime::obj::persistent_string_ref"}
                                       {:name v4 :op :var-deref :var clojure_core_SLASH__EQ__82689 :type "jank::runtime::object_ref"}
                                       {:name v5 :op :dynamic-call :fn v4 :args [v3 name] :type "jank::runtime::object_ref"}
                                       {:name v7 :op :truthy :value v5 :type "bool"}
                                       {:name v8 :op :branch :condition v7 :then if0 :else else1 :merge nil :shadow nil :type "void"}]}
                       {:name if0
                        :instructions [{:name v9 :op :literal :value "Are you me?!" :type "jank::runtime::obj::persistent_string_ref"}
                                       {:name v10 :op :var-deref :var clojure_core_SLASH_println_82691 :type "jank::runtime::object_ref"}
                                       {:name v11 :op :dynamic-call :fn v10 :args [v9] :type "jank::runtime::object_ref"}
                                       {:name v12 :op :ret :value v11 :type "jank::runtime::object_ref"}]}
                       {:name else1
                        :instructions [{:name v13 :op :literal :value "Hello, " :type "jank::runtime::obj::persistent_string_ref"}
                                       {:name v14 :op :literal :value "!" :type "jank::runtime::obj::persistent_string_ref"}
                                       {:name v15 :op :var-deref :var clojure_core_SLASH_str_82694 :type "jank::runtime::object_ref"}
                                       {:name v16 :op :dynamic-call :fn v15 :args [v13 name v14] :type "jank::runtime::object_ref"}
                                       {:name v17 :op :var-deref :var clojure_core_SLASH_println_82691 :type "jank::runtime::object_ref"}
                                       {:name v18 :op :dynamic-call :fn v17 :args [v16] :type "jank::runtime::object_ref"}
                                       {:name v19 :op :ret :value v18 :type "jank::runtime::object_ref"}]}]}]}
```

jank's IR is SSA-based, meaning that each name is only assigned once. This makes
entire categories of optimizations much easier to reason about. jank's IR is
also represented as a control flow graph (CFG), which is composed of one or more
basic blocks, each with exactly one terminating instruction (branch, jump, throw, ret, etc).

As we can see from the IR module, jank handles the lifting of vars and
constants, and it has instructions at the level of Clojure's semantics, for
dereferencing vars, calling functions, and so on. Let's take a look at the
generated C++ from this IR.

```cpp
extern "C" jank::runtime::object_ref
user_greet_19_1(jank::runtime::object_ref const greet, jank::runtime::object_ref name)
{
  auto const v3(const_33);
  auto const v4(clojure_core_SLASH__EQ__34->deref());
  auto const v5(jank::runtime::dynamic_call(v4, v3, name));
  auto const v7(jank::runtime::truthy(v5));
  if(v7)
  {
    auto const v9(const_35);
    auto const v10(clojure_core_SLASH_println_36->deref());
    auto const v11(jank::runtime::dynamic_call(v10, v9));
    return v11;
  }
  else
  {
    auto const v13(const_37);
    auto const v14(const_38);
    auto const v15(clojure_core_SLASH_str_39->deref());
    auto const v16(jank::runtime::dynamic_call(v15, v13, name, v14));
    auto const v17(clojure_core_SLASH_println_36->deref());
    auto const v18(jank::runtime::dynamic_call(v17, v16));
    return v18;
  }
}
```

If you compare the C++ to the IR, you can immediately see that correlation. The
C++ variables are named to match the IR variables. A var dereference just
becomes a call to `->deref()` on the var. A dynamic call just becomes a
`jank::runtime::dynamic_call`.

## Optimizing the IR
It took about six weeks to design and implement the IR, including reworking our
C++ code generation to generate from the IR instead of from jank's AST. At this
point, we're not yet running any optimization passes on the IR. However, we have
everything we need to start doing that. I wanted to prioritize getting the new IR
pipeline merged, rather than building it out as much as possible, since six
weeks is already a long time to be branched from `main`. Now that the IR is
merged in, my approach will be to pick up one benchmark at a time and optimize
it as needed until I'm satistified and/or cannot optimize it any further. Some
of those optimizations will involve the IR directly, while others will not.

If you're interested in more of the technical development side of this IR, there
are a few videos on the [jank TV YouTube channel](https://www.youtube.com/@jank-tv)
from the various Twitch streams I did while working on the IR. These videos go
_way_ into the weeds of implementing things.

With the new IR introduced, let's jump into optimizing our first benchmark:
recursive fibonacci.

## Interlude
Before we proceed, please consider subscribing to jank's mailing list. This is
going to be the best way to make sure you stay up to date with jank's releases,
jank-related talks, workshops, and so on. It's *very* low traffic.

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

## Optimizing recursive fibonacci
Our first benchmark for this round of optimization is a recursive fibonacci
implementation. It's just five lines long. Our goal is to have jank be at least as
fast as Clojure JVM, if not faster, but we have to earn it.

```clojure
(defn fibonacci [n]
  (if (<= n 1)
    n
    (+ (fibonacci (- n 1))
       (fibonacci (- n 2)))))
```

This may seem like a trivial benchmark to optimize. You might wonder why would
this be representative of a real world application. In reality, this benchmark
covers some essential aspects of the compiler and runtime.

1. **Polymorphic arithmetic and relational predicates.** Basically every program
   crunches numbers and needs to do so quickly.
2. **Recursion.** Many popular algorithms, especially in Lisps, are recursive.
   Being able to handle these patterns efficiently is important.
3. **Garbage creation and collection.** The trash truck may come every week, but
   that doesn't mean we should be generating as much garbage as we can.
4. **In general, the ability for the language runtime to get out of the way.**
   If we're trying to calculate fibonacci numbers, we don't want anything
   showing up in the profiler which is unrelated to fibonacci numbers.

### Baseline fibonacci timing
We're going to use Clojure JVM to get our baseline benchmark numbers and then
we'll aim to beat those numbers with jank.

Note that all numbers in this post are measured on my five year old x86_64
desktop with an AMD Ryzen Threadripper 2950X on NixOS with OpenJDK 21. When I
say "JVM" in this post, I mean OpenJDK 21.

```bash
❯ clojure -Sdeps '{:deps {criterium/criterium {:mvn/version "0.4.6"}}}'
Clojure 1.12.4
user=> (require '[criterium.core :refer [quick-bench]])
nil

user=> (defn fibonacci [n]
  (if (<= n 1)
    n
    (+ (fibonacci (- n 1))
       (fibonacci (- n 2)))))
#'user/fibonacci

user=> (quick-bench (fibonacci 35))
```

Clojure takes about 200 milliseconds to calculate `(fibonacci 35)`. This is our
baseline!

#### Be careful of `lein repl`
Note that I originally did my benchmarking for Clojure in `lein repl`, which
gives incredibly different results. On my system, instead of 200 milliseconds,
Clojure clocks in around 2,800 milliseconds instead! There are some notes
[here](https://github.com/technomancy/leiningen/issues/1149#issuecomment-16596462)
stating that `lein repl` disables some JVM optimizations which apparently play a
key role here. Thank you to Kyle Cesare for pointing this out.

### Initial jank timing
Starting from jank's `main` a few weeks ago, we're going to use the same `fibonacci`
definition, but we don't have criterium, since that's a library for the JVM.
Instead, jank has its own benchmarking library, shipped along with jank itself.

```clojure
(defn fibonacci [n]
  (if (<= n 1)
    n
    (+ (fibonacci (- n 1))
       (fibonacci (- n 2)))))

(require '[jank.perf])
(jank.perf/benchmark {:label "fib"} (fibonacci 35))
```

If we run this with optimizations enabled and eager compilation, we can get our
initial numbers.

```bash
❯ jank run -O3 --eagerness eager fib.jank
```

jank clocks in at 5,522 milliseconds. That's... not fast. Not compared to the
JVM's 200 milliseconds.

<div class="small-figure">
  <figure>
    <img src="/img/blog/2026-05-08-optimization/jank-1.plot.svg"></img>
  </figure>
</div>

#### Inlining arithmetic
To kick things off, I know that Clojure is inlining math calls and jank used to
have a hacky solution to that which was removed. It's time to do this properly.
Clojure handles inlining via metadata, since the body of a function from another
namespace is not available. This is not specifically a Clojure problem, since
it's also exactly how C and C++ work. Calls to C or C++ functions across
translation units won't be inlined unless link time optimizations (LTO) are
used. The only other option is to move the definition into a header file and
mark the function `inline`, so that every translation unit has its own copy. In
Clojure, we can achieve the same effect of "putting the function in a header" by
changing the metadata of the var to include inlining information, since anyone
can read var metadata from anywhere. Let's see an example of this.

```clojure
(defn
  ^{:inline (fn [l r]
              (list 'cpp/jank.runtime.max l r))
    :inline-arities #{2}}
  max
  ([x]
   x)
  ([l r]
   (cpp/jank.runtime.max l r))
  ([l r & args]
   (let [res (cpp/jank.runtime.max l r)]
     (if (empty? args)
       res
       (recur res (first args) (next args))))))
```

Here, we have `clojure.core/max`, which defines some metadata containing two
keys: `:inline` and `:inline-arities`. The latter is a set of arities to
inline. Here, we only care about the `[l r]` arity. The value of `:inline` is
an actual function to call to get the body for that arity. In the case of `max`,
we just want to inline a C++ call to `jank::runtime::max`.

The inlining is done during analysis, rather than in an IR pass. When we
find a function call through a var, we check the var's metadata and call the
corresponding `:inline` function if present. You can think of this as a sister
to macro expansion, since it works very similarly.

This style of inlining has some *huge* benefits. Firstly, we get to remove the var
interning and dereference of `clojure.core/max`. Secondly, since every Clojure
function needs boxed parameters, if we're working with native values, we don't
need to box them before calling `max`. Thirdly, if `max` returns an unboxed
native value, we don't need to box it to return from the function. This allows
us to avoid boxing and better propagate type information.

After adding inline support to jank's analyzer and updating the metadata for all
arithmetic functions, we can check our new benchmark results. This drops us from 
5,522 milliseconds to 2,309 milliseconds. It's nice to start with a huge win.

<div class="small-figure">
  <figure>
    <img src="/img/blog/2026-05-08-optimization/jank-2.plot.svg"></img>
  </figure>
</div>

#### Eliminating extra IR instructions
Next, let's take a look at the IR for our fibonacci function. I love inspecting
the IR for jank functions now, since is provides such a nice view into how the
jank compiler sees the code.

```clojure
{:name user_fibonacci_82580
 :lifted-vars {}
 :lifted-constants {const_82598 2
                    const_82597 1}
 :functions [{:name user_fibonacci_82580_1
              :blocks [{:name entry
                        :instructions [{:name fibonacci :op :parameter :type "jank::runtime::object_ref"}
                                       {:name n :op :parameter :type "jank::runtime::object_ref"}
                                       {:name v3 :op :literal :value 1 :type "jank::runtime::obj::integer_ref"}
                                       {:name v4 :op :cpp/call :value "jank::runtime::lte" :args [n v3] :type "bool"}
                                       {:name v5 :op :cpp/into-object :value v4 :type "jank::runtime::object_ref"}
                                       {:name v7 :op :truthy :value v5 :type "bool"}
                                       {:name v8 :op :branch :condition v7 :then if0 :else else1 :merge nil :shadow nil :type "void"}]}
                       {:name if0
                        :instructions [{:name v9 :op :ret :value n :type "jank::runtime::object_ref"}]}
                       {:name else1
                        :instructions [{:name v10 :op :literal :value 1 :type "jank::runtime::obj::integer_ref"}
                                       {:name v11 :op :cpp/call :value "jank::runtime::sub" :args [n v10] :type "jank::runtime::object_ref"}
                                       {:name v12 :op :named-recursion :fn fibonacci :args [v11] :type "jank::runtime::object_ref"}
                                       {:name v13 :op :literal :value 2 :type "jank::runtime::obj::integer_ref"}
                                       {:name v14 :op :cpp/call :value "jank::runtime::sub" :args [n v13] :type "jank::runtime::object_ref"}
                                       {:name v15 :op :named-recursion :fn fibonacci :args [v14] :type "jank::runtime::object_ref"}
                                       {:name v16 :op :cpp/call :value "jank::runtime::add" :args [v12 v15] :type "jank::runtime::object_ref"}
                                       {:name v17 :op :ret :value v16 :type "jank::runtime::object_ref"}]}]}]}
```

So we have three blocks in our function. We start at the `entry` block, we grab
our parameter `n` and the literal `1` and we do our `<=` check, which has been
inlined as a `cpp/call` instruction which returns `bool`.

```clojure
{:name n :op :parameter :type "jank::runtime::object_ref"}
{:name v3 :op :literal :value 1 :type "jank::runtime::obj::integer_ref"}
{:name v4 :op :cpp/call :value "jank::runtime::lte" :args [n v3] :type "bool"}
```

We then convert that bool into a boxed object and check if it's `truthy` so we
can branch.

```clojure
{:name v5 :op :cpp/into-object :value v4 :type "jank::runtime::object_ref"}
{:name v7 :op :truthy :value v5 :type "bool"}
{:name v8 :op :branch :condition v7 :then if0 :else else1 :merge nil :shadow nil :type "void"}
```

This can be optimized away, since the result of our `<=` check (`v4`) was already a
`bool`. We turned it into an object just so we can turn it back into a `bool`.
Ideally, the branch condition can just be `v4` instead.

Before that, let's finish examining our IR. We either branch to `if0`, which just returns our
result, or to `else`, which then needs to do the recursion. Our `else` branch
happens in three steps.

```clojure
; 1. Recur with `(- n 1)`.
{:name v10 :op :literal :value 1 :type "jank::runtime::obj::integer_ref"}
{:name v11 :op :cpp/call :value "jank::runtime::sub" :args [n v10] :type "jank::runtime::object_ref"}
{:name v12 :op :named-recursion :fn fibonacci :args [v11] :type "jank::runtime::object_ref"}

; 2. Recur with `(- n 2)`.
{:name v13 :op :literal :value 2 :type "jank::runtime::obj::integer_ref"}
{:name v14 :op :cpp/call :value "jank::runtime::sub" :args [n v13] :type "jank::runtime::object_ref"}
{:name v15 :op :named-recursion :fn fibonacci :args [v14] :type "jank::runtime::object_ref"}

; 3. Return the sum of those.
{:name v16 :op :cpp/call :value "jank::runtime::add" :args [v12 v15] :type "jank::runtime::object_ref"}
{:name v17 :op :ret :value v16 :type "jank::runtime::object_ref"}
```

So let's eliminate those extra `:cpp/into-object` and `:truthy` instructions and
add support to our IR generation for just using `bool` values directly. The
results are that we drop from 2,309 milliseconds to 2,247 milliseconds. That's
quite marginal, given our overall magnitude. It's nice that the IR no longer has
any extraneous instructions in it, though.

```clojure
{:name entry
 :instructions [{:name fibonacci :op :parameter :type "jank::runtime::object_ref"}
                {:name n :op :parameter :type "jank::runtime::object_ref"}
                {:name v3 :op :literal :value 1 :type "jank::runtime::obj::integer_ref"}
                {:name v4 :op :cpp/call :value "jank::runtime::lte" :args [n v3] :type "bool"}
                {:name v6 :op :branch :condition v4 :then if0 :else else1 :merge nil :shadow nil :type "void"}]}
{:name if0
 :instructions [{:name v7 :op :ret :value n :type "jank::runtime::object_ref"}]}
{:name else1
 :instructions [{:name v8 :op :literal :value 1 :type "jank::runtime::obj::integer_ref"}
                {:name v9 :op :cpp/call :value "jank::runtime::sub" :args [n v8] :type "jank::runtime::object_ref"}
                {:name v10 :op :named-recursion :fn fibonacci :args [v9] :type "jank::runtime::object_ref"}
                {:name v11 :op :literal :value 2 :type "jank::runtime::obj::integer_ref"}
                {:name v12 :op :cpp/call :value "jank::runtime::sub" :args [n v11] :type "jank::runtime::object_ref"}
                {:name v13 :op :named-recursion :fn fibonacci :args [v12] :type "jank::runtime::object_ref"}
                {:name v14 :op :cpp/call :value "jank::runtime::add" :args [v10 v13] :type "jank::runtime::object_ref"}
                {:name v15 :op :ret :value v14 :type "jank::runtime::object_ref"}]}
```

<div class="small-figure">
  <figure>
    <img src="/img/blog/2026-05-08-optimization/jank-3.plot.svg"></img>
  </figure>
</div>

#### Optimizing `nil` usage
At this point, the IR looks good and I have no other planned optimizations, so
let's take a look at a flamegraph to see where our time is being spent. You can
click this image to open it in a new tab and see the details, if you'd like.
I'll cover the essentials here anyway.

<a target="blank_" href="/img/blog/2026-05-08-optimization/flame-1.svg">
  <div class="figure">
    <figure>
      <img width="100%" height="33%" src="/img/blog/2026-05-08-optimization/flame-1.svg"></img>
    </figure>
  </div>
</a>

We're expecting to see arithmetic, and calls to `fibonacci`, but curiously we
see a LOT of time spent in `jank_nil` and `jank_const_nil`. As a Clojure
dialect, we access `nil` very often, since we need to check if things are `nil`,
initialize things to `nil`, and lots of expressions evaluate to `nil`. However,
that shouldn't show up in the profiler! It does because jank currently puts its
`nil` value behind a function for some very in-the-weeds reasons. C++ doesn't
guarantee initialization order for globals across translation units and any
translation units jank AOT compiles will be full of globals (lifted constants)
which will want to initialize their values to be `nil`. If `nil` is defined as a
global in another translation unit, as part of the jank runtime, we might try to
use it before it's initialized. So we've been working around that by putting
`nil` behind a function called `jank_nil`, but apparently this has had a heavy
performance cost.

jank's boxed pointer type doesn't allow initialization with `nullptr`, since
that's not a valid value. jank separates `nil` from `nullptr` since
dereferencing `nil` is well-defined, in Clojure, but dereferencing `nullptr` is
undefined behavior in C++. We simply just can't do it. But I happen to know that
we don't care about the default construction of the globals we generate for AOT
compiled code, since we later re-initialize them when the module is loaded. So
let's add a custom constructor to our boxed pointer type which actually
initializes them to `nullptr` because we know we'll be re-initializing them with
`nil` later. Then we can just keep `jank_nil` as a global value
instead of a function.

This brings us from 2,247 milliseconds to 1,400 milliseconds!

<div class="small-figure">
  <figure>
    <img src="/img/blog/2026-05-08-optimization/jank-4.plot.svg"></img>
  </figure>
</div>

That's a big win! This qualifies as the runtime getting in the way of our
benchmark, so it's nice to clear that up. We're still over 5x slower than
Clojure JVM, though, so it's time to cut off some more huge chunks.

#### Why are `add`/`sub` slow?
If we look at the rest of the flamegraph embedded above, we see the expected
suspects. Namely, `add` and `sub`. The slowest parts of those functions,
the flamegraph tells us, is the GC allocation of new numbers. Every single
integer result from adding or subtracting is a new dynamic allocation. At this
point, we're spending basically our entire time just allocating numbers.

You might wonder why we're not using unboxed numbers for this. Or you might
think "Just add a type hint! Clojure supports those!". But that misses the point
of this benchmark. This benchmark is specifically written to challenge the
compiler and runtime. Let's take a look at our Clojure code one more time and
I'll explain why.

```clojure
(defn fibonacci [n]
  (if (<= n 1)
    n
    (+ (fibonacci (- n 1))
       (fibonacci (- n 2)))))
```

Here, the type of `n` is just going to be a type-erased object. In Clojure JVM,
it will be a Java `Object`. In jank, it's a `jank::runtime::object_ref`. Either
way, we have no idea what kind of object is in there. When we do `(- n 1)` and
`(- n 2)`, we still don't know the type of `n`. It could be a float. It could be
an integer. It could be a ratio. It could be a big decimal or big integer. It
could be something which doesn't even support arithmetic. So we need to do a
whole polymorphic dance to handle arithmetic on `n`. Fortunately, we know the
types of `1` and `2`, so we can optimize for that, but it's not enough to unbox
any of this arithmetic. The same thing applies to the `+` call. We don't know
the return type of `fibonacci`. We either return `n`, which we don't know the
type of, or we return the result of `+` with both inputs being the return from
`fibonacci`, which we still don't know the type of. So there's nothing we can do
here statically either. This is the key aspect that makes this benchmark
difficult. The JVM is doing a much better job of it than we are, currently.

#### Pointer tagging
To remedy this, let's just avoid dynamic allocations for integers entirely.
There are some well-known tricks used by language runtimes to do exactly that
and jank isn't yet using any of them. We'll start with the simplest trick and
then build to a more complex design in a later benchmark post.

Did you know that, on 64 bit sytems, the bottom three bits of pointers are
effectively unused? This is because pointers are aligned to 64 bit machine
words. For example, an aligned pointer exists at address 0, 8, 16, 24, and so
on. But an aligned pointer will not exist at an address which is not divisible
by 8 bytes (64 bits). The bottom three bits of a pointer, 000, are for the 1's
place (lowest bit), 2's place (middle bit), and 4's place (highest bit). If all
bits are on, 111, we have the value 7 (4 + 2 + 1). If you add one more, we get 8
and all three lowest bits go back to 0.

This is an incredible piece of knowledge, since it means that we can embed extra
information in our pointers very easily. For example, if we set the lowest bit
to 1, we can convey that the pointer is not actually a pointer. Instead, it's an
encoded integer. We can then use the other 63 bits to store the actual integer
value. This way, as long as our integers don't need to store values higher than
what 63 bits can manage, we can store them inline without needing to do a
dynamic allocation! In the case where we need all 64 bits, we can just do an
allocation like we normally would and we'd get a normal pointer (lowest three
bits all 0) to a boxed integer.

To visualize this, a normal 64 bit pointer would look like this. The highest 61 bits
are used for pointer data and the lowest three bits are 0.

```
pppppppp pppppppp pppppppp pppppppp pppppppp pppppppp pppppppp ppppp000
```

An encoded integer would look like this. The lowest bit is 1 and the rest is
reserved for integer data. To get the actual 63 bit integer, we just shift the whole
thing to the right once.

```
xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxx1
```

For our fibonacci benchmark, and indeed for basically every benchmark and
real-world application, this is going to effectively eliminate every dynamic
allocation for integers. Let's see how it affects our numbers.

With tagged pointers to optimize for 63 bit integers, jank goes from 1,400
milliseconds to 282 milliseconds. As I said, we were basically spending all of
our time allocating integers. Even better, this puts us within reach of Clojure's
200 milliseconds.

<div class="small-figure">
  <figure>
    <img src="/img/blog/2026-05-08-optimization/jank-5.plot.svg"></img>
  </figure>
</div>

#### Intense inlining
Let's take a look at a new flamegraph with our latest changes and see how we can
trim off the extra fat. Again, you can click this image to open it in a new tab,
if you'd like. Or just take my word for it.

<a target="blank_" href="/img/blog/2026-05-08-optimization/flame-2.svg">
  <div class="figure">
    <figure>
      <img width="100%" height="33%" src="/img/blog/2026-05-08-optimization/flame-2.svg"></img>
    </figure>
  </div>
</a>

Ideally, what we see here is just `fibonacci` and nothing more, since everything
that matters should have been inlined. Instead, we see `jank::runtime::add`,
`jank::runtime::sub`, and `jank::runtime::lte`. This means those calls have not
been inlined by Clang. Let's try to tell Clang to always inline our arithmetic
functions. We can do this by putting some attributes on the C++ `add`, `sub`,
etc functions. Modern C++ syntax makes this easy.

```cpp
template <typename L, typename R>
[[gnu::always_inline, gnu::flatten, gnu::hot]]
auto add(L const l, R const r)
{ ... }
```

Arithmetic is something we always want to be fast, so there's no better thing to
inline than number crunching. Now we can try again. Fortunately, with a sigh of
relief, this drops jank from 282 milliseconds to 114 milliseconds. We're nearly
twice as fast as Clojure JVM! Better yet, we're gone from taking 5,522
milliseconds to taking 114 milliseconds and we're effectively doing the same work.

<div class="small-figure">
  <figure>
    <img src="/img/blog/2026-05-08-optimization/jank-6.plot.svg"></img>
  </figure>
</div>

I once saw an interview with an artist who does chainsaw sculptures. The
interviewer asked him how he approaches sculpting something like a bear out of a
huge log. He said "I just remove everything that doesn't look like a bear".
While that's not a particularly helpful answer, optimization is quite similar.
When we're profiling and taking a look at how the time is being spent, we just
need to remove all of the time spent NOT doing the most essential tasks.
Generally, that just means figuring out what the essential tasks are and then
figuring out how to not do everything else.

## What's next
One benchmark down, many more to go. Next, I will be revisiting a ray tracer I
wrote in Clojure a couple of years ago and we will utilize our new IR and
optimized runtime to see how fast we can push jank. This post, and this first
benchmark, is just the start.

## A note about the JVM versus native
Many folks who use jank for the first time end up saying something along the
lines of "Why is jank slow? Isn't it written in C++?" So, firstly, jank is
slow because it's unoptimized. As we can see here, jank can absolutely compete
with the JVM in this micro-benchmark and I will show in future posts that we can
do so in larger benchmarks, too. Secondly, you know what's also written in C++?
The JVM. jank is indeed a miniature JVM, with some key distinctions.

The JVM has not only a just-in-time (JIT) compiler, but also a JIT optimizer.
The JVM will adaptively perform inter-procedural optimizations, at run-time,
based on which functions are called, how often, and with which values. This is
an incredible piece of engineering.

In the native world, we don't currently have JIT optimization. It could exist,
but LLVM doesn't have any implementation for it and neither does any major C or
C++ compiler. Furthermore, the entire native ecosystem is not designed for it,
whereas it's truly taken for granted in the JVM space. This means that JVM
programs get faster and faster, the more you use them. However, if jank is faster than
Clojure, **it's because it started that fast and stayed that fast**.

Finally, just because jank is written in C++ doesn't mean that we can escape
Clojure's semantics. Clojure is dynamically typed, garbage collected, and
polymorphic as all get out. Regardless of the language used for the runtime, all
of these semantics need to be preserved for a true Clojure dialect. If we were
to rewrite this benchmark in actual idiomatic C++, there would be no contest
between it and Clojure. C++ would just win, since it's statically typed, uses
primitive arithmetic, has almost no runtime, and Clang, in particular, has
world-class AOT optimization. This applies even if you type-hint the Clojure
code. Try it, if you don't believe me. I did, when writing this. :)

## Would you like to join in?
1. Join the community on [Slack](https://clojurians.slack.com/archives/C03SRH97FDK) or [Discord](https://discord.gg/7sSMfKDBU3)
2. Join the design discussions or pick up a ticket on [GitHub](https://github.com/jank-lang/jank)
3. Considering becoming a [Sponsor](https://github.com/sponsors/jeaye) <span class="icon mr-1" style="color: rgb(201, 97, 152);"> <i class="gg-heart"></i></span>
4. **Hire me full-time to work on jank!**
