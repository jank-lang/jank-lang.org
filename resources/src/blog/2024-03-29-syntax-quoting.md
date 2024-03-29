Title: jank development update - Syntax quoting!
Date: Mar 29, 2024
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Description: jank now supports syntax quoting, more reader macros, transients,
             and, and, and other things!

Oh, hey folks. I was just wrapping up this macro I was writing. One moment.

```clojure
(defmacro if-some [bindings then else]
  (assert-macro-args (vector? bindings)
                     "a vector for its binding"
                     (= 2 (count bindings))
                     "exactly 2 forms in binding vector")
  (let [form (get bindings 0)
        pred (get bindings 1)]
    `(let [temp# ~pred]
       (if (nil? temp#)
         ~else
         (let [~form temp#]
           ~then)))))
```

*"Does all of that work in jank?"* I hear you asking yourself. Yes! Indeed it
does. Since my [last update](https://jank-lang.org/blog/2024-02-23-bindings/), which added dynamic
bindings, meta hints, and initial reader macros, I've finished up syntax quoting
support, including gensym support, unquoting, and unquote splicing. We might as
well see all of this working in jank's REPL CLI.

```clojure
❯ jank repl
> (defmacro if-some [bindings then else]
    (assert-macro-args (vector? bindings)
                       "a vector for its binding"
                       (= 2 (count bindings))
                       "exactly 2 forms in binding vector")
    (let [form (get bindings 0)
          pred (get bindings 1)]
      `(let [temp# ~pred]
         (if (nil? temp#)
           ~else
           (let [~form temp#]
             ~then)))))
#'clojure.core/if-some
> (if-some [x 123]
    (str "some " x)
    "none")
"some 123"
> (if-some [x nil]
    (str "some " x)
    "none")
"none"
>
```

### New interpolation syntax
Some of the early feedback I had for jank's inline C++ support is that the
interpolation syntax we use is different from what ClojureScript uses. Turns out
there's no reason to be different, aside from jank needing some more work, so
jank has been improved to support the new `~{}` syntax. If you're not familiar,
inline C++ in jank looks like this:

```clojure
(defn sleep [ms]
  (let [ms (int ms)]
    ; A special ~{ } syntax can be used from inline C++ to interpolate
    ; back into jank code.
    (native/raw "auto const duration(std::chrono::milliseconds(~{ ms }->data));
                 std::this_thread::sleep_for(duration);")))
```

### More reader macros

Aside from that, reader macro support has been extended to include shorthand
`#()` anonymous functions as well as `#'v` var quoting. The only reader macro
not yet implemented is `#""` for regex. All of that concludes what I had aimed
to accomplish for my quarter, and then some. It doesn't stop there, though.

### New logo
I'm wonderfully pleased to announce that jank now has a logo! The logo was
designed by [jaide](https://github.com/jaidetree), who was graciously patient
with me and a joy to work with through the various iterations. With this logo, 
we're capturing C++ on one side, Lisp on the other, and yet a
functional core.

<figure>
  <img src="/img/blog/2024-03-29-syntax-quoting/logo.png" width="50%"></img>
</figure>

### Transients
Back to code. In truth, there's more work going on. A lovely man named
[Saket](https://github.com/Samy-33) has been helping me fill out jank's
transient functionality, which now includes array maps, vectors, and sets, as
well as the corresponding `clojure.core` functions. This is not the first time
I've brought up Saket, since he also implemented the initial lein-jank plugin.
Let's take a look at that.

### lein-jank
This plugin isn't ready for prime time yet, but it's a good proof of concept
that jank can work with leiningen's classpaths and it's a good testing ground
for multi-file projects. jank will be adding AOT compilation soon and this
lein-jank plugin will be the first place new features will land. As a brief
demonstration of where it is today, take a look at this session.

```clojure
❯ cat project.clj
(defproject findenv "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.11.1"]]
  :plugins [[lein-jank "0.0.1-SNAPSHOT"]]
  :jank {:main findenv.core})

❯ cat src/findenv/core.jank
(ns findenv.core)

(defn -main [& args]
  (let [env-var (first args)
        ; Call through into native land to look up the var.
        env-val (or (native/raw "auto const str(runtime::detail::to_string(~{ env-var }));
                                 __value = make_box(std::getenv(str.c_str()));")
                    "var not found")]
    (println env-val)))

❯ export FINDME="found me!"

❯ lein jank run FINDME
found me!

❯ lein jank run YOUWONTFINDME
var not found

❯ lein jank run LC_ALL
en_US.UTF-8
```

### Migration from Cling to clang-repl
Lastly, I've been working on migrating jank to use the upstream LLVM version of
Cling, called clang-repl. The key benefit here is that we'd no longer need to
compile our own Cling/Clang/LLVM stack in order to build jank and we can
distribute jank to use each distro's normal LLVM package, rather than its own.
On top of that, future work is happening more on clang-repl than on
Cling, so it has recent support for loading precompiled C++20 modules, for
example. That would greatly improve jank's startup performance, since Cling
doesn't allow us to load precompiled modules at this point.

Work here is ongoing and there are some bugs that I have identified in clang-repl
which need to be fixed before jank can fully make the switch. I'll keep you
all updated!

## Would you like to join in?
1. Join the community on [Slack](https://clojurians.slack.com/archives/C03SRH97FDK)
2. Join the design discussions or pick up a ticket on [GitHub](https://github.com/jank-lang/jank)
3. Considering becoming a [Sponsor](https://github.com/sponsors/jeaye) ❤️ 
4. **Hire me full-time to work on jank!**
