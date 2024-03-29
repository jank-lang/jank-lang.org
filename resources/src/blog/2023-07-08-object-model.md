Title: jank development update - A faster object model
Date: Jul 08, 2023
Author: Jeaye Wilkerson
Author-Url: https://github.com/jeaye
Description: A deep dive into jank's C++ object model, the cost of polymorphism,
             and how to do better.

This quarter, my work on jank is being sponsored by [Clojurists Together](https://www.clojuriststogether.org/).
The terms of the work are to research a new object model for jank, with the goal
of making jank code faster across the board. This is a half-way report and I'm
excited to share my results!

## The problem
Before getting into any solutions, or celebrating any wins, we need to talk
about why this work is being done at all. As you can see in my previous
development updates, jank is fast. It can beat Clojure in each benchmark I've
published so far. However, some parts of jank's runtime are still quite slow and,
unfortunately, the problem is systemic.

Generally speaking, the problem can be boiled down to this: the JVM is
ridiculously fast at allocations. That's important, since Clojure is, to put it
nicely, very liberal with its allocations. jank overcomes this, in some ways, by
just allocating a whole lot less. Still, each allocation pushes Clojure ahead in
benchmarks and it adds up.

So, JVM allocations are fast, but why are jank's slow? To understand this
requires an understanding of C++ inheritance and virtual function tables
(vtables), so let's cover that at an implementation level.

## Virtual function tables
Clojure is thoroughly polymorphic. Everything is an Object, which can then have
any number of interfaces it implements, all of which can be extended, checked at
run-time, etc. To accomplish this, in C++, I modeled the objects quite closely
to how they are in Clojure's Java runtime. Let's take a look.

Let's take a stripped down jank base object:

```cpp
struct jank_object : gc
{ virtual std::string to_native_string() const = 0; };
```

Now let's define our boxed string object:

```cpp
struct jank_string : jank_object
{
  std::string to_native_string() const override
  { return data; }

  std::string data{};
};
```

This is how each object is modeled in jank, currently, and it's mostly the same as how
they are in Java. Each boxed string, hash map, vector, integer, etc inherits
from a base object and overrides some functionality. We can use `g++ -fdump-lang-class foo.cpp`
to put these sample types into a file and see the generated class hierarchy
details. The output of that is long and confusing, though, so I've turned them
into simpler diagrams. Let's take a look at `jank_string`.

<figure>
  <img src="/img/blog/2023-07-08-object-model/class-1.svg" width="300px"></img>
</figure>

So, `jank_string` is 40 bytes (8 for the `jank_object` vtable pointer + 32 for the `std::string`).
It has its own static vtable and a `vptr` to it, since it inherits from
`jank_object` and overrides a function. Whenever a `jank_string` is allocated,
these vtable pointers need to be initialized. All of this is handled by the C++
compiler, and is implementation-defined, so we don't have much control over it.

Let's take this a step further and add another behavior, since I need to
be able to get the size of a `jank_string`.

```cpp
struct jank_countable
{ virtual size_t count() const = 0; };

struct jank_string : jank_object, jank_countable
{
  std::string to_native_string() const override
  { return data; }

  size_t count() const override
  { return data.size(); }

  std::string data{};
};
```

So now we add `jank_countable` into the mix and implement that for
`jank_string`. What has this done to our vtables? Well, `jank_countable` needs
its own vtable and `jank_string` is going to need a pointer to it.

<figure>
  <img src="/img/blog/2023-07-08-object-model/class-2.svg" width="300px"></img>
</figure>

Notice that `jank_string` was 40 bytes, but now it's 48 bytes, due to the additional
pointer to the `jank_countable` vtable. It's important to note here that we
didn't just make every string we allocate larger, which may slow down
allocations, we also added another field to be initialized, which will
*certainly* slow down allocations.

I'm sure you get the point, so let me wrap this section up by noting that
Clojure's object model involves a *lot* of behaviors. Here's what jank's map
object looks like right now:

```cpp
struct map
  : object,
    behavior::seqable,
    behavior::countable,
    behavior::metadatable,
    behavior::associatively_readable,
    behavior::associatively_writable
{ /* ... */ };
```

That's *six vtable pointers* and it covers maybe half of the functionality
which Clojure's maps have. I just haven't implemented the rest yet. As I do,
jank's maps will become slower and slower to allocate.

## Garbage collectors
Before going further, I need to note that all of my Clojure benchmarking has been done
on my local Linux desktop running OpenJDK 11 with the G1 GC. jank is currently
using the Boehm GC, which is a conservative, non-moving GC that's super easy to
use, but not the fastest on the market. More on this later, but note that jank
has a lot of room to grow in terms of allocation speed by using a more tailored
GC integration.

## Initial numbers
By benchmarking the creation of non-empty hash maps (`{:a :b}` specifically), we
can paint a pretty clear picture of the issue I've been describing.

<figure>
  <object type="image/svg+xml" data="/img/blog/2023-07-08-object-model/allocations-initial.plot.svg" width="33%">
    <img src="/img/blog/2023-07-08-object-model/allocations-initial.plot.svg" width="33%"></img>
  </object>
</figure>

For Clojure, it takes about 16ns to allocate. For jank, that number is nearly doubled to
31ns. So what can be done? Clojure depends on this level of polymorphism, and
virtual functions are how you accomplish this in C++, so what else *can* we even
do?

## Static runtimes
Let's consider how a completely static runtime might be implemented. For
example, let's assume I had a simple language which only supported a few object
types, with no syntax for defining new types or protocols or even extending
existing ones. This would often be implemented using something like a
[tagged union](https://en.wikipedia.org/wiki/Tagged_union) in C-like languages. Here's a quick example:

```cpp
enum class object_type
{
  nil,
  string,
  integer
};

using nil_t = struct { };
using string_t = char const *;
using integer_t = long;

struct object
{
  /* Each object has a "tag", which is generally an enum. */
  object_type type;

  /* Each object can store one of each type, but all in the same location. */
  union
  {
    nil_t nil;
    string_t string;
    integer_t integer;
  };
};

void print(object const &o)
{
  switch(o.type)
  {
    case object_type::nil:
      fmt::print("nil");
      break;
    case object_type::string:
      fmt::print("{}", o.string);
      break;
    case object_type::integer:
      fmt::print("{}", o.integer);
      break;
  }
}
```

So, if you're not familiar how unions work, they just store all of the possible
fields listed in the union in the same memory space. The union is as big as
its largest field. The tag accompanies the union and informs you how to treat
that memory (i.e. as a `integer`, `string`, etc). In order to access data from
the union, we generally just use a `switch` statement on the tag.

The main drawback with this approach is that all possible types need to be known
at compile-time, since they're part of the enum, the union, and each switch
statement. However, the main benefit of this approach is the same. All types are
known at compile-time, so compilers have everything they need to optimize
access. There are no vtables, object allocations are all the same size, each
function call can potentially be inlined, and so on.

## A hybrid runtime
Clojure demands polymorphism, but it also has a well known set of static types. In
fact, we model most of our programs just using Clojure's built-in data
structures, so why not optimize for that case? The entirely open, polymorphic
case doesn't need to negatively impact the average case.

This reasoning lead me to prototyping and benchmarking a tagged object model for
jank. However, since jank is not a trivial language, the tagged implementation
couldn't quite be as simple as my example above. There are a few key
concerns.

### Concern 1: Unions
Unions are very limiting. Even with jank's static objects, there is a large
variety in object size. Requiring every integer, for example, to be as big as a
hash map is not ideal. Numbers need to be fast to allocate and use.

Fortunately, C++ offers a great deal more power than C when it comes to compile-time
polymorphism, in the form of templates, so we can take advantage of that. Let's
see what that looks like:

```cpp
enum class object_type
{
  nil,
  integer
};

template <object_type T>
struct static_object;

template <>
struct static_object<object_type::nil> : gc
{ };

template <>
struct static_object<object_type::integer> : gc
{ native_integer data{}; };
```

Ok, let me break this down. We start with the same enum as with the static
runtime example. Here I'm just showing `nil` and `integer`. Then, we have a new
`static_object` struct template. It's parameterized on the object type. Note
that templates can be parameterized on types as well as certain values. Here
we're parameterizing on the enum value itself. We can *specialize* this template
for each value of `object_type` and each one can be a completely distinct
struct, with its own fields. However, they're all tied together by the
combination of `static_object` and some enum value. This usage of templates is
kind of like Clojure's multi-methods, but for compile-time types.

This is much more flexible than the union approach, since each object type has
its own definition and size. The size of the integer specialization will be far
smaller than the size of the map specialization.

However, the work isn't done yet.

### Concerns 2 and 3: Type erasure and stable pointers
With the above `static_object` template, we can allocate an integer and it has
its own strong, static type. However, to achieve Clojure's polymorphism, we need
[type erasure](https://en.wikipedia.org/wiki/Type_erasure). For example, we need
to be able to store any type of object in a vector, or as a key in a map. When
using inheritance, we have a base `object` type for that. When using the union
based approach, every object fits inside of a single `object` type. However, in
our type-rich object model, each object type is discrete. We need a common way
to refer to them, while still being able to get back to the static object. On
top of that, we need a way to *unerase* the type, allowing us to get back to the
original static object. This is Concern 2.

Also, Concern 3 is that the pointers we use to hang onto these objects need to
be stable and they need to correspond with the pointers the GC gave us when we
allocated them. This is because the GC is constantly scanning the process memory
for references to those pointers; if we type-erase to some other pointer value
and hang onto that, the GC may suspect nobody is referencing the original value
anymore and take the liberty of freeing it.

We can solve both of these problems with the same addition: a simple `object`
type which contains our `object_type` enum. If every `static_object`
specialization has this `object` type as its first member, we can ensure that a
pointer to the `object` member is the same value as a pointer to the
`static_object` itself (and we can `static_assert` this to ensure padding
doesn't bite us). With that knowledge, we can reinterpret any `object` pointer
to be a `static_object` pointer, based on doing a `switch` on the object type.
Here's how it would look:

```cpp
enum class object_type
{
  nil,
  integer
};

/* An object type which contain the enum value. */
struct object
{ object_type type{}; };
using object_ptr = object*;

template <object_type T>
struct static_object;

/* Each specialization composes the object type as its first member. */
template <>
struct static_object<object_type::nil> : gc
{ object base{ object_type::nil }; };

template <>
struct static_object<object_type::integer> : gc
{
  object base{ object_type::integer };
  native_integer data{};
};

void print(object const &o)
{
  switch(o.type)
  {
    case object_type::nil:
      fmt::print("nil");
      break;
    case object_type::integer:
      /* We can cast right from the object pointer to the static_object pointer. */
      auto const typed_o(reinterpret_cast<static_object<object_type::integer> const*>(&o));
      fmt::print(typed_o->data);
      break;
  }
}
```

This is the classic composition versus inheritance change. The previous version of
jank's object model followed Clojure JVM's design of using inheritance. This new
design uses composition, by having each static object have the base `object` as
its first member.

### Concern 4: Switch statements
Imagine if we had to write a switch statement everywhere we wanted polymorphism.
In a simpler language that uses the classic tagged union approach, especially
when written in C, this would typically just be the way things work. However,
surely modern C++ has some more robust features for us to use instead? Indeed it
does.

We can get around this duplication by having the switch in only one place and
using the [visitor pattern](https://en.wikipedia.org/wiki/Visitor_pattern) to
access it. The result looks like this:

```cpp
template <typename F>
[[gnu::always_inline, gnu::flatten, gnu::hot]]
inline void visit_object(object * const erased, F &&fn)
{
  switch(erased->type)
  {
    case object_type::nil:
      fn(reinterpret_cast<static_nil*>(erased));
      break;
    case object_type::integer:
      fn(reinterpret_cast<static_integer*>(erased));
      break;
  }
}

void print(object const &o)
{
  visit_object
  (
    &o,
    /* Generic anonymous function. */
    [](auto const typed_o)
    {
      using T = std::decay_t<decltype(typed_o)>;

      if constexpr(std::same_as<T, static_nil*>)
      { fmt::print("nil"); }
      else if constexpr(std::same_as<T, static_integer*>)
      { fmt::print("{}", typed_o->data); }
    }
  );
}
```

The vistor pattern here allows us to specify a *generic lambda*, which is
basically shorthand for a function template which accepts any input. The
anonymous function will be called with the fully typed `static_object` and we
can use compile-time branching based on the type of the parameter to do the
things we want. This means the most optimal code is generated and there's static
type checking every step of the way, even in our polymorphic system.

The annotations above `visit_object` instruct the compiler to optimize all of
this away. As I will show in just a bit, this is no challenge at all. The
visitor pattern is not *at all* present in the generated binary.

I know that the `if constexpr` branching didn't save us any lines, compared to
the `switch`, in the previous example. Hang tight while we address that.

### Concern 5: Polymorphic behaviors
Finally, we hit our last concern. Objects in Clojure are polymorphic, but they
can also be referred to by their own polymorphic behaviors. For example, in
jank, we have behaviors for `countable` (for use with `count`), `associatively_readable` (which
supplies access to `get`), etc. These aren't objects on their own; they're
behaviors for objects. In typical OOP terms, they're interfaces which these
objects implement. In a world with static objects and compile-time branching to
visit them, how do we handle these behaviors?

Well, C++20 introduces an improved take on the idea of compile-time behaviors
in what it calls concepts. So, let's define a concept for getting a string from
an object. I like to end all of these behaviors with `able`, even when it
doesn't grammatically work at all, as a cheeky jab at OOP.

```cpp
template <typename T>
concept stringable = requires(T * const t)
{
  { t->to_string() } -> std::convertible_to<native_string>;
};
```

C++20 concepts are just compile-time predicates, but they're quite flexible.
This is a predicate for some type `T` that checks if you can call
`->to_string()` on an instance of it and get something compatible with a
`native_string`. This is less specific than a C++ interface which says you need
to implement something like `virtual native_string to_string() const`, since it
allows returning references to strings, or something which can convert to a
string.

Keep in mind that, while inheritance is intrusive, concepts are not. They're
just predicates for types and are not coupled to any given type. This is
analogous to the [structural typing](https://en.wikipedia.org/wiki/Structural_type_system)
versus nominal typing discussion.

If we wanted to use this in our `print` function, we could just do:

```cpp
void print(object const &o)
{
  visit_object
  (
    &o,
    [](auto const typed_o)
    {
      using T = std::decay_t<std::remove_pointer_t<decltype(typed_o)>>;
      /* Alternatively, I could `if constexpr` check here and
         do something else otherwise. */
      static_assert(stringable<T>, "Object must be stringable");

      fmt::print("{}", typed_o->to_string());
    }
  );
}
```

Finally, let's wrap up with a more real world example. In Clojure, getting the
length of a sequence can be an O(n) operation. However, some sequences may
already know their length, or have it cached. In Clojure, there's a `Counted`
interface for this; in jank, it's called `countable`. The old inheritance
version of `countable` looked like this:

```cpp
struct countable
{
  virtual ~countable() = default;
  virtual size_t count() const = 0;
};
```

The concept for it would be very similar:

```cpp
template <typename T>
concept countable = requires(T * const t)
{
  { t->count() } -> std::convertible_to<size_t>;
};
```

And we can conditionally use it when measuring a sequences length:

```cpp

size_t sequence_length(object_ptr const s)
{
  if(s == nullptr)
  { return 0; }

  visit_object
  (
    s,
    [](auto const typed_s)
    {
      using T = std::decay_t<std::remove_pointer_t<decltype(typed_s)>>;

      if constexpr(countable<T>)
      { return c->count(); }
      else
      { /* Normal O(n) code... */ }
    }
  );
}
```

## The numbers
This has been a lot of theory, but my aim here is to shed light on how these
things work. Your feedback on whether or not this is a good level of detail is
very welcome, so please reach out to me any way you can to let me know your
thoughts. Now let's celebrate some wins!

<figure>
  <object type="image/svg+xml" data="/img/blog/2023-07-08-object-model/allocations-tagged.plot.svg" width="50%">
    <img src="/img/blog/2023-07-08-object-model/allocations-tagged.plot.svg" width="50%"></img>
  </object>
</figure>

Non-empty map allocations are down from 31ns to 17ns, which is marginally higher
than Clojure's 16ns. As I mentioned, the Boehm GC isn't the fastest on the
market, so there's more work to be done here. Still, this is a huge win.
Remember that jank has been consistently beating Clojure in benchmarks *without*
these changes, so this is going to set it well ahead.

<figure>
  <object type="image/svg+xml" data="/img/blog/2023-07-08-object-model/extra-benchmarks.plot.svg" width="50%">
    <img src="/img/blog/2023-07-08-object-model/extra-benchmarks.plot.svg" width="50%"></img>
  </object>
</figure>

Map operations such as `get` and `count` were already very fast, compared to
Clojure. This is one area which allowed jank to make up for its slower
allocations. However, with the static objects, visitors, and concepts, these
benchmarks also dropped significantly.

I'm very excited to benchmark more once I've finished this work, but there's
still more to do. This is only a half-way report!

## Status
Initial prototyping and benchmarking is finished and I'm ripping apart jank's
inheritance object model to replace it with the tagged object model. This is a
large effort, which is why I wanted to get this done while jank was still young;
it changes how every jank object works. At the end of the quarter, I'll be
presenting more final numbers, as well as outlining future plans.

## Wrapping up
You may be wondering how jank will handle dynamic objects now. For those, there
will just be an enum value for dynamic, which will then rely entirely on jank
protocols for its behavior. This currently cuts off the static object model from
the dynamic object model, in the sense that the static objects rely on concepts
and not protocols. However, I think that jank can still maintain the strong Clojure
compatibility goal with this approach. ClojureScript, for example, doesn't
implement all of Clojure JVM's protocol API. With this design, jank can implement all
that ClojureScript does, which fits my overall mantra of "If it works in both Clojure and
ClojureScript, it should work in jank."

In the original announcement of this work, I noted that I would be investigating
various ECS frameworks as a way of representing objects and behaviors. While I
didn't go over them much here, I did a deep dive into them (in particular
[FLECS](https://github.com/SanderMertens/flecs)
and [EnTT](https://github.com/skypjack/entt)) and ruled them out.
The primary reason is that they cannot address Concern 3, meaning they won't
play well with garbage collection. The secondary reason would be that they would
require multiple allocations per object, since behaviors are stored separately
from entities, which is going to slow things down rather than speed them up.

Also, to those C++ devs wondering if I've just reimplemented `variant`, in some
ways you're right. I benchmarked `boost::variant` against this implementation
and it's equally fast for map `get` and `count`. However, variants are
significantly slower to allocate than a `static_object`. Allocation speeds was
the primary focus here, so I had to roll my own.

## Thanks again
As a reminder, my work on jank this quarter is sponsored by [Clojurists Together](https://www.clojuriststogether.org/).
Thank you to all of the members there who chose jank for this quarter. Thanks,
also, to all of my [Github sponsors](https://github.com/sponsors/jeaye). Your
continued support fuels jank's continued development!

I've already submitted a proposal for next quarter, to build out jank's
namespace loading, class file generating, compilation cache, and class path
handling. This leads into support for multi-file projects, leiningen
integration, and AOT compilation, so if you'd like to see this work funded,
reach out to me!

## Would you like to join in?
1. Join the community on [Slack](https://clojurians.slack.com/archives/C03SRH97FDK)
2. Join the design discussions or pick up a ticket on [GitHub](https://github.com/jank-lang/jank)
3. Considering becoming a [Sponsor](https://github.com/sponsors/jeaye) ❤️ 
4. **Hire me full-time to work on jank!**
