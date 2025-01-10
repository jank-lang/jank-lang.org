Title: I quit my job to work on my programming language
Date: Jan 10, 2025
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Description: Jeaye recounts the past 10 years working on jank
             and discusses what lead up to him quitting his
             job at EA in order to focus on jank full-time.

I did it. On Wednesday, I will hand in my badge and gun, so to speak, and
dedicate the rest of 2025 to shipping my programming language, jank. It's been a
long time coming, and actually a gradual transition, but how did we get here?

## The start of jank
Ten years ago, *to the month*, I started tinkering with programming language
design and compiler development. At that point, I was deep into C++ and my job
was focused on building game engines. I wanted to build a language which would
allow me to express safer systems which would have incredible compile-time meta
programming and also a robust mechanism for dealing with parallelism. I had run
up against the practical limits of C++'s template meta programming and the day
to day pain of trying to parallelize a relatively large monolithic in-house game
engine at work.

At this point, I began exploring many other languages. To the Rust people who
are rustling at my goals above, yes, I picked up Rust over
[12 years ago](https://github.com/jeaye/q3). It was eye opening. But I also picked up
Common Lisp, some OCaml, some Factor, some Haskell, and finally Clojure.

Around this time, I reached out to
[Dr. Christian Schafmeister](https://www.schafmeistergroup.com/chris-schafmeister) regarding
his project, [Clasp](https://github.com/clasp-developers/clasp). Clasp is a
Common Lisp implementation on LLVM. He was building something very similar to
what I wanted, except I was hoping to borrow more from Clojure than Common Lisp.
I considered potentially hopping on board with him and we discussed that
possibility, but ultimately I decided to go my own way. Still, the fact that
Clasp existed was a great source of motivation for me. It could be done.

## The next eight years
Of all the languages I tried, Clojure and Rust were the only two which had real
staying power. Both of them changed the way that I wrote code and thought about
programs. [Rich Hickey's talks](https://changelog.com/posts/rich-hickeys-greatest-hits/),
in particular, blew my mind. I rewrote jank
several times, during these eight years, with a different design each time,
trying to find something which felt right. I wanted something which married the
things I loved about C++, Clojure, and Rust. To be fair, that's a tall order,
given how different Clojure is from both C++ and Rust.

During this time, I worked more in C++ game engine development, I co-founded an
e-sports startup and built it in Clojure full-stack for 5 years, and then I
joined EA and started building web products for them in Clojure too.

All the while, I was tinkering more with jank, learning more Clojure, and
ultimately honing in on the goal that my first step should be getting Clojure
into the native world. This clarity brought the latest version of jank,
developed over the past two years, as well as a swelling jank community,
borrowed almost entirely from the Clojure community.

## The past two years
Once I knew what I wanted for jank, I needed more free time to build it. To kick
things off, I switched to part-time at EA. I worked three days a week at EA and
then had a four day jank work week. I started speaking at and attending Clojure
conferences, in the US and Europe, taking interviews on podcasts, and publishing
articles in collaboration with other tech groups. I built jank's website, this
blog, and I began building jank's community. Most of all, though, I put a huge
effort into building a native Clojure dialect on LLVM. Around 1200 commits, if
that's an interesting metric. Another metric would be around 1000 files changed,
nearly 60k lines of code changed.

jank's popularity skyrocketed.

<figure>
  <img src="/img/blog/2025-01-10-i-quit-my-job/star-history.png" width="75%" style="margin:auto;">
  </img>
  <figcaption>
  A graph of the Github stars for jank's repository over the past ten years.
  </figcaption>
</figure>

## Today
Today, I think that jank is the most popular *unreleased* Clojure project (with
[HumbleUI](https://github.com/HumbleUI/HumbleUI) coming in as a close second).
Community support and interest is teeming. I get DMs, emails, etc regularly with
people asking if jank is ready for them to use. I've spoken with founders and
tech leads of dozens of companies, at various Clojure conferences and meet ups,
who are interested in using jank to solve performance, efficiency, usability, or
interop problems with Clojure.

For the past five quarters (15 months), once per quarter, I have applied and
received funding grants from [Clojurists Together](https://www.clojuriststogether.org/).
While these don't pay the bills entirely, they do certainly help and they also further
demonstrate the community's interest in jank.

Yet, jank isn't finished. A *lot* of work remains.

I feel like I don't have enough time in the day, or in the week.

I feel like there's a ton of momentum behind jank and I don't want to lose it. I
don't want to miss my window.

I also feel like there's nothing else I'd rather be working on.

So I'm quitting my job at EA to focus on jank. 

Fortunately, I have the privilege of being able to make this decision. Yes, I'm
giving up a great job at EA. I was in a leadership role, established into a
product I founded, working part-time, while keeping my benefits. It was a
sweet deal and EA was a great employer. I'm giving that up for financial
uncertainty, no real chance at a big payout, but a whole bunch more time to work
on the thing I want to build.

I'm giving that up for more time to hack.

## My financial plans
I've been asked about what I intend to do with jank, especially
since I'm giving up my income to work on it. Put simply,
**my goal for 2025 is to get jank released and be proud of what I've built**.
I love hacking on my projects and I'm so excited for the community and momentum
I've built so far. I want to enjoy that. Rest assured that jank will always be
free and open source. I build it for me, but it's also my gift to you.

Maybe I can create enough value for the Clojure world to have a company pay me
to continue working on jank? Yes, that'd be the dream, as long as I can keep
working on my terms. But if it doesn't happen, I'll still spend the whole year
on jank. I'll still get it released. Next year, I will revisit the situation and
make a decision for 2026. Either way, I'll keep working on jank. It's been ten
years since I started this journey and I expect to be still developing jank in
another ten years, in some fashion.

In the meantime, I'll continue to guide people toward
[Github sponsorship](https://github.com/sponsors/jeaye) <span class="icon mr-1" style="color: rgb(201, 97, 152);"> <i class="gg-heart"></i></span>, I'll continue applying
for various open source grants, I'll keep building the community, and I'll keep building jank.

## The roadmap
This year, there's a ton of work to do. Here are the top ten items.

1. Better error reporting
2. Seamless C++ interop
3. Whole-project AOT compilation
4. Parity with the main Clojure core libraries
5. nREPL server support
6. Leiningen and deps.edn support
7. Packaging and distribution of jank on major Linux distros and macOS
8. Documentation
9. Huge amounts of stability and robustness testing
10. Outreach, support, and community development

I likely won't be able to finish all of these in 2025, but I will get enough
done on them to get jank released.

## Beyond Clojure
I mentioned that a native Clojure is the first step toward my dream language.
Indeed, it doesn't stop there. jank will always be a Clojure dialect, but it
will also optionally support more. Features like gradual typing (maybe linear
typing), more explicit memory management, value-based errors, and stronger pattern
matching, to name a few, improve upon Clojure. This will allow another axis of
control, where some parts of the program can remain entirely dynamic and garbage
collected while others are thoroughly controlled and better optimized. That’s
exactly the control I want when programming.

## Summary
It's been a long journey so far, but this is just the beginning. In three
years, we'll have new game engines written in jank, jank written in existing
game engines, GUI development, web services, jank support in all your favorite
libraries, WASM builds, and serious performance to top it all. It's going to be
amazing.

Thanks for joining me.

## Would you like to help out?
1. Join the community on [Slack](https://clojurians.slack.com/archives/C03SRH97FDK)
2. Join the design discussions or pick up a ticket on [GitHub](https://github.com/jank-lang/jank)
3. Considering becoming a [Sponsor](https://github.com/sponsors/jeaye) <span class="icon mr-1" style="color: rgb(201, 97, 152);"> <i class="gg-heart"></i></span>
4. **Better yet, hire me full-time to work on jank!**
