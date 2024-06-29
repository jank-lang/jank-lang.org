Title: jank development update - Multimethods!
Date: June 29, 2024
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Description: TODO

Welcome back to another jank development update! For the past month, I've been
pushing jank closer to production readiness primarily by working on multimethods
and by debugging issues with Clang 19 (currently unreleased). Much love to
[Clojurists Together](https://www.clojuriststogether.org/) and all of my
[Github sponsors](https://github.com/sponsors/jeaye) for their support this
quarter.

## Multimethods
I thought, going into this month, that I had a good idea of how multimethods
work in Clojure. I figured we define a dispatch function with `defmulti`:

```clojure
(defmulti sauce-suggestion ::noodle-type)
```

Then we define our catch-all method for handling types:

```clojure
(defmethod sauce-suggestion :default [noodle]
  (println "You can't go wrong with some butter and garlic."))
```

Then we define some specializations for certain values which come out of our
dispatch function.

```clojure
(defmethod sauce-suggestion ::shell [noodle]
  (println "Cheeeeeeeese!"))

(defmethod sauce-suggestion ::flate-white-rice [noodle]
  (println "Hor fun gravy."))
```

Then, when you call the `sauce-suggestion` function, first the dispatch
function is called and then the correct method is looked up and called.

```clojure
(sauce-suggestion {::noodle-type ::shell})
Cheeeeeeeese!

(sauce-suggestion {::noodle-type ::spaghetti})
You can't go wrong with some butter and garlic.
```

This is as much as I knew. But wait, there's more!

### Hierarchies
It turns out that multimethods match dispatch values based on a couple of
different hierarchies, too. If you're matching actual class types, like
`String`, you could have a method which is parameterized on `Object` and it will
be a catch-all. So this would allow you to match on everything which inherits
from `IRenderable`, for example, and then use that interface to render the
object. I wasn't concerned about this, since jank's object model isn't based on
inheritance. I figured I could leave this whole feature out of multimethods.

However, it turns out that Clojure supports another form of hierarchies! Even
crazier, we have full control over those hierarchies at run-time and we can
build as many as we want. Check this out.

```clojure
; We can classify spaghetti and penne as Italian.
; They will both be considered children of ::italian.
(derive ::spaghetti ::italian)
(derive ::penne ::italian)

; Then we can define a method based on the parent.
(defmethod sauce-suggestion ::italian [noodle]
  (println "Sugo al pomodoro."))

; This allows us to match multiple dispatch values in a
; deterministic and intuitive way.
(sauce-suggestion {::noodle-type ::penne})
Sugo al pomodoro.
```

There are a handful of related core functions for working with these
hierarchies. jank now implements all of them.

* `make-hierarchy`
* `isa?`
* `parents`
* `ancestors`
* `descendents`
* `derive`
* `underive`

As I was implementing multimethods, I needed a few more core functions, so those
were all implemented as well:

* `hash-set`
* `disj`
* `defmulti`
* `alter-var-root`
* `bound?`
* `thread-bound?`

Notably, this includes `bound?`, which required me to actually create a
dedicated unbound var object so I could distinguish between unbound vars and
vars holding `nil`.

## Clang/LLVM 19
Most of my time this past month was not spent developing new features for jank,
which is why I only have multimethods and 13 new functions to report. Instead,
my time was spent trying to get jank ported over to the latest Clang/LLVM
version, which will allow us to leave Cling behind. jank uses these for JIT
compiling C++ code and upgrading to the upstream Clang will unlock huge
performance wins, make compiling jank easier, and will allow for jank to follow
the bleeding edge of the native JIT space. However, before we get there, we have a
couple of bugs to get past.

### Extern templates
The [first bug](https://github.com/llvm/llvm-project/issues/97137), which was causing JIT
linking issues, I reduced down to a simple test case involving an extern
template which is linked either in the current process or in a loaded shared
library. Clang will be unable to resolve the address of the definition of that
function. As it happens, the fmt library uses this pattern to provide some
optimized versions of certain templates. However, we can fortunately work around
this, since fmt wraps those definitions in a `FMT_HEADER_ONLY` preprocessor flag.
The relevant fmt source is [here](https://github.com/fmtlib/fmt/blob/b61c8c3d23b7e6fdf9d44593877dba1c8a291be1/include/fmt/format.h#L4283).

The process of narrowing this down from the entire jank runtime is cumbersome,
ruling out chunks of code at a time while still trying to keep things compiling
and correct.

### Optimization crash
This is the [blocking bug](https://github.com/llvm/llvm-project/issues/95581)
preventing jank from switching to Clang. It only happens in release builds,
which also makes it harder to debug. This month, I traced the bug down from
a crash in jank all the way to a minimal test case involving assignments with an
implicit constructor. However, when testing whether or not the bug existed in
Clang 18, I found that it indeed did not. This meant that it's since been
introduced in the yet unreleased Clang 19. So I bisected around 1300 commits,
each time requiring a fresh Clang/LLVM compilation and taking ~30m. It was an
entire day of all 32 cores on my machine being busy compiling, but fortunately I
could script all of the hard work just using some bash. Bisecting allowed me to
find the commit which introduced the issue. This has yet to be fixed and I don't
have the expertise to know what's wrong with that commit, but I've provided a
test case, pinged the relevant people, and now I'm hoping the real experts can
come in for the save.

### Clang status
Aside form those two issues, only one of them being a blocker, the port to Clang
is ready. In debug builds (which avoid the second bug), jank can pass its full
test suite using Clang 19. Even better, some early benchmarking has shown that
Clang 19 is **more than twice as fast** as Cling when it comes to JIT compiling
large amounts of generated C++ code (such as all of `clojure.core`). That will
mean faster startup times and shorter REPL iteration loops.

## Community progress: characters
This month, [Saket Patel](https://github.com/Samy-33) has been working on adding
character support to jank. This involves support for tokens such as `\a`,
`\space`, and Unicode characters like `\u2764`. He has
[a PR open](https://github.com/jank-lang/jank/pull/78) for initial character
support, which is still in review. It should be merged soon.

Aside from regexes and ratios, characters are the only remaining syntax objects
which have not yet been implemented in jank!

## What's next?
Implementing multimethods identified a couple of issues related to certain
sequence types in jank which I'm still investigating. Once those are sorted,
I'll continue working through the requirements to implement `clojure.test`,
which is why I was implementing multimethods in the first place. From there, I
can start testing my jank code using more jank code and the dogfooding cycle can
really begin. Stay tuned, folks!

## Would you like to join in?
1. Join the community on [Slack](https://clojurians.slack.com/archives/C03SRH97FDK)
2. Join the design discussions or pick up a ticket on [GitHub](https://github.com/jank-lang/jank)
3. Considering becoming a [Sponsor](https://github.com/sponsors/jeaye) <span class="icon" style="color: rgb(201, 97, 152);"> <i class="gg-heart"></i></span>
4. **Hire me full-time to work on jank!**
