Title: jank development update - Module loading
Date: Oct 14, 2023
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Description: An update on the past couple of months of building out jank's
             module loading system to meet parity with Clojure JVM.

For the past month and a half, I've been building out jank's support for
`clojure.core/require`, including everything from class path handling to
compiling jank files to intermediate code written to the filesystem. This is a
half-way report for the quarter. As a warm note, my work on jank this quarter is being sponsored
by [Clojurists Together](https://www.clojuriststogether.org/).

## High level requirements
### Class paths
Clojure JVM benefits a great deal from being built upon an existing VM. In the
native world, we don't have things like class paths. Maybe the closest things
would be include paths at compile-time and `LD_LIBRARY_PATH` at run-time, but
neither of those capture the flexibility of JVM's class paths, which work at both
compile-time and run-time.

So, to start with, jank needs a similar system. This is a common pattern for
jank, requiring me to implement not just Clojure, but a VM of my own, with the
necessary parts to reach parity.

#### Progress
I've built out class path traversing for jank, which supports both directories
and JAR files. This will allow jank to work out of the box with Clojure's
existing Maven dependencies and file structures, which is of course important.

jank traverses the class path exhaustively on startup and caches what it finds,
mapping the module name (ns name or ns name with a nested class, like
`clojure.core$foo`) to the relevant file. When a function like `require` or
`compile` is called, jank will find the most relevant source to work with.

### Core functions
There are a handful of related `clojure.core` functions for module loading, like
`require`, `compile`, `load-libs`, `load-lib`, `load-one`, `load-all`, `alias`, etc. The
next step, after having class path support, is to implement these.

#### Progress
I have a working implementation of `(require 'clojure.core)` and `(compile 'clojure.core)` now!
They hook into the class path work and do the necessary work to require or
compile. Compilation writes files to a particular directory, which is also in
the class path. Requiring a module which is already loaded will not do anything.

There's still a lot of work to do to build out the necessary core functions and
have them work the same as in Clojure JVM. The implementations of `require` and
`compile` that I have right now only accept a single symbol, rather than
being variadic, supporting lib specs, flags, etc. So this is still an MVP, right
now, but it works!

### Class files
There's no such thing as a class file in the native world. Maybe the closest
equivalent would be an object file or, for C++20, a pre-compiled module. Those
are both more limiting than a class file, though, since they're not portable;
compiled native code is generally targeting a specific platform/architecture.
Trying to share these in a Maven dependency, for example, is only going to help
those who are on the same hardware as you. Even then, we can run into ABI
incompatibilities.

So, while I'm interested in exploring support for intermediate object files and
pre-compiled modules, I'm starting with intermediate files being just C++ source
(which is what the jank compiler outputs for Cling to JIT compile). From there,
another step toward machine code will be to target LLVM IR by having Clang
compile the C++ source first. This is closer to JVM byte code, but LLVM IR is
actually still platform/architecture specific!

Lastly, I'm very hesitant to provide a default of jank dependencies coming in as
binary files, even if I can solve the portability problem, simply due to supply
chain security concerns. I would rather live in a world where people share
source dependencies with pinned versions and transparent updates. I do think
that binary intermediate files make sense for local development, though, and
they can greatly speed up iteration.

#### Progress
As of now, I have `(compile 'clojure.core)` compiling jank source to C++ source,
which is being written to the class path. If you then later try to
`(require 'clojure.core)`, it will be loaded from the compiled C++ source.
If the C++ source was on the class path already, it will be favored over the
jank source.

One benefit of this implementation is that jank developers can include
arbitrary C++ source along with their jank source and just `require` it
alongside everything else. In order to work with this, the C++ source just needs
to follow a particular interface.

A challenge I ran into with this is how to manage module dependencies. For
example, if `clojure.core` depends on `clojure.core$take`, which depends on a
local fn its own, `clojure.core$take$fn_478`, I need to ensure that all of these
are loaded in order of deepest dependency (leaf node) first. I went back on forth
on the design for this, but ultimately settled on something *similar* to what
Clojure does. I generate two C++ source modules for `clojure.core` itself. One
is something like `classes/clojure.core.cpp` and the other is a special
`classes/clojure.core__init.cpp`. When `clojure.core` is required, it will look
for a `clojure.core__init` module first. Within that module is a special
interface with an `__init` function which has a big list of all of the
dependencies needed to actually load `clojure.core`. The `__init` function will
just iterate through that list and load each one. Finally, we can actually load
`clojure.core`, which runs the top-level effects of creating all of the vars,
the value for each being based on new types brought in from the dependencies.

This is different from Clojure, since the JVM has a standard way for one module
to depend on another. That dependency is just conveyed, like using `import` in Java,
and then the JVM ensures all dependencies are met before getting to the body of
the module. Again, I need to reimplement that portion of the JVM for jank since
the native world has no equivalent feature.

### What's remaining
Iterative compilation (tracking source timestamps to know how much to
recompile) and support for reloading have not been touched yet. Aside from that,
most things I have implemented are quite rough and need further polish to meet
parity with Clojure. Although I have `require` and `compile` working in the
simple case, none of the other related core functions have been implemented.

### Performance wins so far
By pre-compiling `clojure.core` to C++ source, and then just requiring it on startup, the
time it takes to boot the jank compiler + runtime and print hello world dropped
from 8.7 seconds to 3.7 seconds. So that was all time spent compiling jank code
to C++ code. What remains is almost entirely just time compiling C++. If I
remove `clojure.core` loading altogether, it takes less than 0.2 seconds to run
the same program. I'll be digging more into the performance here, as I get more
implemented, but I want to call out a couple of things.

1. We've already cut 5 seconds down, which is great!
2. Everyone knows that compiling C++ is *not fast* and we are set up to be able
   to start loading LLVM IR instead, after some more work
3. The creator of Cling informed me that LLVM tends to spend around 50% of its
   time in the front-end for C++, which means that by using LLVM IR we'll be
   cutting down our compilation time by around 50%
4. I haven't done any startup time benchmarking or profiling for jank yet, but if
   there's time this quarter, you can bet that I'll be digging deep into this

I have some exciting plans for visualizing jank's performance, both the compiler
and your application code, in a way which will ship with the compiler itself.
More info on this in a later post.

## Thanks again
As a reminder, my work on jank this quarter is sponsored by [Clojurists Together](https://www.clojuriststogether.org/).
Thank you to all of the members there who chose jank for this quarter. Thanks,
also, to all of my [Github sponsors](https://github.com/sponsors/jeaye). Your
continued support fuels jank's continued development!

## Would you like to join in?
1. Join the community on [Slack](https://clojurians.slack.com/archives/C03SRH97FDK)
2. Join the design discussions or pick up a ticket on [GitHub](https://github.com/jank-lang/jank)
3. Considering becoming a [Sponsor](https://github.com/sponsors/jeaye) <span class="icon mr-1" style="color: rgb(201, 97, 152);"> <i class="gg-heart"></i></span>
4. **Hire me full-time to work on jank!**
