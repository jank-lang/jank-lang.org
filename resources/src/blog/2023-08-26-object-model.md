Title: jank development update - Object model results
Date: Aug 26, 2023
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Description: The results of a quarter focused on rebuilding jank's object model
             with the goal of being faster.
Image: https://jank-lang.org/img/logo-dark.png

As summer draws to a close, in the Pacific Northwest, so too does my term of
sponsored work focused on a faster object model for jank. Thanks so much to
[Clojurists Together](https://www.clojuriststogether.org/) for funding jank's
development. The past quarter has been quite successful and I'm excited to share the results.

If you haven't yet read my [previous post](/blog/2023-07-08-object-model), which goes over why I'm overhauling
jank's object model, and how I'm doing it, take a look! Without that, I suppose
you could still continue, if you enjoy looking at the results of unknown
problems. Just know the problem is interesting and the results are impressive.

## Overview of changes
These changes spanned almost the entire code base. I think only the lexer was
left unchanged, since it deals only with tokens and not runtime Clojure objects.
From the parser, through semantic analysis and JIT, and into every runtime
function, basically every operation on objects needed changes. I've made a pull
request on the jank repo so that these changes can be both quantified and
reviewed, by the daring reader:
[here](https://github.com/jank-lang/jank/pull/45).

Overall, it's currently at 8,634 added lines and 4,380 deleted lines, across 123
files. Indeed, the new object model lends itself to more code, and somewhat
longer compile times, but I think the results are worth it.

What follows is a lot of benchmarking graphs, each covering Clojure JVM, jank
prior to this quarter's work, and jank after all of this work. For all graphs, lower is better.

## Overall ray tracing speeds
The ray tracer used in the past couple of posts has been my primary benchmark
for the overall performance of the new object model, since it relies heavily on
maps, numbers, sequence traversal and strings (for output). I'm very pleased to
report that **jank is now nearly twice as fast at running the same ray tracing code as Clojure JVM**,
with jank clocking in at 36.96ms versus Clojure's 69.44ms. Since jank was only
marginally faster at the end of the last post, this also means the improvements
in the past quarter have been nearly 2x overall.

<figure>
  <object type="image/svg+xml" data="/img/blog/2023-08-26-object-model/ray-tracing.plot.svg" width="50%">
    <img src="/img/blog/2023-08-26-object-model/ray-tracing.plot.svg" width="50%"></img>
  </object>
</figure>

This is the primary celebration and is the culmination of a handful of months
worth of work, spanning back before I started this object model redesign.
When I could first run the ray tracer, [two blog posts ago](/blog/2023-04-07-ray-tracing) (5 months ago),
**jank took 797.49ms to run the exact same code**! 

A lot has changed in the past 5 months. Before I get to where jank will be in
the next 5 months, though, let's dig deeper into some of the
benchmark results.

## Maps
The previous post showed that jank had nearly caught up with Clojure in terms of
array map allocation speed. This hasn't changed since then, primarily because I
had already pushed map allocations as far as I can for now, with my prototype.
The final numbers are 16ns for Clojure and 17ns for jank. I'll be following up
on this, at a later time, by introducing a new GC (via [MMTK](https://www.mmtk.io/)), instead of Boehm.

Map lookups were already fast, but have been made twice as fast still.

<figure>
  <object type="image/svg+xml" data="/img/blog/2023-08-26-object-model/map.plot.svg" width="50%">
    <img src="/img/blog/2023-08-26-object-model/map.plot.svg" width="50%"></img>
  </object>
</figure>

## Vectors
Vector allocation speeds have been improved, but were quite slow to start with.
jank's vectors are backed by immer's persistent vectors and this allocation is
using the provided initializer list constructor. Clearly some work will be
needed here, possibly requiring changes to immer. The improvements we see are
solely due to the new object model being faster to allocate, since no other
changes were made.

It's also worth noting that Clojure JVM has some very efficient ways to
construct vectors which jank does not have. I'm not sure I can do this without
exposing some internals of immer, but it will likely be worth it, since those
Clojure JVM constructors can run in under 20ns. The one I'm showing here is the
constructor closest to what jank is doing (taking in an initializer list).

Similar to maps, vector lookups were already quick and have nearly doubled in
speed.

<figure>
  <object type="image/svg+xml" data="/img/blog/2023-08-26-object-model/vector.plot.svg" width="50%">
    <img src="/img/blog/2023-08-26-object-model/vector.plot.svg" width="50%"></img>
  </object>
</figure>

### Strings
jank's strings lag significantly behind Clojure JVM's. This is the most glaring
performance difference between the two. The new object model improves this, but
more work needs to be done. jank is currently using [folly](https://github.com/facebook/folly)'s string,
which is compliant with `std::string` but generally faster. However, folly's
string is using jemalloc, rather than Boehm, which means both that jank is
currently leaking string memory and also that allocations may be slower than
with Boehm. On top of that, folly strings have proven to be fast to use,
but slow to construct. I have work planned to provide a custom string instead.

I have included both short string and long string benchmarks here, since I know that
folly's implementation uses a short string optimization which avoids allocations and
stores the string data [in situ](https://en.wikipedia.org/wiki/In_situ). Still,
it's much slower than Clojure JVM. JVM strings may be magic, but we'll see when I look into it.

<figure>
  <object type="image/svg+xml" data="/img/blog/2023-08-26-object-model/string.plot.svg" width="50%">
    <img src="/img/blog/2023-08-26-object-model/string.plot.svg" width="50%"></img>
  </object>
</figure>

## Fast math
Math has sped up the most out of anything, which bodes very well for our ray
tracing numbers. Here are the results for fully boxed subtraction, where no
type info is known, as well as subtraction between an unknown box and an unboxed
double. In both cases, jank is now significantly faster than Clojure JVM. These
wins apply across the board for all binary math operations.

<figure>
  <object type="image/svg+xml" data="/img/blog/2023-08-26-object-model/boxed-sub.plot.svg" width="50%">
    <img src="/img/blog/2023-08-26-object-model/boxed-sub.plot.svg" width="50%"></img>
  </object>
</figure>

## Next quarter
This is the last performance-oriented bout of work for a while. jank is where it
needs to be, I think, in order for me to start investing more in pushing the
compiler and runtime features closer to parity with Clojure JVM. I'm very happy
to share that Clojurists Together is actually sponsoring jank development
*again*, for the upcoming quarter. The sponsored work will be focused on
building out jank's module system, implementing `clojure.core/require`,
preparing for iterative compilation, and setting the stage for AOT compilation
and leiningen integration.

After this work, using jank for multi-file projects will be possible. Soon after
that, I hope, we can start using leiningen to manage jank projects. This will
mean adventurous devs can start actually using jank themselves, which I expect
will only add to the momentum I currently have.

## Would you like to join in?
1. Join the community on [Slack](https://clojurians.slack.com/archives/C03SRH97FDK)
2. Join the design discussions or pick up a ticket on [GitHub](https://github.com/jank-lang/jank)
3. Considering becoming a [Sponsor](https://github.com/sponsors/jeaye)

## Benchmark sources
For those readers interested in my benchmark code, both the C++ (jank) and
Clojure JVM versions are provided in this gist: [here](https://gist.github.com/jeaye/2173da7851955ad13815862356cbfc6d).

All benchmarks were done on my Arch Linux desktop with a AMD Ryzen Threadripper 2950X using
OpenJDK 11 with the G1 GC.
