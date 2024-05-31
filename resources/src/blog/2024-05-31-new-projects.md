Title: jank development update - New projects!
Date: Apr 27, 2024
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Description: jank now has chunked sequences, volatiles, atoms, and so
             many new functions! On top of all of that, some big new
             projects are under development.

Hey folks! I've been building on last month's addition of lazy sequences,
`loop*`, destructuring, and more. This month, I've worked on rounding out
lazy sequences, adding more mutability, better meta support, and some big
project updates. Shout-out to
[Clojurists Together](https://www.clojuriststogether.org/), who are funding my
work this quarter.

## Chunked sequences
I've expanded the lazy sequence support added last month to include chunked
sequences, which pre-load elements in chunks to aid in throughput. At this
point, only `clojure.core/range` returns a chunked sequence, but all of the
existing `clojure.core` functions which should have support for them do.

If you recall from last month, there is a third lazy sequence type: buffered
sequences. I won't be implementing those until they're needed, as I'd never even
heard of them before researching more into the lazy sequences in Clojure.

## Initial quarter goals accomplished
Wrapping up the lazy sequence work, minus buffered sequences, actually checked
off all the boxes for my original goals this quarter. There's a bottomless
well of new tasks, though, so I've moved onto some others. So, how do I decide what
to work on next?

My goal is for you all to be writing jank programs. The most important tasks are
the ones which bring me closer to that goal. Let's take a look at what those
have been so far.

## Volatiles, atoms, and reduced
Most programs have some of mutation and we generally handle that with volatiles
and atoms in Clojure. jank already supported transients for most data structures,
but we didn't have a way to hold mutable boxes to immutable values. Volatiles are
also essential for many transducers, which I'll mention a bit later. This month,
both volatiles and atoms have been implemented.

Implementing atoms involved a fair amount of research, since lockless
programming with atomics is
[not nearly as straightforward](https://www.youtube.com/watch?v=c1gO9aB9nbs)
as one might expect.

As part of implementing atoms, I also added support for the `@` reader macro and
the overall `derefable` behavior. This same behavior will be used for delays,
futures, and others going forward.

## Meta handling for defs
Last quarter, I added support for meta hints, but I didn't actually use that
metadata in many places. Now, with defs, I've added support for the optional
meta map and doc string and I also read the meta from the defined symbol. This
isn't a huge win, but it does mean that jank can start using doc strings
normally, and that we can do things like associate more function meta to the var
in a `defn`, which can improve error reporting.

## Monorepo
There will be many jank projects and I've known for a while that I want them
all to be in one git [monorepo](https://en.wikipedia.org/wiki/Monorepo).
This makes code sharing, searching, refactoring, and browsing simpler. It gives
contributors one place to go in order to get started and one place for all of
the issues and discussions. I don't care to convince you of this, if you're not
a fan of monorepos, but jank is now using one. 

This started by bringing in [lein-jank](https://github.com/Samy-33/lein-jank),
which was initially created by Saket Patel. From there, I've added a couple of
more projects, which I'll cover later in this update.

## New clojure.core functions
Following last month's theme, which saw 52 new Clojure functions, I have
excellent news. We actually beat that this time, adding 56 new Clojure functions
in the past month! However, I only added 23 of those and the other 33 were added
by [madstap](https://github.com/madstap) (Aleksander Madland Stapnes). He did
this while also adding the transducer arity into pretty much every existing
sequence function. Volatiles were originally added to support him in writing
those transducers.

| | |
|---|---|
| `dotimes` | `chunk` |
| `chunk-first` | `chunk-next` |
| `chunk-rest` | `chunk-cons` |
| `chunked-seq?` | `volatile!` |
| `vswap!` | `vreset!` |
| `volatile?` | `deref` |
| `reduced` | `reduced?` |
| `ensure-reduced` | `unreduced` |
| `identical?` | `atom` |
| `swap!` | `reset!` |
| `swap-vals!` | `reset-vals!` |
| `compare-and-set!` | `keep` |
| `completing` | `transduce` |
| `run!` | `comp` |
| `repeatedly` | `tree-seq` |
| `flatten` | `cat` |
| `interpose` | `juxt` |
| `partial` | `doto` |
| `map-indexed` | `keep-indexed` |
| `frequencies` | `reductions` |
| `distinct` | `distinct?` |
| `dedupe` | `fnil` |
| `every-pred` | `some-fn` |
| `group-by` | `not-empty` |
| `get-in` | `assoc-in` |
| `update-in` | `update` |
| `cond->>` | `as->` |
| `some->` | `some->>` |

## New projects
At this point, I was thinking that jank actually has pretty darn good Clojure
parity, both in terms of syntax and essential core functions. So how can I best
take steps toward getting jank onto your computer?

Well, I think the most important thing is for me to start writing some actual
projects in jank. Doing this will require improving the tooling and will help
identify issues with the existing functionality. The project I've chosen is
jank's nREPL server. By the end of the project, we'll not only have more
confidence in jank, we'll all be able to connect our editors to running jank
programs!

## nREPL server
nREPL has [some docs](https://nrepl.org/nrepl/building_servers.html) on building
new servers, so I've taken those as a starting point. However, let's be clear,
there are going to be a *lot* of steps along the way. jank is *not* currently
ready for me to just build this server today and have it all work. I need a goal
to work toward, though, and every quest I go on is bringing me one step closer
to completing this nREPL server in jank. Let's take a look at some of the things
I know I'll need for this.

#### Module system
jank's module loader was implemented two quarters ago, but since there are no
real jank projects, it hasn't seen much battle testing. To start with, I will
need to work through some issues with this. Already I've found (and fixed) a
couple of bugs related to module writing and reading while getting started on
the nREPL server. Further improvements will be needed around how modules are
cached and timestamped for iterative compilation.

#### Native interop
Next, jank's native interop support will need to be expanded. I've started that
this month by making it possible to now write C++ sources alongside your jank
sources and actually `require` them from jank! As you may know, jank allows
for inline C++ code within the special `native/raw` form, but by compiling
entire C++ files alongside your jank code, it's now much easier to offload
certain aspects of your jank programs to C++ without worrying about writing too
much C++ as inline jank strings.

jank's native interop support can be further improved by declaratively noting
include paths, implicit includes, link paths, and linked libraries as part of
the project. This will likely end up necessary for the nREPL server as well.

#### AOT compilation
Also required for the nREPL server, I'll need to design and implement jank's AOT
compilation system. This will involve compiling all jank sources and C++ sources
together and can allow for direct linking, whole-program link time optimizations
(LTO), and even static runtimes (no interactivity, but smaller binaries).

#### Distribution
Finally, both jank and the nREPL server will need distribution mechanisms for
Linux and macOS. For jank, that may mean AppImages or perhaps more integrated
binaries. Either way, I want this to be easy for you all to use and I'm
following Rust/cargo as my overall inspiration.

I hope I've succeeded in showing how much work will remains for this nREPL
server to be built and shipped out. However, I think having this sort of goal in
mind is very powerful and I'm excited that jank is far enough along to where I
can actually be doing this.

## nREPL server progress
Since I have C++ sources working alongside jank source now, I can use
boost::asio to spin up an async TCP server. The data sent over the wire for
nREPL servers is encoded with bencode, so I started on a `jank.data.bencode`
project and I have the decoding portion of that working. From there, I wanted to
write my tests in jank using `clojure.test`, but I haven't implemented
`clojure.test` yet, so I looked into doing that. It looks like `clojure.test`
will require me to implement multimethods in jank, which don't yet exist. On top
of that, I'll need to implement `clojure.template`, which requires
`clojure.walk`.

I'll continue on with this depth-first search, implementing as needed, and then
unwind all the way back up to making more progress on the nREPL server. Getting
`clojure.test` working will be a huge step toward being able to
[dogfood](https://en.wikipedia.org/wiki/Eating_your_own_dog_food) more, so I
don't want to cut any corners there. Once I can test my decode
implementation for bencode, I'll write the encoding (which is easier) and then
I'll be back onto implementing the nREPL server functionality.

Hang tight, folks! We've come a long way, and there is still so much work to do,
but the wheels are rolling and jank is actually becoming a usable Clojure
dialect. Your interest, support, questions, and encouragement are all the
inspiration which keeps me going.

## Would you like to join in?
1. Join the community on [Slack](https://clojurians.slack.com/archives/C03SRH97FDK)
2. Join the design discussions or pick up a ticket on [GitHub](https://github.com/jank-lang/jank)
3. Considering becoming a [Sponsor](https://github.com/sponsors/jeaye) <span class="icon" style="color: rgb(201, 97, 152);"> <i class="gg-heart"></i></span>
4. **Hire me full-time to work on jank!**
