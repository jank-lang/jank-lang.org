Title: jank is off to a great start in 2026
Date: Mar 06, 2026
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Description: Learn about the jank alpha, LLVM bugs, nREPL support, the latest
             C++ interop enhancements, and what's upcoming for jank.

Hey folks! We're two months into the year and I'd like to cover all of the
progress that's been made on jank so far. Before I do that, I want to say thank you to
all of my Github sponsors, as well as Clojurists Together for sponsoring this
whole year of jank's development!

## jank book
To kick things off, let me introduce the [jank book](https://book.jank-lang.org/).
This will be the recommended and official place for people to learn jank and its
related tooling. It's currently targeted at existing Clojure devs, but that will
start to shift as jank matures and I begin to target existing native devs as well. The
jank book is written by me, not an LLM. If you spot any issues, or have any
feedback, please do create a [Github Discussion](https://github.com/jank-lang/jank/discussions).

My goals for this book include:

1. Introduce jank's syntax and semantics
2. Introduce jank's tooling
3. Walk through some small projects, start to finish
4. Demonstrate common use cases, such as importing native libs, shipping AOT artifacts, etc.
5. Show how to troubleshoot jank and its programs, as well as where to get help
6. Provide a reference for error messages

As the name and technology choice implies, the jank book is heavily inspired by
the [Rust book](https://doc.rust-lang.org/stable/book/).

## Alpha status
jank's switch to alpha in January was quiet. There were a few announcements made
by others, who saw the commits come through, or who found the jank book before I
started sharing it. However, I didn't make a big announcement myself since I
wanted to check off a few more boxes before getting the spotlight again. In
particular, I spent about six weeks, at the end of 2025 and into January, fixing
pre-mature garbage collection issues. These weeks will be seared into my memory
for all of my days, but the great news is that all of the issues have now been
fixed. jank is more and more stable every day, as each new issue improves our
test suite.

## LLVM 22
On the tail of the garbage collection marathon, the eagerly awaited LLVM 22
release happened. We had been waiting for LLVM 22 to ship for several months,
since it would be the first LLVM version which would have all of jank's required
changes upstreamed. The goal was that this would allow us to stop vendoring our
own Clang/LLVM with jank and instead rely on getting it from package managers.
This would make jank easier to distribute and, crucially, make jank-compiled AOT
programs easier to distribute. You can likely tell from my wording that this
isn't how things went. LLVM 22 arrived with a couple of issues.

Firstly, some data which we use for very important things like loading object
files, adding LLVM IR modules to the JIT runtime, interning symbols, etc was
changed to be private. This can happen because the C++ API for Clang/LLVM is not
considered a public API and thus is not given any stability guarantees. I have
been in discussions with both Clang and LLVM devs to address these issues. They
are aware of jank and want to support our use cases, but we will need to codify
some of our expectations in upstreamed Clang/LLVM tests so that they are less
likely to be broken in the future.

Secondly, upon upgrading to LLVM 22, I found two different performance
regressions which basically rendered debug builds of jank unusable on Linux
([here](https://github.com/llvm/llvm-project/issues/179611) and
[here](https://github.com/llvm/llvm-project/issues/182954)). Our startup time
for jank debug builds went from 1 second to 1 minute and 16 seconds. The way
jank works is quite unique. This is what allows us to achieve unprecedented C++
interop, but it also stresses Clang/LLVM in ways which are not always well
supported. I have been working with the relevant devs to get these issues fixed,
but the sad truth is that the fixes won't make it into LLVM 22. That means we'll
need to wait several more months for LLVM 23 before we can rely on distro
packages which don't have this issue.

That's a tough pill to swallow, so I took a week or so to
[rework](https://github.com/jank-lang/jank/pull/702) the way we do
codegen and JIT compilation. I've not only optimized our approach, but I've also
specifically crafted our codegen to avoid these slower parts of LLVM. This
not only brings us back to previous speeds, it makes jank faster than it was
before. Once LLVM 23 lands, the fixes for those issues will optimize things
further.

So, if you've been wondering why I've been quiet these past few months, I likely
had my head buried deep into one of these problems. However, with these issues
out of the way, let's cover all of the other cool stuff that's been implemented.

## nREPL server
jank has an nREPL server now! You can read about it in the relevant
[jank book chapter](https://book.jank-lang.org/getting-started/04-hello-nrepl.html).
One of the coolest parts of the nREPL server is that it's
[written in jank](https://github.com/jank-lang/jank/tree/16ace19a4dc771a540f86fbc4c1fbb3e0fae5fe8/compiler%2Bruntime/src/jank/jank/nrepl/server)
and yet also baked into jank, thanks to our two-phase build process. The nREPL
server has been tested with both NeoVim/Conjure and Emacs/CIDER. There's a lot
we can do to improve it, going forward, but **it works**.

As Clojure devs know, REPL-based development is revolutionary. To see jank's
seamless C++ interop combined with the tight iteration loop of nREPL is
beautiful. Here's a quote from an early jank nREPL adopter, Matthew Perry:

> The new nREPL is crazy fun to play around with. Works seamlessly with my
> editor (NeoVim + Conjure). It's hard to describe the experience of compiling
> C++ code interactively - I'm so used to long edit-compile-run loops and
> debuggers that it feels disorienting (in a good way!)

A huge shout out to Kyle Cesare, who originally wrote jank's nREPL server back
in August 2025. Thank you for your pioneering! If you're interested in helping
out in this space, there's still so much to explore, so jump on in.

## C++ interop improvements
Most of my other work on jank has been related to improving C++ interop.

### Referred globals
jank now allows for C/C++ includes to be a part of the `ns` macro. It also
follows ClojureScript's design for `:refer-global`, to bring native symbols into
the current namespace. Without this, the symbols can still be accessed via the
special `cpp/` namespace.

```clojure
(ns foo
  (:include "gl/gl.h") ; Multiple strings are supported here.
  (:refer-global :only [glClear GL_COLOR_BUFFER_BIT])) ; Also supports :rename.

(defn clear! []
  (glClear GL_COLOR_BUFFER_BIT))
```

## Native loop bindings
jank now supports native loop bindings. This allows for loop
bindings to be unboxed, arbitrary native values. jank will ensure that the
native value is copyable and supports `operator=`. This is great for looping
with C++ iterators, for example.

```clojure
(loop [i #cpp 0]
  (if (cpp/== #cpp 3 i)
    (cpp/++ i)
    (recur (cpp/++ i))))]
```

There's more work to be done to automatically use unboxed values and use native
operators, when possible. For now it's opt-in only.

## Unsafe casting
jank had the equivalent of C++'s `static_cast`, in the form of `cpp/cast`.
However, for some C/C++ APIs, unsafe casting is necessary. To accomplish this,
jank now has `cpp/unsafe-cast`, which does the equivalent of a C-style cast.

```clojure
(let [vga-memory (cpp/unsafe-cast (:* uint16_t) #cpp 0xB8000)]
  )
```

## Type/value DSL
This one is working, but not yet in `main`. jank now supports encoding C++ types
via a custom DSL. With this DSL, we can support any C++ type, regardless of how
complex. That includes templates, non-type template parameters, references,
pointers, const, volatile, signed, unsigned, long, short, pointers to members,
pointers to functions, and so on. The jank book will have a dedicated chapter on
this once merged, but here's a quick glimpse.

<table>
<tr>
<td> C++ </td> <td> jank </td>
</tr>

<tr>
<td>

A normal C++ map template instantiation.

```cpp
std::map<std::string, int*>
```

</td>
<td>

```clojure
(std.map std.string (ptr int))
```

</td>
</tr>

<tr>
<td>

A normal C++ array template instantiation.

```cpp
std::array<char, 64>::value_type
```

</td>
<td>

```clojure
(:member (std.array char 64) value_type)
```

</td>
</tr>

<tr>
<td>

A sized C-style array.

```cpp
unsigned char[1024]
```

</td>
<td>

```clojure
(:array (:unsigned char) 1024)
```

</td>
</tr>

<tr>
<td>

A reference to an unsized C-style array.

```cpp
unsigned char(&)[]
```

</td>
<td>

```clojure
(:& (:array (:unsigned char)))
```

</td>
</tr>

<tr>
<td>

A pointer to a C++ function.

```cpp
int (*)(std::string const &)
```

</td>
<td>

```clojure
(:* (:fn int [(:& (:const std.string))]))
```

</td>
</tr>

<tr>
<td>

A pointer to a C++ member function.

```cpp
int (Foo::*)(std::string const &)
```

</td>
<td>

```clojure
(:member* Foo (:fn int [(:& (:const std.string))]))
```

</td>
</tr>

<tr>
<td>

A pointer to a C++ member which is itself a pointer to a function.

```cpp
void (*Foo::*)()
```

</td>
<td>

```clojure
(:member* Foo (:* (:fn void [])))
```

</td>
</tr>

</table>

This type DSL will be enabled *automatically* in type position for `cpp/new`,
`cpp/cast`, `cpp/unsafe-cast`, `cpp/unbox`, and so on. It can also be explicitly
introduced via `cpp/type`, in case you want to use it in value position to
construct a type or access a nested value. For example, to dynamically allocate
a `std::map<int, float>`, you could do:

```clojure
(let [heap-allocated (cpp/new (std.map int float))
      stack-allocated ((cpp/type (std.map int float)))]
  )
```

## Interlude
Interested in jank? Please consider subscribing to jank's mailing list. This is
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

## Other improvements
jank will now defer JIT compilation of functions, when possible. In some
scenarios, such as during AOT compilation, this can cut compile times in half.
We do this by generating a stub object which will JIT compile the relevant code
when it's first called. It understands vars, too, so it will replace itself in
its containing var when called so that subsequent calls through the var just go
to the JIT compiled function. JVM folks happily don't need to worry about these
sorts of things, but we can have nice things, too.

Also, jank's object model has been opened up. I
[previously documented](https://jank-lang.org/blog/2023-07-08-object-model/) my research into an
efficient object model. Over the past couple of years of hammock time, I have
found an approach which allows for JIT-defined objects while still avoiding the
costs of C++'s runtime type information (RTTI). This is worthy of its own post
entirely, which I will likely do once the transition is complete. For now, we
have most of our code still using the old model while some of it is using the
new model. This is great, though, since it allows us to port piece by piece
while keeping everything in `main`. The main outcome of opening up the object
model is that jank users can define their own jank objects which integrate well
into the system, can be stored within jank data structures, and used with jank
functions.

Finally, to better support nREPL, jank added support for `clojure.core/future`.
This required an audit of all synchronization across the jank compiler and
runtime. Now, we should be in a good place from which to build multi-threaded
jank applications. Tools like Clang's thread sanitizer will help ensure we stay
there.

## Typed exceptions
Jianling Zhong merged his work to support typed C++ exceptions in jank.
This required several months of effort from him and it's hard to understate how
complex the mechanics for this are, at the LLVM IR level. However, the end
result is that now jank can catch C++ exceptions by type, polymorphically, and
in a way which matches C++'s own semantics. The syntax looks just how one would
expect from Clojure. It's worth noting that since C++ doesn't have a base
`Throwable` type, we need to handle all possible types. Whenever you throw a
jank object, it will be type-erased to a `jank::runtime::object_ref`. A normal
try/catch in jank will look like this:

```clojure
(try
  (throw :wow)
  (catch jank.runtime.object_ref e
    (println :caught e)))
```

But, if you're working with a C++ library, you can also catch their specific
exception types. Let's consider a more complete example:

```clojure

(ns file-size
  (:include "boost/filesystem.hpp")
  (:refer-global :only [boost.filesystem.file_size]
                 :rename {boost.filesystem.file_size file-size}))

(try
  (println :file-size (file-size #cpp "jank.json"))
  (catch boost.filesystem.filesystem_error e
    (println :caught (.what e))))
```

## What's next
In March, I am wrapping up work on the type DSL and getting that merged. I also
need to investigate why the Arch binary package for jank is broken. Beyond that,
I will be starting into some deep performance research for jank. That will mean
first collecting a series of benchmarks for jank versus Clojure and then profiling
and optimizing those benchmarks as needed. I would really like to get some
continuous benchmarking set up, so we can track performance over time, tied
to particular commits. The current plan is to spend all of Q2 on performance,
but there's a lot to do, so I won't be able to tackle everything. Benchmark
optimization posts are often quite fun, so stay tuned for the next one!

## Would you like to join in?
1. Join the community on [Slack](https://clojurians.slack.com/archives/C03SRH97FDK)
2. Join the design discussions or pick up a ticket on [GitHub](https://github.com/jank-lang/jank)
3. Considering becoming a [Sponsor](https://github.com/sponsors/jeaye) <span class="icon mr-1" style="color: rgb(201, 97, 152);"> <i class="gg-heart"></i></span>
4. **Hire me full-time to work on jank!**
