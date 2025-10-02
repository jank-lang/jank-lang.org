Title: The jank community has stepped up!
Date: Oct 03, 2025
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Description: Clojure, LLVM, and C++ walk into a bar. jank is born. Don't think about it too much.
Draft: true

The past two months have been incredibly busy and the jank community has been
playing and ever larger role in moving jank toward its alpha release. I want to
cover what I've been working on, but my main goal here is to shine the spotlight
on everyone else who has pitched in recently. Before that, thank you so much to
all of my Github sponsors, as well as Clojurists Together for sponsoring this
quarter of jank's development! You folks are the best.

## C++ interop stability
My main focus for the past several weeks has been identifying bumpy areas in
jank's C++ interop so they can be smoothed out. This includes issues with
analysis and LLVM IR generation mostly, but I also continue to expand upon our
[fork](https://gist.github.com/jeaye/f6517e52f1b2331d294caed70119f15f) of the
Compiler Research Group's CppInterOp library, which serves as a middleman
between jank and Clang's AST.

More quantifiably, jank now has more robust handling for C++ enums, pointers to
arrays, pointers to functions, implicit conversions, non-type template
parameters, lambda captures, initial array support via `cpp/aget`, and syntax error
detection within `cpp/value` and `cpp/type`. Furthermore, jank's interop support
is now robust enough to handle all of jank's `clojure.core` functions (which do
interop). This has allowed us to rip out some of the old code which set up those
functions from C++ instead.

## Portability and distribution
Aside from robust C++ interop, my focus these past two months has been on making
jank easy to build and install on major Linux distros and macOS. The main
announcement here is that jank now has an Ubuntu PPA which is hosting jank
binaries compatible with Ubuntu 24.04 and 24.10. These binaries are built
continuously. If you'd like to try out the latest version of jank on Ubuntu, you
can look at the installation docs
[here](https://github.com/jank-lang/jank/blob/main/compiler+runtime/doc/install.md#ubuntu-linux-2404-2410).

Additionally, we now continuously build jank using macOS, as well as Linux.
While jank already has a build-from-source Homebrew formula, in the coming
weeks, we'll also have a binary Homebrew formula so folks on macOS (aarch64) can
easily and quickly try out jank.

## Two-phase compiler building
I've added a two phase setup for the jank compiler so that phase 1 builds a jank
binary which is capable of compiling `clojure.core` and other core Clojure
libraries from jank source (with C++ interop). Then, phase 2 builds a jank
binary which has all of those sources linked in as well. This results in our
final jank binary already having all of the core libraries compiled into the
executable, which makes startup more efficient since they don't all need to be
JIT loaded. Users who previously tried to install a binary version of jank and
found that it took a long time to start up will now find that it's several times
faster, since it's not processing `clojure.core` and others every time it
starts.

## Community efforts
The community has been incredibly helpful the past couple of months, both in
trying out jank and reporting interop issues and in actually submitting
improvements to the code. These months have unsurprisingly been the most active
for support requests, questions, and suggestions.

### nREPL server + imgui wrapper
[Kyle Cesare](https://github.com/kylc) dropped a few surprises into jank's [Slack channel](https://clojurians.slack.com/archives/C03SRH97FDK)
recently. Firstly, he demonstrated using jank with glfw, OpenGL, and imgui, as
shown [here](https://github.com/kylc/jank-imgui). This was impressive, to say
the least, and it was the first time someone had really used jank's C++ interop
to build something remotely practical.

Well, a couple weeks later, Kyle took that further and dropped another surprise
message in Slack, showing that he had implemented a working nREPL server,
written in jank. Combining that nREPL server with his previous imgui work, he
demonstrated a video showing interactive GUI dev in an OpenGL window, right from
Emacs. I've included that video below.

<p align="center">
  <video controls src="/video/blog/2025-10-03-community/nrepl+imgui.mp4" type="video/mp4"></video>
</p>

We can definitely make a flashier demo, but this is hot off the press, in
pre-alpha jank. Yet people are making it work!

### Interop improvements
The community has also been helping with some smaller C++ interop changes. [Chris Badahdah](https://github.com/djblue)
has been helping us port the rest of jank's `clojure.core` to use C++ interop
wherever possible. He's also implemented `#cpp` for string literals, which
provides a C string rather than a jank object. This alleviates the need for casting
or other heavier implicit conversions which jank can do. Let's compare a couple
examples.

```clojure
(let [u (cpp/getenv "USER")]
  )
```

This jank code is calling the C `getenv` function, which takes a C string as an
argument. When we pass `"USER"`, we're passing a jank object which is a
`persistent_string`. During analysis, jank will see that the function
expects a `char const*` and will generate code to use a builtin conversion from
jank object to `char const*`, which involves checking if the object is a
`persistent_string` and then pulling out the underlying data. The optimized IR for this
looks like so:

```llvm
define ptr @clojure_core_let_24071_0(ptr %this) {
entry:
  ; Convert the jank object into a C string.
  %0 = load ptr, ptr @string_671781537, align 8
  %1 = call ptr @_ZN4jank7runtime7convertIPKcE11from_objectENS0_4orefINS0_6objectEEE(ptr %0)
  ; Call `getenv` with the C string.
  %2 = call ptr @getenv(ptr %1)
  ; Return nil.
  %3 = load ptr, ptr @nil, align 8
  ret ptr %3
}
```

Now, if we instead use the new `#cpp` tag, we can skip the whole `from_object`
conversion and use the C string directly.

```clojure
(let [u (cpp/getenv #cpp "USER")]
  )
```

The IR is now much more efficient and there's no need to allocate the `"USER"`
jank object.

```llvm
define ptr @clojure_core_let_24072_0(ptr %this) {
entry:
  ; Call `getenv`, using the C string constant directly.
  %0 = call ptr @getenv(ptr @.str.3)
  ; Return nil.
  %1 = load ptr, ptr @nil, align 8
  ret ptr %1
}
```

The `#cpp` prefix for strings operates not only as an optimization, but also as
an explicit type hint, which can allow you to easily select the correct C++
overload you want. The jank compiler will know that `#cpp "USER"` has the type
`char const[5]` (the last byte is a null terminator) and it will act accordingly.

We'll be adding `#cpp` support for numbers as well, so we can avoid conversions
and just put the literals right into the IR when possible. Ideally, going
forward, jank can do more of this automatically, when it's obvious that the
object is only being used for interop. Even still, providing the option for
users to explicitly say what they want is a good thing.

## Interlude
Please consider subscribing to jank's mailing list. This is going to be the best way to
make sure you stay up to date with jank's releases, jank-related talks,
workshops, and so on.

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

### New jank objects
[Jianling Zhong](https://github.com/jianlingzhong) and Chris Badahdah recently merged in support for big
decimals, which follows Jianling's work on adding big integers before that.
Chris has also added support for regex, UUID, and instant objects, as well as
their corresponding `#".*"`, `#uuid`, and `#inst` reader support. Chris has been
on a roll, submitting dozens of fixes and improvements to jank's core libraries.

### AOT compilation + linking of executables
jank has had AOT compilation of specific modules for a while. This basically
allows us to compile a `.jank` file to a `.o` file. jank's module loader
will then search for all suitable files on the module path (like Clojure's
class path) and will load binary files instead of source files if they're recent enough.
However, [Saket Patel](https://github.com/Samy-33) has extended this behavior to allow jank to link
together all of a project's `.o` files, along with a tailored entry point, so we
can get a working executable. Let's check out an example of this. We can build
on the previous `getenv` usage to build our own `whoami`.

```bash
❯ mkdir whoami
mkdir: created directory 'whoami'

❯ cat << EOF > whoami/main.jank
(ns main)

(defn -main [& args]
  (println (cpp/getenv #cpp "USER")))
EOF

❯ jank --module-path whoami compile main -o build/whoami

❯ ./build/whoami
jeaye
```

This is perhaps the most overkill `whoami` you've ever seen. On top of calling
`getenv`, it also links in the entire jank compiler/runtime, as well as
Clang/LLVM. This is because the program still has full interactive capabilities,
so you can still REPL into it, redefine anything, and grow it by adding more
code on the fly. This is basically the same as a Clojure uberjar.

Going forward, jank will also support the option to build AOT binaries without
all of that interactive functionality. Those binaries will be much lighter and
they won't depend on jank or Clang/LLVM for distribution. This approach will
basically be the same as a Graal native image, but even lighter.

### AUR packages: `jank-git` and `jank-bin`
[Flinner Yuu](https://github.com/Flinner) has authored AUR packages for jank, so users can now either
build from source (`jank-git`) or use the latest binaries (`jank-bin`). The
binary package is built from our continuous Ubuntu PPA and patched to work
cleanly on Arch Linux. You can find the docs for how to try out jank on Arch
Linux
[here](https://github.com/jank-lang/jank/blob/main/compiler+runtime/doc/install.md#arch-linux-aur).

### Nix package
jank isn't yet in nixpkgs, but we do have a much more robust Nix setup,
including continuous builds and cachix support, thanks to Kyle Cesare. This
means that Nix users can install binary versions of jank as well, simply by
enabling our cache. You can find the details
[here](https://github.com/jank-lang/jank/blob/main/compiler+runtime/doc/install.md#nix).

### clojure-test-suite
As part of testing jank, we have been writing a suite of unit tests for
`clojure.core`, `clojure.string`, and so on. Thanks to recent work by [David Miller](https://github.com/dmiller) (Clojure CLR)
and [borkdude](https://github.com/borkdude) (bb), this test suite is now continuously
testing Clojure JVM, ClojureScript, Clojure CLR, and babashka.

On top of that, [Dave Liepmann](https://github.com/daveliepmann) and
[Emma Griffin](https://github.com/E-A-Griffin), among others, have been chipping away at filling in
the test suite. We need more help on this, since we're only 27% done! Anyone who
knows Clojure can help, just check out the guide
[here](https://github.com/jank-lang/clojure-test-suite/issues/1).

## What's next?
There's plenty more work to do for smoothing out the C++ interop, but I will
also be implementing C++ destructor support for local values, adding a binary
Homebrew formula, fixing some GC issues jank has, improving LLVM IR generation, and
in general getting jank ready for the **alpha release in December**.

I hope that the jank community keeps up the incredible work of tackling
everything else I can't get my hands on. The momentum gain recently has been
amazing and I'm so appreciative of all the help.

## Would you like to join in?
1. Join the community on [Slack](https://clojurians.slack.com/archives/C03SRH97FDK)
2. Join the design discussions or pick up a ticket on [GitHub](https://github.com/jank-lang/jank)
3. Considering becoming a [Sponsor](https://github.com/sponsors/jeaye) <span class="icon mr-1" style="color: rgb(201, 97, 152);"> <i class="gg-heart"></i></span>
4. **Hire me full-time to work on jank!**
