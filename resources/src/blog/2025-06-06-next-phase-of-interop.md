Title: The next phase of jank's C++ interop
Date: Jun 06, 2025
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Description: C++! Clojure! Magic! Dragons! Read all about it!

Two months into seamless C++ interop in jank, what sort of magic can we do now?
Sit tight and I'll show you. This quarter's work is being sponsored
not only by all of my individual Github sponsors, but also by Clojurists Together.
Thank you for the support!

## Seamless interop
As discussed in my [previous post](https://jank-lang.org/blog/2025-05-02-starting-seamless-interop/),
I am striving to match Clojure's seamless interop with Java, but instead with
C++. This involves JIT compiling C++, using Clang as a library, fitting Clang's
type system into jank's, and weaving together LLVM IR generated from C++ into jank's IR.

jank is leveraging Clang and
[CppInterOp](https://github.com/compiler-research/CppInterOp) to make this happen and it's the first
Lisp to ever achieve this seamlessness. It's also the first dynamically typed
language to support AOT compilation with interleaved C++ IR, as far as I know!

## Tests
Tests?! Who wants to read about those? Actually, when building something as
complex as jank, especially with all of these C++ shenanigans, tests are
literally the only way to have confidence in what I'm building. Last month was a
mad race to prove that this whole idea can work. Fortunately, it can! But I was
left with a couple thousand lines of new code, all of which had been manually
tested, but I had no confidence in any of it. Before I progressed with more
features this month, I added around 60 new tests, each covering a specific
pass/fail case for the features introduced last month. Unsurprisingly, around
half of them did not pass. Also unsurprisingly, around half of those actually
crashed.

Getting everything into a working state took about half of this month, but now I
_know_ this code works. I have thought of so many ways to break it and have
codified those into the test suite. Permit me, please, to share a bit about
jank's testing methodologies. The most important thing is that each new test is
focusing on a language feature from the user's perspective. That is to say, it's
a [functional test](https://en.wikipedia.org/wiki/Functional_testing). I don't
care very much that some `jank::util::helper` function has exhaustive unit
testing. I care that the compiler works correctly when it's given good and bad
source code.

For jank, we have a few different test suites which focus on testing in
different ways.

### Lexer/parser (functional tests)
We have suite of functional tests in C++ for both the lexer and the parser. For
these, we feed in source and verify we get the expected tokens or objects back.
They test the lexer as a whole, and the parser as a whole, rather than each of
the many dozens of functions involved with either.

### Analysis/codegen (functional tests)
For verifying that analysis and codegen work to catch errors and produce working
code, we have a system driven by C++ which is constructed of a bunch of `.jank`
files in nested directories. There's a C++ test runner which recursively finds
each `.jank` file and then runs it. The files must start with either `pass-` or
`fail-` (we support some others, like `throw-`, `warn-`, and `skip-`) and that
dictates what result we expect. Furthermore, `pass-` files need to evaluate to
`:success`. For example, we have a test `test/cpp/cast/pass-same-type.jank`:

```clojure

(let* [a (cpp/cast cpp/int (cpp/int. 4))
       b (cpp/cast cpp/double (cpp/double. 4.0))]
  (if (and (= 4 a) (= 4.0 b))
    :success))
```

For all of the new C++ interop features, this is where the new tests were added.

### End to end (system tests)
Outside of C++ entirely, we have a set of scripts which operate similar to the
`.jank` tests where we find scripts named `pass-test` within nested directories
and then invoke them. Those scripts use the `jank` binary to run files, compile
modules, etc. Scripts can have setup steps to create JARs, directories, and so
on. This allows us to test features like CLI usage, module paths, and AOT
compilation.

### Miscellaneous (unit tests)
jank does have some custom data structures, for which we have unit tests. Some
analysis behavior, such as box tracking, is unit tested as well. Overall, this
is a very small percentage of the tests.

### clojure-test-suite
Finally, the jank community has been working on a unit test suite for
`clojure.core` and other official libs, such as `clojure.string`. This is a test
suite which runs for Clojure JVM, ClojureScript, and jank. The goal is for all
Clojure dialects to be able to run it. Right now, we have about 20% coverage
of `clojure.core` and we're looking for more contributors to help out. If you're
interested, click [here](https://github.com/jank-lang/clojure-test-suite)! All
you need to know is Clojure.

Now, onto new features.

## Free functions
Once I had ironed out all of the issues with last month's code and I had
confidence in the code again, I added support for global functions. These follow
the same syntax we've been using so far. You might think that global function
calls are simple enough, but this is C++ we're talking about.

1. Functions can be overloaded
2. Function calls can have implicit conversions for arguments
3. There exists a nasty `void` type which has no value
4. Functions can be variadic
5. Functions can be templates (and variadic templates!)
6. jank supports further implicit conversions between jank objects and native
   types

Let's see how that looks by calling some C functions!

```clojure
; Straight up C calls to seed rand.
(cpp/srand (cpp/time cpp/nullptr))
; C call for rand, mixed with Clojure calls for mod and zero?.
(if (zero? (mod (cpp/rand) 2))
  ; Straight up C calls to print.
  (cpp/printf "Your name sings in my ears, Rand al'Thor.")
  (cpp/printf "The Wheel weaves as the Wheel wills."))
```

Oh, I also added `nullptr` support, which is a special C++ value which can
implicitly convert to any pointer type.

## Interlude
Hey! Are you digging the jank updates? Consider signing up for the mailing list.
This is the best way to make sure you stay up to date with jank's releases,
jank-related talks, workshops, and so on.

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

## Member functions
Member function calls are similar to global calls, but they have an implicit
`this` argument. On top of that, member functions can be overloaded based on
their constness and also based on their ref-qualifier. They can also be public,
protected, or private! So, in C++, you can write a member function which only
gets matched when calling it from a const invoking object which is an lvalue.
Tackling all of this correctly is tricky. At this point, I have most of these
covered.

Let's see it in action.

```clojure
(cpp/raw "#include <string>")
(let [s (cpp/cast cpp/std.string "meow")
      size (cpp/.size s)]
  (str s " contains " size " bytes."))
```

## Member access
While we're reaching into members, we might as well cover the new member access
support. Following Clojure's syntax, we can now use `(cpp/.-foo bar)` to
effectively to the equivalent of `foo.bar` in C++. Interestingly, this will get a
*reference* to the member, rather than a copy. This will ensure you're not
accidentally copying things around!

```clojure
(cpp/raw "namespace wow_members
          {
            struct bar
            {
              int a{ 500 };
            };
          }")
(let* [b (cpp/wow_members.bar.)
       ; a is a reference to b's a, not a copy.
       a (cpp/.-a b)]
  (println "Wow!" a))
```

This same syntax also works for accessing static members through an object! As
we saw in the previous post, static members can also be accessed directly using
the qualified name (i.e. `cpp/std.string.npos`).

## References
Speaking of references, C++'s references are bonkers. They're just like
pointers, **except**:

1. References cannot be initialized with null (in a well-defined program)
2. References have no address of their own; the address of a reference is the
   address of the referred value (pointers have their own address, wherein the
   address of its pointee is held)
3. References use the `.` notation for member access, even though there is
   indirection going on
4. When passing a value as an argument to a function which takes a reference,
   the argument will *automatically* get its address taken

This is so complicated to deal with, from an interop perspective. Clang,
naturally, implements references as pointers, but there's nothing in the
standard saying that needs to be the case. At this point, jank has pretty good
coverage of all of the different scenarios, but there are still some edge cases
I've identified which need fixes and tests.

## Operators
Lastly, this month I tackled the myriad of C++ operators. By my count, there are
[45 of them](https://en.cppreference.com/w/cpp/language/expressions.html#Operators) for
jank to support, many allowing both unary and binary usage, sometimes with
incredibly different meaning.

```clojure
; No calls to clojure.core anywhere.
(cpp/srand (cpp/time cpp/nullptr))
(if (cpp/== (cpp/int. 0) (cpp/% (cpp/rand) (cpp/int. 2)))
  (cpp/printf "Your name sings in my ears, Rand al'Thor.")
  (cpp/printf "The Wheel weaves as the Wheel wills."))
```

Here's one of the `.jank` tests for pointer arithmetic which shows off more
operator support.

```clojure

(let [i (cpp/int. 1)
      ; p is a pointer to i.
      p (cpp/& i)
      ; We're moving forward to 4 bytes after i.
      np (cpp/+ p i)
      ; Pointer comparisons.
      _ (assert (cpp/< p np))
      _ (assert (cpp/== (cpp/+ i p) np))
      ; If we move np back and deref, we get i.
      _ (assert (= 1 (cpp/* (cpp/- np i))))]
  :success)
```

Also, many (but not all) operators can be overloaded for user-defined types.
Some C++ libraries use this for more expressive code.

```clojure

(cpp/raw "struct vec2
          { float x{}, y{}; };

          vec2 operator+(vec2 const &lhs, vec2 const &rhs)
          { return { lhs.x + rhs.x, lhs.y + rhs.y }; }")
(let [; Also showing off jank's new aggregate initialization support.
      ; No need to define constructors to be able to create aggregates.
      s (cpp/vec2. (cpp/float. 1.0) (cpp/float. 2.0))
      b (cpp/vec2. (cpp/float. 4.0) (cpp/float. 3.0))
      sb (cpp/+ s b)]
  (println "s" (cpp/.-x s) (cpp/.-y s) "\n"
           "b" (cpp/.-x b) (cpp/.-y b) "\n"
           "sb" (cpp/.-x sb) (cpp/.-y sb)))
```

### Mentorship
This month, I added a fourth mentee to the jank crew. Thanks,
[Shantanu](https://github.com/shantanu-sardesai), for helping out on jank! These
guys are all doing excellent work hacking on jank. Let me showcase their current
tasks.

* Saket [has a PR up](https://github.com/jank-lang/jank/pull/331) for the first
  working version of full AOT executable building
* Monty has a working version of direct linking which will no doubt have a PR soon
* Jianling recently added big integer support and has since been improving our
  IR generation for exceptions, leading up to make IR for C++ destructors easier
* Shantanu is just getting started, but already has been improving the error
  handling in jank's parser

As a mentor in partnership with the [Scicloj mentorship program](https://scicloj.github.io/docs/community/groups/open-source-mentoring/),
I meet with each of these guys once a week, explain the inner workings of jank,
Clojure, and LLVM, and do my best to give them what they need to be compiler hackers. I'm
grateful for the passion they put into it.

### What's next?
Two months into working on seamless interop, I'm still pleased with the
progress. I don't think we'll be able to have 100% of the interop features I
want done by the end of the quarter, but we'll certainly have enough to make
jank very dangerous as a native Clojure. In the coming month, I still want to
tackle manual memory management, better template support, ensure C++'s
destructor guarantees apply to jank, stabilize the code base further with more
testing, and improve portability.

I'll have the final seamless interop update coming out in a month, so stay tuned!

## Would you like to help out?
1. Join the community on [Slack](https://clojurians.slack.com/archives/C03SRH97FDK)
2. Join the design discussions or pick up a `help-wanted` ticket on [GitHub](https://github.com/jank-lang/jank)
3. Considering becoming a [Sponsor](https://github.com/sponsors/jeaye) <span class="icon mr-1" style="color: rgb(201, 97, 152);"> <i class="gg-heart"></i></span>
4. **Better yet, reach out to discuss corporate sponsorship!**

## Sign up!
If you skipped out on the mailing list sign up earlier, give it a go now!

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
