Title: jank development update - Load all the modules!
Date: Dec 17, 2023
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Description: jank now supports module loading, class paths, aliases, and
             referred vars. It's a big leap closer to becoming your favorite
             Clojure dialect.

I've been quiet for the past couple of months, finishing up this work on jank's
module loading, class path handling, aliasing, and var referring. Along the way,
I ran into some very interesting bugs and we're in for a treat of technical
detail in this holiday edition of jank development updates! A warm shout out to
my [Github sponsors](https://github.com/sponsors/jeaye)
and [Clojurists Together](https://www.clojuriststogether.org/) for sponsoring this work.

## Module loading progress
Ok, first and foremost, where is jank now with regard to module loading? I'm
very pleased to say that everything I wanted to tackle this quarter has been
finished and even more on top of that. There's a PR up for the full changes
[here](https://github.com/jank-lang/jank/pull/49).

Let's break this down by section.

### Class paths
jank traverses a user-defined class path, which supports directories and JAR
files, and can use that to find modules when you use `require` and friends. This
is specifically designed to be compatible with the JVM, so once we hook in
Leiningen or Clojure CLI, your existing dependency management should work just
fine.

### Necessary core functions
The following functions have all been implemented, which were required for
module loading:

* `require`
* `alias`
* `use`
* `refer`
* `load`

These take into account modules that are already loaded, flags for things like
reloading, excluding, etc. For most use cases, they're at functional parity with
Clojure on the happy path. Error handling will improve once I have some better
mechanisms for it.

Still, that's not a very big list of functions, I know. How about this one?

* `compile`
* `create-ns`
* `find-ns`
* `remove-ns`
* `the-ns`
* `ns-name`
* `ns-map`
* `ns-publics`
* `var?`
* `var-get`
* `keys` (note - not using a custom seq yet)
* `vals` (note - not using a custom seq yet)
* `name`
* `namespace`
* `subs`
* `gensym`
* `concat`
* `contains?`
* `find`
* `select-keys`
* `map` (note - not lazy yet, no transducers)
* `mapv` (note - not lazy yet, no transducers)
* `mapcat` (note - not lazy yet, no transducers)
* `filter` (note - not lazy yet, no transducers)
* `complement`
* `remove`
* `set?`
* `set`
* `vector`
* `doseq` (note - not supporting fancy `for` features yet)
* `list*`
* `apply`
* `some`
* `not-any?`
* `not=`
* `symbol`
* `var?`
* `cond`
* `and`
* `or`
* `ns`

All of these were needed by some of the above necessary functions, so I
implemented them as much as possible. Most of them have complete functional
parity with Clojure, but a few have interim implementations, especially since
jank doesn't yet have have an equivalent object type to Clojure JVM's `LazySeq`.
Still, jank feels, and looks, more and more like a proper Clojure every day.

### (Bonus) Initial AOT compilation
You may have noticed, in that list, that `compile` has been implemented. This is
an initial step toward AOT compilation and it compiles jank files into C++ files
on the class path. Those can then be loaded in lieu of the jank files for a
performance win. I also added a CMake job to jank's build system to build the
jank Clojure libs along with the compiler, so we can always have those
pre-compiled and also always know they actually compile.

I'm currently working with the Cling developers to get support added to Cling
for jank to pre-compile these C++ files into a closer equivalent to JVM class files.
In my local testing, the startup time improvements by doing this were 10x. I'll
have more info on this once the work picks up.

### (Bonus) CLI argument parsing
In order to support things like user-defined class paths, I've added a proper
CLI arg parser to jank. You can see the current options in the help output here:

```bash
❯ ./build/jank -h
jank compiler
Usage: ./build/jank [OPTIONS] SUBCOMMAND

Options:
  -h,--help                   Print this help message and exit
  --class-path TEXT           A : separated list of directories, JAR files, and ZIP files to search for modules
  --output-dir TEXT           The base directory where compiled modules are written
  --profile                   Enable compiler and runtime profiling
  --profile-output TEXT       The file to write profile entries (will be overwritten)
  --gc-incremental            Enable incremental GC collection
  -O,--optimization INT:INT in [0 - 3]
                              The optimization level to use

Subcommands:
  run                         Load and run a file
  compile                     Compile a file and its dependencies
  repl                        Start up a terminal REPL and optional server
```

Each subcommand has its own help output, too. Speaking of subcommands, however,
jank now has a `repl` subcommand which spins up a terminal REPL client with
readline enabled for (single session) history and improved editing. This has
been very handy for me as I'm testing out new things and was something that just
came naturally after implementing the CLI argument parsing.

```clojure
❯ ./build/jank repl
> (ns foo.bar)
nil
> *ns*
foo.bar
> (def wow "WOW!")
#'foo.bar/wow
> (def nice "NICE!")
#'foo.bar/nice
> (ns main)
nil
> *ns*
main
> (refer 'foo.bar :only '[wow])
nil
> wow
WOW!
> (alias 'fb 'foo.bar)
nil
> fb/nice
NICE!
> (ns omg.wow (:use [foo.bar :exclude [wow]]))
nil
> *ns*
omg.wow
> nice
NICE!
> (native/raw "*((char*)0) = 0;")
Segmentation fault (core dumped)
```

### (Bonus) Maps, sets, keywords as functions
As part of implementing all of the new core functions this quarter, I also
tackled these particular objects which behave as functions. Fortunately, because
of the new object model design, these objects can have this behavior without the
need for dynamic dispatch!

### There will be bugs
jank is still pre-alpha software. I have an ever growing test suite, but no
battle testing yet. As I develop more functionality, I find more issues and
introduce more yet. That will remain the case until development can settle down
and stable APIs can be decided. jank still isn't ready to compile most Clojure
programs, since it lacks support for some basic features like destructuring,
lazy sequences, and even doc strings. While we're talking about bugs, though,
and since I've shown everything else I've built this quarter, let me tell you
about such an interesting bug I found and how I fixed it.

## Variadic argument matching bug
I fixed a few interesting bugs in the past couple of months, but this one was
the most intriguing by far. So, the problem showed up in this case:

```clojure
(defn ambiguous
  ([a]
   :fixed)
  ([a & args]
   :variadic))

(ambiguous :a) ; => should be :fixed
```

What jank was trying to do was call the variadic arity, with an empty seq for
`args`, rather than to call the fixed arity. This is because both of them
require one fixed argument first and the information I was storing for each
function object was the required fixed args prior to variadic arg packing. 

The equivalent function in Clojure JVM is `RestFn.getRequiredArity`, which
returns the required fixed position arguments prior to the packed args. However,
where Clojure JVM differs from jank is that Clojure uses dynamic dispatch to
solve this ambiguity whereas jank does its own fixed vs variadic overload
matching, for performance reasons.

To actually solve this problem, we need to know three things:

1. Is the function variadic?
2. Is there an ambiguous fixed overload?
3. How many fixed arguments are required before the packed args?


We cannot perform the correct call without *all* of this information.
Also, function calls in a functional programming language like Clojure are on
the hottest of hot code paths, so I can't exactly add two more virtual functions
to jank's `callable` interface to get this data. In truth, even keeping one
function but putting all of this data in a struct proved too much of an impact
on the performance. Thus, we need to encode the data more compactly.

jank now packs all of this into a single byte. Questions 1 and 2 each get a high bit
and question 3 gets the 6 remaining bits (of which it uses 4) to store the fixed
arg count. So, this byte for our `ambiguous` function above would look like
this:

```cpp
1  1  0  0  0  0  0  1
^  ^  ^---------------
|  |  |
|  |  /* How many fixed arguments are required before the packed args? */
|  /* Is there an ambiguous overload? */
/* Is the function variadic? */
```

From there, when we use it, we disable the bit for question 2 and we
`switch` on the rest. This allows us to do a `O(1)` jump on the combination of
whether it's variadic and the required fixed args. Finally, we only need the
question 2 bit to disambiguate one branch of each switch, which is the branch
equal to however many arguments we received.

```cpp
object_ptr dynamic_call(object_ptr const source, object_ptr const a1)
{
  return visit_object
  (
    [=](auto const typed_source) -> object_ptr
    {
      using T = typename decltype(typed_source)::value_type;

      if constexpr(function_like<T> || std::is_base_of_v<callable, T>)
      {
        /* This is the whole byte, answering all three questions. */
        auto const arity_flags(typed_source->get_arity_flags());
        /* We strip out the bit for ambiguous checking and switch on it. */
        auto const mask(callable::extract_variadic_arity_mask(arity_flags));

        /* We're matching on variadic + required arg position. */
        switch(mask)
        {
          case callable::mask_variadic_arity(0):
            return typed_source->call(make_box<obj::native_array_sequence>(a1));
          case callable::mask_variadic_arity(1):
            /* Only in the case where the arg count == the required arity do we
               check the extra bit in the flags. */
            if(!callable::is_variadic_ambiguous(arity_flags))
            { return typed_source->call(a1, obj::nil::nil_const()); }
            /* We're falling through! */
          default:
            /* The default case is not variadic. */
            return typed_source->call(a1);
        }
      }
      else
      { /* ... redacted error handling ... */ }
    },
    source
  );
}
```

The special case, which needs to check the ambiguous flag, incurs a performance
cost, due to the if. Every other case is unaffected. This was a challenge to
wrap my head around at first, but after I wrote out all the things I need to
know, as well as a test suite for each of the cases, I could work toward a
solution which addressed everything.

## What's next?
Firstly, dynamic vars. Once those are implemented, I'll need to go through all
of the different parts of the compiler and runtime to start filling in vars.
This will allow everything from improved error messages by tracking
file/line/function to cyclical dependency checks on module loading.

Also, in order for jank to operate alongside other Clojure dialects, we'll need
to support reader conditionals on the `:jank` key. Currently, jank doesn't
support any reader macros, so getting that system going will open up the door to
things like `#()` and `#{}` being supported.

Finally, I'll be improving the interop interpolation syntax to be consistent with
ClojureScript, adding meta hint support, and more. Stay tuned!

## Would you like to join in?
1. Join the community on [Slack](https://clojurians.slack.com/archives/C03SRH97FDK)
2. Join the design discussions or pick up a ticket on [GitHub](https://github.com/jank-lang/jank)
3. Considering becoming a [Sponsor](https://github.com/sponsors/jeaye) <span class="icon mr-1" style="color: rgb(201, 97, 152);"> <i class="gg-heart"></i></span>
4. **Hire me full-time to work on jank!**
