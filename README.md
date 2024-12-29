# URL DSL

This is a tiny library for parsing and generating paths and parameters of urls.

## Getting started

We represent the path and query parameters of a url as follows:

```scala
import urldsl.language.simpleErrorImpl._
import urldsl.vocabulary.{Segment, Param, UrlMatching}

val path = root / "hello" / segment[Int] / segment[String]
val params = param[Int]("age") & listParam[String]("drinks")

val pathWithParams = path ? params

pathWithParams.matchRawUrl(
  "http://localhost:8080/hello/2019/january?age=10&drinks=orange+juice&drinks=water"
) should be(
  Right(UrlMatching((2019, "january"), (10, List("orange juice", "water"))))
)

path.matchPath("/hello/2019/january") should be(
  Right((2019, "january"))
)

params.matchQueryString("age=22&drinks=orange+juice&drinks=water") should be(
  Right((22, List("orange juice", "water")))
)
```

For more example usages, head over the tests, and in particular in the `urldsl.examples` package..

## Installation

Add the following to your `build.sbt` (or wherever you add your dependencies):

```scala
libraryDependencies += "be.doeraene" %% "url-dsl" % "0.4.0"
```

or, for Scala.js,

```scala
libraryDependencies += "be.doeraene" %%% "url-dsl" % "0.4.0"
```

## The biggest abstraction

The highest abstraction that this library provide is the [UrlPart](shared/src/main/scala/urldsl/language/UrlPart.scala). A `UrlPart` represents a part of the URL and will be able to:

- when applied to a URL, tries to extract information from that URL
- when fed with information, generates a string that this piece of information represents.

The part that the `UrlPart` represents can be:

- the path of the URL
- the query of the URL
- the fragment of the URL
- a combination of any of the above.

This is a trait with two type parameters, `T` and `E`. The type `T` is the type of information represented in this `UrlPart`. For example, if `T =:= (String, Int)`, it means that we will get a pair when extracting information from a URL, and we will need to feed that when generating the string part of the URL.

Matching a URL can fail. When it does, it will produce an element of type `E`. `E` can be any ADT of your choosing, however url-dsl comes with two default implementations, one particularly suited for debugging, and the other when you don't care about what error was produced (but you only care to know whether the test was successful).

## The three important classes

There are essentially three classes that are important for using `urldsl` (plus two others when you combine them), all of them extending `UrlPart`. These three classes are

- [PathSegment](shared/src/main/scala/urldsl/language/PathSegment.scala): abstraction describinng the path part of the URL
- [QueryParameters](shared/src/main/scala/urldsl/language/QueryParameters.scala): abstraction describing the query (search) part of the URL
- [Fragment](shared/src/main/scala/urldsl/language/Fragment.scala): abstraction describing the fragment (ref) part of the URL

As said above, these classes come with two type parameters, one for the information, and one for the possible matching error. For example, in the case of the path, if `T =:= (String, Int)`, the path contains the information about a `String` and an `Int`, as in the following segment

```scala
root / segment[String] / "foo" / segment[Int]
```

### Path

The path part of the URL is modelled by the `urldsl.language.PathSegment[T, A]` trait. At its core, a `PathSegment` is
merely an object with two methods `matchRawUrl` and `createPath`. The `matchRawUrl` method takes as input the string
containing a URL (well formed!) and returns `Either` an instance of `T` (the information contained in the input URL)
or an error of type `A` (if the segment could not retrieve the information).

`PathSegment`s are immutable objects that can be composed together with the `/` operator. This operator is associative
and creates, given two `PathSegment`s with type parameters `T` and `U`, will create a `PathSegment` with type parameter,
roughly, `(T, U)` (with some additional rules described in the `Composition` class that, without entering details, flattens
the tuples and removes `Unit`s). So if `T =:= (String, Int)` and `U =:= (String, Double)`, you'll get the type
`(String, Int, String, Double)`.

When a `PathSegment` matches a URL, it internally receives the list of `Segment`s, consumes one or several of them, and
passes the rest onto the following (when there are composed). For example, if you have a `PathSegment` that matches the
string "foo", and an other that matches an `Int`, if you give the composition "foo/22", the first `PathSegment` consumes
"foo" and passes "22" to the next one, which will them consume it.

> Important (since 0.7.0 💥): the `PathSegment` has to fully match the path in order to succeed. If there is a left over segment after, then it will not match. You can use `ignoreRemainingSegments` if you want to only match the "beginning" of the path.

#### Built in path segments

There are a bunch of `PathSegment`s that are already defined, and should satisfy most of your basic needs. For example,
the following things are implemented (you can look at the companion object of `PathSegment` to have the comprehensive
list):

- `root`: matches everything and passes all segments onto the next
- `segment[T]` matches an element of type `T`, whose information is contained in only one segment, and passes the other
  segments onto the next
- `remainingSegments`: consumes all the remaining segments, returning the list
- `ignoreRemainingSegments`: consumes all the remaining segments and ignore their content
- the list goes on...

#### Examples

Comprehensive examples can be found in [here](shared/src/test/scala/urldsl/examples/PathSegmentExamples.scala). Below we give a quick overview.

The following examples assume the following import:

```scala
import urldsl.language.PathSegment.simplePathErrorImpl._
```

Here are a bunch of things that you can do with the paths:

```scala
(root / "home" / "about").matchRawUrl("http://localhost:8080/home/about") // success, returns Unit
(root / "home" / "about").matchRawUrl("http://localhost:8080/home") // failure, returns MissingSegment

(root / segment[String] / segment[Int]).matchRawUrl("http://www.google.be/user/22") // success, returns ("user", 22)
(root / segment[String] / segment[Int]).matchRawUrl("http://www.google.be/user/foo") // failure, returns SimpleError

(root / "home").matchRawUrl("http://scala-lang.org/about") // failure, returns WrongValue("home", "about")

case class User(id: Int, name: String)

object User {
  implicit val userCodec: Codec[(Int, String), User] = new Codec[(Int, String), User] {
    def leftToRight(left: (Int, String)): User = User(left._1, left._2)
    def rightToLeft(right: User): (Int, String) = (right.id, right.name)
  }
}

val userPath = (root / "user" / segment[Int] / segment[String]).as[User]

userPath.matchRawUrl("http://scala-lang.org/user/5/Alice") // success, returns User(5, "Alice")
userPath.createPath(User(5, "Alice")) // returns user/5/Alice
```

Note that starting with `root` is not strictly necessary (technically, `root` is the neutral of the `/` operator) but
it allows to use the implicit conversion from elements to single segment matching. Also, it's good to really interpret
it as the beginning of the path.

### Query parameters

The query parameters part of the URL is modelled by the `urldsl.language.QueryParameters[Q, A]`. As for the
`PathSegment` class, this is essentially a class that has two methods `matchRawUrl` and `createParams`. Analogously to
the `/` operator of path segments, the query parameters have an operator `&` to compose them and build more complicated
query parameters.

The tupling of the type parameters works the same way as for path.

#### Built in query parameters

There are two main built in query parameters that you can use as building blocks for most of yours needs:

- `param[T](paramName: String)`: represents the value of the parameter with name `paramName` as a type `T` element
- `param[T](paramName: String).?`: optional param, with value encoded as `Option[T]`
- `listParam[T](paramName: Strig)`: same as `param` but for lists.

#### Query parameters examples

Comprehensive examples can be found in [here](shared/src/test/scala/urldsl/examples/QueryParamsExamples.scala). Below we give a quick overview.

The following examples assume the following import:

```scala
import urldsl.language.QueryParameters.simpleParamErrorImpl._
```

Here are a bunch of things that you can do with the query parameters:

```scala
(param[String]("foo") & param[Int]("bar")).matchRawUrl(
  "http://localhost:8080/home?foo=hello&bar=3"
) // returns ("hello", 3)
(param[Int]("bar") & param[String]("foo")).matchRawUrl(
  "http://localhost:8080/home?foo=hello&bar=3"
) // returns (3, "hello")

(listParam[Int]("numbers")).matchRawUrl(
  "http://localhost:8080/home?numbers=1&numbers=2"
) // returns List(1, 2) (however you should assume that it could be List(2, 1) in a non predictible way)
```

#### Note on commutativity of &

The significant difference between `/` for paths and `&` for parameters is that `&` is commutative. That is, the string
"foo=hello&bar=3" is equivalent to "bar=3&foo=hello" (this is obviously not the case for `/`). The `&` operator of
`QueryParameters` is however _not_ commutative, but it essentially is (we say that it is quasi-commutative).

Let's expose what this means. Suppose we have two instances `foo` and `bar` respectively of types
`QueryParameters[String, A]` and `QueryParameters[Int, A]`. Then `foo & bar` and `bar & foo` will both match the two
strings above, but the first one has type `QueryParameters[(String, Int), A]`, while the other one has type
`QueryParameters[(Int, String), A]`.

### Fragment

The last class is `urldsl.language.Fragment`. It represents the fragment (sometimes called ref) part of the URL (which is the part after "#" at the end).

The fragment part is very basic compared to the other two. It is either present or absent, and you can invoke it with

```scala
import urldsl.language.simpleErrorImpl._

fragment[String] // extract the fragment information when it's there, failing otherwise
fragment[Int]    // extract the fragment information when it's there, casting to int, failing otherwise

maybeFragment[String] // extract the fragment information wrapped in Some when it's there, or None otherwise (never failing)
empty                 // imposes that the fragment is absent, failing otherwise
```

#### Examples for Fragment

Head over this [test file](shared/src/test/scala/urldsl/examples/FragmentExamples.scala).

## Error mechanism

Both `PathSegment`s and `QueryParameters` contain the type information of their content and the type information of the
errors they return. This allows the user to define its own ADT of errors that can be used to easily manage the errors
if needed.

There are however two kind of errors that are built in, see below.

### Simple errors

The first kind of built in errors are the `SimplePathMatchingError` and `SimpleParamMatchingError` implementations.
These simple errors form a basic error system that contains error message as strings. The (probably) best usage of these
errors is to debug and begin to play with the library.

In order to use the simple errors, you need the following imports:

```scala
import urldsl.language.PathSegment.simplePathErrorImpl._
import urldsl.language.QueryParameters.simpleParamErrorImpl._
```

### Custom errors

If you want to create your own ADT for errors, you need to do two things:

- first, you need an implementation of the `urldsl.errors.PathMatchingError` and `urldsl.errors.ParamMatchingError` type
  classes, that you can set implicit for ease with the following
- second, you create an instance of `urldsl.language.PathSegmentImpl` and `urldsl.language.QueryParametersImpl` simply
  by use their constructor

Implementing these two type classes simply requires you to give a concrete implementation to some special errors that
are needed for the default paths and segments built in helpers.

Then, wherever you want to use the library, you should import the contents of your implimentation, as shown in the
"Simple errors" section above.

### `DummyError`

What about if you simply want to know whether something matches, but don't really care about the reason why? This would
for example be the case when implementing a Router.

For that scenario, there is a type of error, called `DummyError` that only has one instance, which is returned for
every failure.

In some methods, this error type also adds sugar on some methods (see, e.g., the `filter` method of `PathSegment`).

In order to use the dummy error, you need the following imports:

```scala
import urldsl.language.dummyErrorImpl._
```

## A router example

url-dsl was thought with one possible goal in mind, the one of creating a routing system. An example of how to do that can be found [here](shared/src/test/scala/urldsl/examples/RouterUseCaseExample.scala).

## Moving from 0.6.x to 0.7.x

There is a breaking change introduced in 0.7.0. If a `PathSegment` only partially match the path, then it will fail. If you were relying on this behaviour, you need to add `/ ignoreRemainingSegments` to your `PathSegment` instances.

## Moving from 0.4.x to 0.5.x

In 0.5.0 we replaced our `Tupler` with the `Composition` type from the [tuplez](https://github.com/tulz-app/tuplez) library. It works exactly the same for most use cases, but if your data types are complex enough that URL-DSL previously gave you nested tuples, those tuples will generally be flattened now.

## Moving from 0.2.0 to 0.3.x

If you come from version 0.2.0, here are a few things that you should pay attention to.

### The new `UrlPart` trait

If you need/want to abstract over all possible way to extract information from a URL, this is the way to go. Asking your user to give a `UrlPart[T, E]` will allow them to provide any of the above mentioned classes, as well as their combinations.

### Import all syntax

Previously, if you needed to mix `PathSegment` and `QueryParameters`, you had to make the two imports:

```
import urldsl.language.QueryParameters.simpleParamErrorImpl._
import urldsl.language.PathSegment.simplePathErrorImpl._
```

With the addition of the fragment feature, we decided to create an instance for importing everythin at once:

```
import urldsl.language.simpleErrorImpl._
// or
import urldsl.language.dummyErrorImpl._
```

### Error type is now covariant

This should not impact you very much.

## Internal

The project is decomposed in four packages:

- `url`: it contains parsing and rendering of urls (with encodings)
- `vocabulary`: it contains the little blocks on which the dsl is built
- `language`: it contains the actual implementation of the dsl
- `errors`: it contains the implementation of the matching errors logic.

In order to flatten the tuples that are generated by the operators `/` and `&`, we use the `Tupler` mechanism built by
@julienrf and orginiated from [here](https://github.com/julienrf/endpoints/blob/7d0af49bc7a83b5985815d936201616ed8a3fa5d/json-schema/json-schema/src/main/scala/endpoints/Tupler.scala).
