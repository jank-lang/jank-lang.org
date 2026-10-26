Title: The road to jank beta
Date: Oct 27, 2026
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Description: There's a new jank milestone we're targeting and you can help us
             get there!
Draft: true

As I mentioned at Clojure Conj 2026, the next milestone for jank is a beta
release. I'm targeting January 2027. In this post, I'd like to spell out exactly
what that release will entail and how we'll get there. The beta release has four
priorities, in no particular order:

* Stability
* Portability
* Parity
* Usability

The progress of all of these is being tracked by this [Github milestone](https://github.com/jank-lang/jank/milestone/2).

## Stability
jank still has many bugs and we're not going to catch them all prior to the beta
release, but we do want to catch the most obvious and impactful bugs. Over the
past couple of years, we've added a ton of new functionality to jank and we need
to ensure it works reliably. We have great test coverage for most systems, but we
still need to fill in the gaps and address the systems which are lacking
coverage. The main targets here are:

* Core Clojure libs (as tested by jank's [clojure-test-suite](https://github.com/jank-lang/clojure-test-suite) -- help wanted!)
* Various C++ interop edge cases
* nREPL and native build system tooling

## Portability
We have jank packages for macOS Homebrew (`arm64`), Ubuntu (`x86_64`), Arch
(`x86_64`), and Nix (`x86_64`). Each of these packages is bleeding edge, since jank
doesn't yet have versioned releases. The biggest change here for beta will be
that jank releases will become versioned and immutable. The existing bleeding
ege jank packages will be renamed to jank-dev and the default jank packages will
track versioned releases. Releases will happen at whatever cadence feels right.

I'd like to grow the package area to include Homebrew packages for Linux
`x86_64` and macOS `x86_64`, as well as statically linked Alpine packages and
[Windows `x86_64` MSYS2](https://github.com/jank-lang/jank/pull/945). If you have
an OS where you'd like to see jank, let me know!

Part of portability is to improve jank's embedding story. This involves filling
out jank's C API, improving error handling, and addressing the common gotchas
around embedding an LLVM-based JIT runtime.

## Parity
Some areas of Clojure parity, such as protocols and records, have yet to be
tackled. We're also missing a couple of dozen key vars in `clojure.core`, like
`rseq`, unchecked arithmetic, etc. For the beta release, I want to achieve a
strong parity with upstream Clojure JVM and track it in terms of Clojure's
versioned releases. We should be able to say that a particular version of jank
tracks a particular version of Clojure JVM.

On the C++ interop side, we need more parity with how we handle native arrays,
pointer decays, and initializer lists. I also want to improve our feature set
for C++ interop to allow for defining statically typed C++ functions, globals,
and structs within jank code.

## Usability
As part of the beta release, the jank website will be updated to include starter
examples for game development, GUI apps, and terminal apps. Part of this will
include more packaged native libraries in [awesome-jank](https://github.com/jank-lang/awesome-jank).
I will also focus more on implementing guides and references for Clojure devs to
onboard into jank land, understand the interop, jank's type system, and start
building an intuition for how jank's host systems work.

## Performance
It's intentionally **not a goal** to improve jank's performance for the beta
release. There are plenty of areas in jank to optimize and plenty of benchmarks
where jank will not do favorably against Clojure JVM, but I will be focusing on
those next year, leading up to jank's gamma release. Don't worry! When I do
focus on performance, we're going to make some huge gains. I'm already prepared
for many of them, but they're simply not a priority right now compared to
everything else.

# How's the progress so far?
If you check out the milestone right now, you'll see that we're around 50% of
the way through already. Thanks to the help of [Chris Badahdah](https://github.com/djblue),
of [Portal](https://github.com/djblue/portal) fame, we've been melting through
the bug list at quite a good clip. I'm expecting, and hoping, that we'll get more
bug reports in the coming weeks. That's exactly where folks like you come in...

# How can I help?
I'm glad you asked! The best way you can help is to [install jank](https://book.jank-lang.org/getting-started/01-installation.html)
and write some software in it! For any friction points or bugs you find along
the way, please keep notes and then share your experience with me on Github,
Slack, Discord, or email.

Speaking of which, did you know that there's a 10 day long
[Lisp Game Jam](https://itch.io/jam/autumn-lisp-game-jam-2026) happening this
week? I'll be participating and I encourage anyone interested in jank to do the
same. We already have handy packages for raylib, imgui, box2d/3d, and more, in 
[awesome-jank](https://github.com/jank-lang/awesome-jank). This is the exact
sort of thing jank needs: a good excuse for people to try it and produce
experience reports, bug reports, and success stories. If you're going to
participate in the jam, let me know! I'd like to gather all of the jank games at
the end and make a showcase. **Anyone can participate. No game dev experience is required.**
If you need inspiration, check out our [flappy bird](https://github.com/jank-lang/jank/tree/main/example/gui/flappy-bird)
example.

Catch me [on stream](https://www.twitch.tv/jeayewilkerson) during the game jam,
if you want to see some jank coding in action.

# Summary
So, in summary, the next milestone is a beta release, targeted for January 2027.
We're going to focus on portability, stability, parity, and usability. We need
all of you to try jank out and report issues in order to do a great job of that.
The game jam this week is an excellent excuse to kick the tires. Please file a
Github issue if the tires fall off!

I want to say thank you to my
[Github sponsors](https://github.com/sponsors/jeaye) and to Clojurists Together for
sponsoring me this whole year. Thank you!

# Try the jank mailing list
I know, this post could've been an email. It actually was! Please consider
subscribing to jank's mailing list. It's a great way to make sure you stay up
to date with jank's releases, jank-related talks, workshops, and so on. It's
very low traffic.

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


# Would you like to join in?
1. Join the community on [Slack](https://clojurians.net) or [Discord](https://discord.gg/7sSMfKDBU3)
2. Join the design discussions or pick up a ticket on [GitHub](https://github.com/jank-lang/jank)
3. Considering becoming a [Sponsor](https://github.com/sponsors/jeaye) <span class="icon mr-1" style="color: rgb(201, 97, 152);"> <i class="gg-heart"></i></span>
4. **Hire me to work on jank!**
