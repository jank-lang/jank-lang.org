Title: jank development update - Lots of new changes
Date: Dec 08, 2022
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Description: A new development update for the jank programming language.


I was previously giving updates only in the
[#jank](https://clojurians.slack.com/archives/C03SRH97FDK) Slack channel, but
some of these are getting large enough to warrant more prose. Thus, happily, I
can announce that jank has a new blog and I have a _lot_ of new progress to
report! Let's get into the details.

Over the past couple of months, jank has gained initial C++ interop, an upgrade
of Cling to resolve Linux crashes, support for multiple fn arities, including
variadic arities, support for `if` and `let*`, which involved a rework of the
C++ codegen, and initial metadata support, and many smaller changes.

Before I get into the details, I'd like to point out that
**feedback is welcome on all of this**.
Very little of jank's design is set in stone right now, so that means your
feedback can matter a lot.

## Initial C++ interop
When digging deep enough into `clojure.core` implementations, most things
eventually just turn into Java. Crucial fns like `println`, for example, rely on
Java streams, as well as direct interop. Something like `str`, as well, reaches
right for `StringBuilder`. So jank needs a way to call into C++ to get the
important things done.

C++ is syntactically and semantically more complicated than Java. Its reflection
story is poorer and is made worse by `template` usage, which requires a C++
compiler to generate the necessary instantiations ahead of time. With all of
this in mind, I figured jank's interop is not going to look like Clojure JVM's,
or ClojureScript's. I did dive into what
[Ferret](https://ferret-lang.org/#outline-container-sec-4) and
[Carp](https://github.com/carp-lang/Carp/blob/master/docs/CInterop.md) are doing
for this and came up with some [design notes](https://github.com/jank-lang/jank/blob/main/DESIGN.md#interop)
that may be of interest. The notes cover both approaches, some pros/cons, and
the rationale for jank's approach.

In short, jank is introducing a special `native/raw` form which accepts inline
C++ code. Within that code can be interpolated jank code, escaped using the `#{}` form.

### Example
```clojure
; No macros yet, so no defn or fn.
(def str
  (fn*
    ([]
     "")
    ; Unary case uses interop and interpolation to call
    ; the to_string fn on the object.
    ([o]
     (native/raw "__value = make_box(#{ o }->to_string());"))
    ; Variadic case could recurse, but we can iterate
    ; and avoid extra boxing of each intermediate.
    ([o & args]
     (native/raw "std::string ret(#{ o }->to_string().data);
                  auto const * const l(#{ args }->as_list());
                  for(auto const &elem : l->data)
                  { ret += elem->to_string().data; }
                  __value = make_box(ret);"))))

(assert (= "" (str)))
(assert (= "1" (str 1)))
(assert (= "12" (str 1 2)))
(assert (= ":foo:bar[2 3]" (str :foo :bar [2 3])))
```

This is clearly a low-level primitive for interop and I'm interested to see how
we can build on it. For now, it unlocks all the bits

## Cling upgrade to 0.9
A crash on Linux was requiring jank to use Cling 0.7, which meant using
Clang/LLVM 5. After dozens of hours of compiling Cling/Clang/LLVM different
ways, to troubleshoot, I found the [issue](https://github.com/root-project/cling/issues/470)!
Now jank's build system has been updated to build Cling 0.9 for both macOS and Linux.

What came out of this, as well, is that jank can now use a pre-compiled header
(PCH) for improving Cling startup times. That PCH is packaged with jank.
**This changed dropped the Cling startup time from 1.4 seconds to 265 milliseconds!**
More
similar improvements to come.

## Support for if/let*
For a while, jank's codegen (to C++) was benefiting from the fact that Clojure
is such an expression-based language. Code such as:

```clojure
(println (foo (bar)))
```

Could be generated as something like:

```cpp
println->call(foo->call(bar->call()));
```

But that breaks altogether with something like:

```clojure
(println (if (even? n)
           :foo
           :bar))
```

To address this, I've completely switched the codegen approach to be entirely
statement based. Every sub-expression is now pulled up into a statement with a
temporary and then each outer expression just refers to those symbols. So, the
corresponding C++ for the above Clojure would look something like:

```cpp
// This is cleaned up a bit, to make it easier
// to read, but the shape of the code is the same.
object_ptr if_result;
object_ptr even_result{ even_QMARK->call(n) };
if(jank::runtime::detail::truthy(even_result))
{ if_result = kw_foo; }
else
{ if_result = kw_bar; }
println->call(if_result);
```

The way this manifests in the compiler is really clean, since every jank
expression resolves to a single C++ symbol, which is its temporary. Could be a
`let*`, `if`, fn call, or anything else, but it's all boiled down to a symbol
for that value. I have more detailed [design notes](https://github.com/jank-lang/jank/blob/main/DESIGN.md#codegen)
on my approach, and Clojure's approach, in case you're interested.

## What's next
I'm working on macros next, since that will help clean up a lot of the jank code
I'm writing. After that, there are some more special forms which would really
help, such as `loop`. Finally, extending the runtime behaviors to add
analogous abstractions for functionality like `clojure.lang.Associative`, as well as better
`clojure.lang.Seqable` support within jank itself would mean I can start
implementing fns like `assoc`, `map`, `filter`, `reduce`, etc.

## How can we join in?
Glad you asked!

1. Join the community on [Slack](https://clojurians.slack.com/archives/C03SRH97FDK)
2. Join the design discussions on [Github](https://github.com/jank-lang/jank/discussions)
3. Considering becoming a [Sponsor](https://github.com/sponsors/jeaye)
