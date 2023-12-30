Title: jank's new persistent string is fast
Date: Dec 30, 2023
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Description: jank has a new C++ string class which is smaller and faster than
             std::string and folly::fbstring in benchmarks. How can that be?
Image: https://jank-lang.org/img/logo-dark.png

One thing I've been meaning to do is build a custom string class for jank. I
had some time, during the holidays, between wrapping up this quarter's work and
starting on next quarter's, so I decided to see if I could beat both `std::string`
and `folly::fbstring`, in terms of performance. After all, if we're gonna make a
string class, it'll need to be fast. :)

The back story here is that jank needs to be able to get a hash from a string,
and that hash should be cached along with the string. This means I can't use
existing string classes, since C++ doesn't have the [duck typing](https://en.wikipedia.org/wiki/Duck_typing)
mechanisms needed to add this behavior without completely wrapping the class.

Now, I could just wrap `std::string` or `folly::fbstring`, and all their
member functions but I had a couple other goals in mind, too. In particular, I
want jank's string to be persistent, as the rest of its data structures are.
Also, since I know jank is garbage collected, and the string is persistent, I
should be able to do substring operations and string copies by sharing memory,
rather than doing deep copies. To summarize these goals shortly:

## Goals
* As fast, or faster, than `std::string` and `folly::fbstring`
* Hashing, with cached value
* Immutable (i.e. no copy on substrings, writes only done in ctors, no mutators)
* I don't care about complete standard compliance (which allows me to cheat)

There are three noteworthy C++ string implementations:

* `std::string` from GCC's libstdc++
* `std::string` from LLVM's libc++
* `folly::fbstring` from Facebook's folly

Each of them uses a different memory layout and encoding scheme. GCC's string is
the simplest to understand, so I started with that. libc++'s string and
folly's string are similar, but folly takes things a step further, so I'm going
to skip over talking about the libc++ string entirely. Let's not get ahead of
ourselves, though. We're starting with GCC's string.

## libstdc++'s string
Each of these strings has some features in common, but they go about it
differently. In libstdc++'s string, the overall layout is composed of
three things:

1. A pointer to the char array
2. The length of the char array
3. The allocated capacity (which may be more than the length)

However, one commonality each of these strings have is that they employ a "small
string optimization" (SSO). SSO is a trick which avoids dynamic allocations by
storing small strings within the memory of the string class itself, up to a
certain size. To accommodate this, libstdc++'s string has a fourth member, which
is a char array of 16 bytes. However, a union is used so that the 16 byte char
array actually shares the same memory as the third member listed above, the
capacity. Depending on whether or not the string is small (based on its length),
the pointer will point at the local buffer or somewhere on the "heap" and the
capacity will either actually be the capacity or it'll be part of the memory
used to store the small string _in-situ_ (in place).

The code for this would look like:
```cpp
struct string
{
  char *data;
  size_t length;

  union
  {
    char sso[16];
    size_t capacity;
  };
};
```

This is a straightforward approach that ends up saving `sizeof(size_t)` in
memory, per string, by overlapping the capacity and the local buffer. If they
weren't overlapping, a large string (not using SSO) would have 16 bytes of
completely unused and wasted memory, which makes it slower to allocate and,
naturally, increases the memory usage of your program.

On a 64 bit system, libstdc++'s string takes up 32 bytes, half of which is used
for SSO, including a null-terminator. So that means up to 15 bytes of string
data can fit within the string without requiring an allocation. The plus side of
this is that, on a 32 bit system, the string will take up 24 bytes and you'll
still have up to 15 bytes of string data to use for SSO.

However, I have two key gripes with this design:

1. The ratio of string bytes to SSO bytes (`15:32 = 0.469`) is not great
2. The string is already quite large and I'm looking to add a cached hash to it,
   which will only make it larger

How can we do better?

## folly's string
folly's string, and libc++'s string, take a different approach which is
significantly more complex. However, the wins are impressive. So, we start with
the same three members:

1. A pointer to the char array
2. The length of the char array
3. The allocated capacity (which may be more than the length)

That's how we handle the string in the large case. However, for the small case,
we use a union over *all three* of those members, spanning the entire string. It
looks like this:

```cpp
struct string
{
  struct large_storage
  {
    char *data;
    size_t length;
    size_t capacity;
  };

  union
  {
    uint8_t bytes[sizeof(large_storage)];
    large_storage large;
  };
};
```

In the small case, we have all 24 bytes to work with. But how do we distinguish
between the small case and the large case? We'll dedicate one bit to that, in the
right-most byte of the capacity. folly actually has three cases: small, medium,
and large. It dedicates another bit, in the capacity, so if the first bit is
set, it's medium, and if the second bit is set, it's large. The drawback here,
aside from the complexity of bit twiddling, is that we've cut our capacity down
by two bits. On a 64 bit machine, that means we only can represent capacities up
to 4,611,686,018,427,387,903, instead of the full 18,446,744,073,709,551,615. Oh
well.

So folly's capacity (on a 32bit machine) would be outlined like this.

```cpp
00000000 00000000 00000000 000000??
---------------------------------^^
    /* Actual capacity data. */ |||
              /* Is it large? */ ||
              /* Is it medium? */ |
```

For a small string, both of those flag bits are 0. This is important and it's
the final piece to a puzzle: where do we store the size, for small strings?
Well, we store it in the remaining 6 bits of capacity data. But we don't just
store the size, oh no. We store the *remaining capacity* (max_size - size). This
lovely treat allows us to use that final byte as the null terminator when the
string is full, since the two flag bits will be 0 and the remaining capacity
will be 0, thus the byte will be 0.

This means folly's string allows for 23 bytes of small string data in a 24 byte
string. That's `23:34 = 0.958`, compared to the previous `15:32 = 0.469`. Our
string is 24 bytes, compared to previous 32 bytes, too! A very impressive design.

## Empty member optimization
There's a trick which all three of the string classes use called
[empty member optimization](https://www.cantrip.org/emptyopt.html) and I'll
explain it because it's another example of how crazy C++ is. In C++, an empty
struct can't have the size of 0. It generally has the size of 1. This is
important for addressing, as I'll show here.

```cpp
struct empty
{ };

struct foo
{
  empty e;
  char *p;
};
```

In this example, if `empty` had a size of 0, then both `e` and `p` would have
the same address within a `foo` instance. That's not the case. Actually, it's
far more complicated. Since `empty` has the size of 1, so does `e`, but since a
machine word is generally larger (i.e. 64 bits), the space between `e` and `p`
is filled with *padding*. On my 64 bit system, `e` is 1 byte, followed by 7
bytes of padding, followed by 8 bytes for `p`. The total size of `foo` is 16
bytes, even though it holds just a single 8 byte pointer.

Why does this matter?

Containers like strings each have an allocator type, which is generally
customizable. Most, but not all, allocators are stateless, empty structs, which
differ only in their function behavior. The containers need an instance of the
allocator in order to be able to use it and they can't make assumptions about
what state is in there. But who wants to pay for all of that padding for an
empty allocator? Not me. Not the C++ standard library authors.

Fortunately, C++ allows for base classes to be empty, without affecting the size
of the derived class. So, we wrap one of our members in a class, inherit from
our allocator, and we don't actually need to pay any space or runtime cost for
an empty allocator. In folly's string, it looks something like this:

```cpp
struct string
{
  /* We wrap our union in a struct and inherit from
     our allocator. */
  struct storage : allocator_type
  {
    union
    {
      uint8_t bytes[sizeof(large_storage)];
      large_storage large;
    };
  };

  /* This will be the same size as the union alone, assuming
     an empty allocator. */
  storage store;
};
```

I had never seen this done, until I was studying all three of these string
implementations, and each one of them does it in their own way, but it all boils
down to the same trick. Fascinating!

## jank's string
Ok, all of this culminates to jank getting a new string. Let's get hacking.
jank has no need for a distinction between small, medium, and large, like folly
has. Just small and large will work. It also has no need for separating size and
capacity, since it will only ever hold as much as it needs. Small strings will
be stored in-situ and large strings will be GC-allocated. All strings will be
immutable.

Now, since all strings are immutable, and all string data is GC allocated, large
strings could actually share memory trivially. So, we'd have three cases:

1. Small string (all data is in the string itself)
2. Owned large string (GC allocated, but able to be deallocated in the destructor)
3. Shared large string (GC allocated, no deallocation -- let the GC handle it)

We need a way of marking which large strings are owned, though. If only there
was a bit, laying around, which we could use... oh wait, folly was using two
bits for medium/large and we're only using one. So we can repurpose that second
bit like so:
```cpp
00000000 00000000 00000000 000000??
---------------------------------^^
    /* Actual capacity data. */ |||
              /* Is it owned? */ ||
               /* Is it large? */ |
```

Now this allows us to do some very nice optimizations:

1. Copy constructors are practically free, for both small and large cases
2. Substring operations are practically free as well

### Substring trickery

Alas, there's one big caveat with this approach: shared substrings of larger
strings may not be null-terminated! For example, if I have this string:

```text
Life before death. Strength before weakness. Journey before destination.
```

This is 72 bytes, so definitely a large string. But what if I take a substring
of just the second sentence:

```text
Strength before weakness.
```

Since we're sharing the memory with the larger string, there is no
null-terminator there. This is important, since if we use `c_str()` or `data()`
on our string, the returned C string pointer won't "stop" until the null, which
will mean our string will appear to be this:

```text
Strength before weakness. Journey before destination.
```

So, since I don't care about 100% standard compliance, but I still want the API
to make sense, I compromised here that `data()` doesn't need to return a
null-terminated string, but that `c_str()` always will. I handle this by
checking if it's shared and then lazily switching to being owned by allocating
and copying the data. In the vast majority of the cases, within jank's code,
`data()` is sufficient, since we also have the `size()`. We don't need to go
looking for nulls. However, compatibility with the C string world is important
so we meet in the middle.

## The results
I implemented my string from scratch, using benchmark-driven development, taking
the best of each of the strings I studied along the way. The entire string is
`constexpr`, which required a [change](https://github.com/ivmai/bdwgc/pull/603)
to the Boehm GC's C++ allocator. Ultimately, I'm quite pleased with the results.
Between just folly's string and libstdc++'s string, one of them is generally the
clear winner in each benchmark, with folly's more complex encoding (and smaller
size) making it slower for some large string operations. However, with jank's
string, we have the best of both worlds. It's just as small as folly's string,
but it either ties or outperforms the fastest in every benchmark. On top of
that, it packs another word for the cached hash! Finally, the data sharing for
copy
construction and substrings leave the other strings in the dust. Take a look!

<figure>
  <object type="image/svg+xml" data="/img/blog/2023-12-30-fast-string/allocations.plot.svg" width="50%">
    <img src="/img/blog/2023-12-30-fast-string/allocations.plot.svg" width="50%"></img>
  </object>
  <figcaption>
  jank constructs small strings the fastest and ties with std::string for large strings.
  </figcaption>
</figure>

<figure>
  <object type="image/svg+xml" data="/img/blog/2023-12-30-fast-string/copy.plot.svg" width="50%">
    <img src="/img/blog/2023-12-30-fast-string/copy.plot.svg" width="50%"></img>
  </object>
  <figcaption>
  jank ties with folly for copying small strings and seriously beats both when
  copying large strings.
  </figcaption>
</figure>

<figure>
  <object type="image/svg+xml" data="/img/blog/2023-12-30-fast-string/find.plot.svg" width="50%">
    <img src="/img/blog/2023-12-30-fast-string/find.plot.svg" width="50%"></img>
  </object>
  <figcaption>
  jank ties with std::string for large and small string searches.
  </figcaption>
</figure>

<figure>
  <object type="image/svg+xml" data="/img/blog/2023-12-30-fast-string/substr.plot.svg" width="50%">
    <img src="/img/blog/2023-12-30-fast-string/substr.plot.svg" width="50%"></img>
  </object>
  <figcaption>
  jank ties with folly for small substrings and seriously beats both when
  creating large substrings.
  </figcaption>
</figure>

The benchmark source, which uses nanobench, can be found
[here](https://gist.github.com/jeaye/306d6aefd7ed6c29fdec6eef2cafbb1f).

## Is sharing large strings a big deal?
This is easy to quantify. When compiling `clojure.core`, with jank, we end up
sharing 3,112 large strings. That's 3,112 large string deep copies, and just as
many allocations, which we can completely elide. In the span of a larger
application, we'll be talking about millions of allocations and deep string
copies elided. It's fantastic!

## Wrapping up
jank now has a persistent string which is tailored for how Clojure
programs work. It shares data, reduces allocations for strings all the way up to
23 bytes (which fits most keywords, I'd bet), and supports fast, memoized
hashing. Going forward, I'll be exploring whether keeping that hash around is
worth the 8 bytes, but I'm thinking it is and I'd rather bite the bullet for it
now than have to add it later. When string building is needed, I've aliased a
very capable transient string type called `std::string`, which you can get
to/from a persistent string easily.

There's a lot more detail I could go into about how I made these improvements,
to take folly's string design and make it as fast, or faster, than libstdc++'s
string in every benchmark. I optimized aspects of data locality, write ordering,
branch elimination, tricks to enable `constexpr` even for complex code (like
`reinterpret_cast`), etc. If you're interested in even more detail in these
areas, let me know!

## Would you like to join in?
1. Join the community on [Slack](https://clojurians.slack.com/archives/C03SRH97FDK)
2. Join the design discussions or pick up a ticket on [GitHub](https://github.com/jank-lang/jank)
3. Considering becoming a [Sponsor](https://github.com/sponsors/jeaye)
