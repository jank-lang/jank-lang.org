Title: jank reimagines C++ errors and gets an official native package repo
Date: Sep 04, 2026
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Description: jank's errors are slick, but did you know you can start a new
             raylib app in jank with one command?

Welcome back! It's been three months since my last post and the amount of
improvements to jank are staggering. Let me tell you all about it! Before
jumping into the details, though, I want to say thank you to my
[Github sponsors](https://github.com/sponsors/jeaye) and to Clojurists Together for
sponsoring me this whole year. Thank you!

# Error reporting
Last year, I [published a post](/blog/2025-03-28-error-reporting) showcasing how
jank has reimagined Clojure errors. In every way, I aim for jank compiler errors
to be more helpful, provide more context, and be more visually appealing than
the Clojure JVM counterpart. Even in complex scenarios involving macros, jank
excels.

<div class="wide-figure">
  <figure>
    <img src="/img/blog/2025-03-28-error-reporting/jank-macro-def-2.png"></img>
  </figure>
</div>

However, I hadn't yet tackled a couple of other areas of error reporting:
runtime exceptions and C++ interop errors. 

## Uncaught runtime exceptions
In today's modern C++ compilers, there's no standard, portable way to get a
stack trace. Coming from the JVM, this may sound surprising, but it's par for
course in the native world. Even worse, jank is JIT compiling C++ code and we
want to get accurate stack traces which include those frames as well. Even
worse, we need to map some of that C++ back to actual jank code. So, in order to
get beautiful, accurate stack traces for jank's uncaught runtime exceptions,
there was a lot of work to be done. Check out the results!

<div class="wide-figure">
  <figure>
    <img src="/img/blog/2026-09-04-better-and-better/stacktrace.png"></img>
  </figure>
</div>

As you can see in the figure above, an exception was thrown from the C++ code backing
`clojure.core/subs`. jank properly reports the error by pointing at the nearest
user's Clojure call, skipping over the one in `clojure.core` which calls the C++
function. In the stack trace, we can see two Clojure-specific frames, numbered
as #1 and #3. First, we see the frame for `clojure.core/subs`. Then we see the
frame for `user/foo`, which actually does the call to `subs`. Note that both of
these frames include the exact arity that was used, as well as the precise
source location in their respective jank files.

What you're not seeing here is that this stack trace is pulling debug info from
three separate places:

1. The current executable, for all of the non-Clojure frames.
2. An AOT-compiled object file, which was loaded when `clojure.core` was required. This is equivalent to Clojure JVM's `.class` files.
3. A JIT-compiled object file, which was added to the LLVM JIT runtime when the
   `user/foo` function was compiled.

After my recent efforts, jank now weaves all of these together seamlessly to
provide you a lovely error report. This works reliably on macOS and on Linux.

## Error pages
Building on the error output above, you may also notice the URL that's tucked
into the bottom of the code snippet. Since the original error reporting design
last year, I have intended for jank to have a dedicated error page for each
error. Each page should provide more information about the error, common causes,
and suggested fixes. All of these pages have now been created and are part of
the jank book. Some of them are more bare-bones than others and I plan to
continue filling them in over time. Getting them created sooner will start
aiding in SEO, though, which will help ensure that if you search for any jank
errors you hit, the right resources will be shown to you.

Here's an example of what I have in mind:
[analyze/invalid-cpp-conversion](https://book.jank-lang.org/reference/error/analyze/invalid-cpp-conversion.html)

## C++ candidates
I have saved the best for last, as far as error messages go. We know that
Clojure is infamous for its error messages and I hope to have shown how jank
addresses that. However, C++ is also infamous for its error messages and jank
is just as much C++ as it is Clojure. C++ is a much scarier beast when it comes
to all of the possible things that can go wrong, though. So how can we reimagine
C++ error messages? Well, I gave it my best shot. Take a look. :)

<div class="wide-figure">
  <figure>
    <img src="/img/blog/2026-09-04-better-and-better/implicit-ambiguous.png"></img>
  </figure>
</div>

The call is ambiguous because the second argument is an `int`, which directly
matches neither `long` nor `short` but can implicitly be converted to either of
them. jank's AST is intertwined with Clang's AST, so we can extract all of the
necessary information to render this neatly. Unlike Clang, or GCC, jank renders
these in a table format which I find to be incredibly succinct and appealing.

Also, as a bonus, the signature and source information for these `bar` functions
is correct, even when they're declared inside of a `cpp/raw` in a jank file.
Let's take a look at another one.

<div class="wide-figure">
  <figure>
    <img src="/img/blog/2026-09-04-better-and-better/rank.png"></img>
  </figure>
</div>

When there are many candidates to report, jank optimizes useful output by
ranking the candidates based on argument count, required conversions, as well as
access levels. By default, jank will only show the top three candidates.

Finally, I'll show one more image, which is of a special kind of ambiguity with
some jank-specific behavior. On top of normal C++ overloading, implicit
conversions, etc, jank also supports automatic trait conversions, which use a
well-known trait to convert to/from jank objects and native values. If an
argument to a native function is a jank object, the compiler will consider
whether or not a trait conversion can be used. However, this can result in
ambiguities, too, if multiple candidates are viable. Here's an example.

<div class="wide-figure">
  <figure>
    <img src="/img/blog/2026-09-04-better-and-better/trait-ambiguous.png"></img>
  </figure>
</div>

There's a lot more that jank can already do with these C++ candidate failures,
but I can only show so much in a blog post. I'm sure you'll see more next time
you're writing some jank code!

## Why bother with all of this?
You may not be as excited as I am about these images of error reports. That's
understandable. It's partly a compiler nerd thing, since effective error
reporting can be quite tricky. However, it's also partly a huge usability win
over not only Clojure JVM, not only Clang and GCC, but also the status quo in a
lot of developer tooling. I am trying to build a language, and tooling
ecosystem, that is the best it can be. That's the language I want to use. I want
to entice others to try it by dedicating this time to usability, too.

To me, this is incredibly important.

# Interlude
Before we move on to the next topic, please consider subscribing to jank's
mailing list. This is a great way to make sure you stay up to date with jank's
releases, jank-related talks, workshops, and so on. It's very low traffic.

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

Also, while we're in this interlude, are you sponsoring me? If so, you're the
best! Thank you. If not, did you know I've been working on jank full-time for nearly two
years, relying on Github sponsors and Clojurists Together grants as my sole
income? If you're able to chip in $10 per month, [please do](https://github.com/sponsors/jeaye)!

# Native build system
Another large system I've been working on is jank's native build system. This is
a Cargo-like build system, for those familiar with Rust. The goal of the build
system is to enable easy consumption of native libraries, both from the
installed system and from compiled sources. The jank build system stands on top
of Clojure's existing package management, namely through Clojars. When you add a
dependency, the jank tooling will automatically pick up if that dependency has a
native jank build script and will build the package locally. These scripts are
always run in a sandbox which has no access to your personal files. This is an
improvement over the default Cargo machinery.

This build system was originally created by Kyle Cesare and I've been further
improving it these past few months by adding sandboxing support to macOS,
improving static linking support, and overall making things more robust and
stable. Now that we have a powerful native build system, what we need is a
repository of high quality packages. That is precisely why I started the jank
commons.

# jank commons
The [jank commons](https://github.com/jank-lang/commons) is an official
repository of native jank packages published to Clojars. Each of these packages
integrates seamlessly into the jank build system and has an example project
which is continuously compiled. Following Rust/Cargo's naming scheme, the jank
commons is currently full of `foo-sys` packages. The `-sys` suffix conveys that
it's a package which provides a system library without providing a higher level
API. Writing a higher level API is left up to other packages which then depend
on the `-sys` packages. The key benefit here is that the higher level packages
don't need to bother with all of the system details of packaging native libs and
can just focus on writing good APIs. Another benefit is that generally only
`-sys` packages will need native build scripts, so isolating those can further
help with security. Even better, since jank has seamless C++ interop, idiomatic
Clojure APIs are optional.

An even easier way to browse the native packages jank has is through the
[awesome-jank](https://github.com/jank-lang/awesome-jank) list. This is mainly
populated by the jank commons right now, but please take this to be a call to
action to get more native libs packaged for jank! The jank commons README has a
guide for exactly how to do this and the whole native build system is thoroughly
documented in the [jank book](https://book.jank-lang.org/jank-build/overview.html).

# Everything else
There's a lot more that's been going on in the jank repos, but it's too much to
cover in detail here. For example:

* The [terminal REPL client has been rewritten](https://github.com/jank-lang/jank/pull/949) to support syntax highlighting,
  tab completion, and multi-line inputs
* Syntax quote expansion has been [drastically optimized](https://github.com/jank-lang/jank/pull/966), cutting generated code
  size from macros down substantially
* [Multiple](https://github.com/jank-lang/jank/pull/954) Clang/LLVM [bugs](https://github.com/llvm/llvm-project/pull/217781) have been fixed and upstreamed for LLVM 23, resulting
  in [more graceful jank updates](https://github.com/jank-lang/jank/pull/954) and more robust exception handling on macOS
* Support for throwing (and re-throwing) native values [has been added](https://github.com/jank-lang/jank/pull/943)
* The nREPL server received some [usability improvements](https://github.com/jank-lang/jank/pull/951)

## What's next
This is the last post before my talk at [Clojure Conj 2026](https://2026.clojure-conj.org/schedule).
If you can't make it, **check out the free live stream**! In the coming weeks, I will be
racing to improve jank's stability, portability, and usability, leading up to
the Conj. After the Conj, and for the remainder of the year, I'll be focused on
much of the same.

It's my goal to get jank into your hands, dear reader. For many of you, I think
that jank is already capable enough for you to begin your tinkering. With the
addition of the jank commons, starting a new raylib game in jank is as easy as
`lein run`. Getting a distributable binary is as easy as `lein compile`. What
follows is just polishing up all of the rough edges so that developing your
games and applications is a breeze.

If you've been waiting to try jank, give it a go! If there's something you need
which jank is missing, let me know! I'll make sure it's noted down and
prioritized.

I'll see you all at the Conj.

## Would you like to join in?
1. Join the community on [Slack](https://clojurians.net)
2. Join the design discussions or pick up a ticket on [GitHub](https://github.com/jank-lang/jank)
3. Considering becoming a [Sponsor](https://github.com/sponsors/jeaye) <span class="icon mr-1" style="color: rgb(201, 97, 152);"> <i class="gg-heart"></i></span>
4. **Hire me full-time to work on jank!**
