Title: jank development update - Lazy sequences!
Date: Apr 27, 2024
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Description: jank now has lazy seqs, loop, destructuring, and a whole slew of
             new functions!

This quarter, I'm being funded by [Clojurists Together](https://www.clojuriststogether.org/)
to build out jank's lazy sequences, special `loop*` form, destructuring, and
support for the `for` and `doseq` macros. Going into this quarter, I had only a
rough idea of how Clojure's lazy sequences were implemented. Now, a month in,
I'm ready to report some impressive progress!

## Lazy sequences
There are three primary types of lazy sequences in Clojure. I was planning on
explaining all of this, but, even better, I can shine the spotlight on
[Bruno Bonacci's blog](https://blog.brunobonacci.com/2023/09/08/buffered-sequences/),
since he's covered all three of them very clearly. In short, we have:

1. Per-element lazy sequences
2. Chunked lazy sequences
3. Buffered lazy sequences

This month, I have implemented per-element lazy sequences, along with partial
support for chunked lazy sequences. Chunked lazy sequences will be finished next
month. By implementing even per-element lazy sequences, so many new
opportunities open up. I'll show what I mean by that later in this post, so
don't go anywhere!

## Loop
Prior to this month, jank supported function-level `recur`. As part of this
month's work, I also implemented `loop*` and its related `recur`. When we look at
how Clojure JVM implements `loop*`, it has two different scenarios:

1. Expression loops
2. Statement loops

If a loop is in a statement position, Clojure JVM will code-generate labels with
goto jumps and local mutations. If the loop is an expression, Clojure JVM
generates a function around the loop and then immediately calls that. There is
*potentially* a performance win of not generating the function wrapper and
calling it right away, but note that this particular idiom is commonly
identified and elided by optimizing compilers. It even has its own acronym:
[IIFE](https://en.wikipedia.org/wiki/Immediately_invoked_function_expression).
(see [this](https://rigtorp.se/iife/) also)

jank, for now anyway, simplifies this by always using the IIFE. It does it in a
more janky way, though, which is interesting enough that I'll share it with you
all. Let's take an example `loop*` (note that the special form is actually
`loop*`, same as in Clojure; `loop` is a macro which provides destructuring on
top of `loop*` -- now you know):

```clojure
(loop* [x 0]
  (when (< x 10)
    (println x)
    (recur (inc x))))
```

Given this, jank will replace the `loop*` with a `fn*` and just use function
recusion. Initial loop values just get lifted into parameters. The jank compiler
will transform the above code into the following:

```clojure
((fn* [x]
   (when (< x 10)
     (println x)
     (recur (inc x)))) 0)
```

jank code-generates function recursion into a `while(true)` loop with mutation on some
locals for each iteration, similar to Clojure.

However, `loop*` is tricky, since it can also do anything `let*` can do. For
example (also note: no recursion):

```clojure
(loop* [a 1
        b (* 2 a)]
  (println a b))
```

Since we're using `a` in the binding for `b`, we can't know `b` until we've
calculated `a`, and doing so can involve any arbitrary expression. Agh! This
can't work if we just dump those into the positional parameters of the IIFE. So
jank gets around this by actually just wrapping it in a `let*`. 🙃

```clojure
(let* [a 1
       b (* 2 a)]
  ((fn* [a b]
    (println a b)) a b))
```

This could be done in a macro, but since it's a language-level feature, the
compiler does it for us. This means you can still use `loop*` even if you're
running without `clojure.core`.

## Destructuring
Clojure supports all kinds of fancy destructuring of sequences, maps, and
keyword arguments. We use destructuring in `let`, `defn`, and `loop`, primarily. One
interesting thing about this destructuring is that there's no compiler support
for it at all; it's not a language-level feature. It's a library feature, done
entirely in macros. The amazing thing about this is that, as long as we support
all of the core functions required, we can support destructuring. The actual
`destructure` function is huge, but you can see it
[here](https://github.com/clojure/clojure/blob/06d450895e2d4028afaa4face17f8e597c772a24/src/clj/clojure/core.clj#L4417-L4511).

This month, I implemented all of the missing functions required for
the `destructure` function to be ported over to jank. Largely, once all those
functions were implemented, the port just meant updating Java interop in a few
places to be C++ interop. Now jank supports all of the fancy destructuring
Clojure does, in all the same places. This helps demonstrate how much closer
jank is to being a complete Clojure dialect, since complex functions like this
can *almost* just work.

## New clojure.core functions
So, to support lazy sequences and destructuring, I needed to add several new
core functions. While adding those, I tended toward implementing any similar or
surrounding functions as well. I got a little carried away, to be honest. Let's
take a look at the new functions jank now supports.

| | |
|---|---|
| `take`  (no transducer) |       `cycle` |
| `take-while` (no transducer) | `repeat` |
| `drop` (no transducer) |       `seq?` |
| `filter` (no transducer) |     `concat` |
| `identity` |                   `->` |
| `constantly` |                 `->>` |
| `into` (no transducer) |       `cond->` |
| `mapv` |                       `zipmap` |
| `filterv` |                    `last` |
| `reduce` |                     `butlast` |
| `nthrest` |                    `map?` |
| `nthnext` |                    `key` |
| `partition` |                  `val` |
| `partition-all` |              `dissoc` |
| `partition-by` |               `ident?` |
| `dorun` |                      `simple-ident?` |
| `doall` |                      `qualified-ident?` |
| `when-let` |                   `boolean` |
| `when-some` |                  `nth` |
| `when-first` |                 `loop` |
| `split-at` |                   `peek` |
| `split-with` |                 `pop` |
| `drop-last` |                  `for` |
| `take-last` |                  `chunk-buffer` |
| `chunk-append` |             `destructure`     |

That's 52 new functions/macros! That alone amounts to around 10% of all the
functions in `clojure.core` jank will be implementing. A few of these will need
some updates once jank fully supports chunked lazy sequences and transducers,
but they're all very usable today. You may also note that `for` is in there,
which was one of the goals this quarter.

## Migration from Cling to Clang
jank is much closer to running on Clang's JIT compiler than it was a month ago.
Some recent patches have landed which partially address a blocking bug with
pre-compiled header handling in Clang's internal C++ JIT compiler. I have
identified another small reproduction case for what I hope to be the rest of the
issues. Part of my work this month involved getting jank running on LLVM
19 and updating filling out the related CMake system to be able to flexibly
bring in LLVM on any system.

Once jank moves away from Cling in favor of Clang, building and distributing
jank will be significantly easier. Developers won't need to compile a custom
Cling/Clang/LLVM stack. On top of that, Clang's JIT compiler has recently landed
support for loading C++20 modules, which can serve as an less-portable
equivalent to JVM's class files, allowing jank to load pre-compiled modules very
quickly. This will drastically optimize jank's startup time, but will require
some work to get going. I'll keep you updated!

## What's next?
I'm well ahead of schedule, for the quarter, but I need to finish up chunked
sequences and `doseq`. I'll have time after that and I'd like to get atoms
working, since most Clojure programs have some form of state. From there, I can
look into strengthening native interop and making jank more easily
distributable, but let's not get ahead of ourselves.

## Website changes
On a smaller note, the theming of this website has been spruced up. Those of you
with your browsers set to prefer dark themes may notice that this website now
respects it. I try to make things look appealing without requiring any JS,
even for all of the code highlighting and charts across my posts. Your
feedback on my success here would also be welcome. The static site itself is
built in Clojure!

## Would you like to join in?
1. Join the community on [Slack](https://clojurians.slack.com/archives/C03SRH97FDK)
2. Join the design discussions or pick up a ticket on [GitHub](https://github.com/jank-lang/jank)
3. Considering becoming a [Sponsor](https://github.com/sponsors/jeaye) <span class="icon" style="color: rgb(201, 97, 152);"> <i class="gg-heart"></i></span>
4. **Hire me full-time to work on jank!**
