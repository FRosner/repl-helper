## REPL Helper [![Build Status](https://travis-ci.org/FRosner/repl-helper.svg?branch=master)](https://travis-ci.org/FRosner/repl-helper) [![Coverage Status](https://coveralls.io/repos/FRosner/repl-helper/badge.svg?branch=master&service=github)](https://coveralls.io/github/FRosner/repl-helper?branch=master)

### Description

Small library allowing to provide annotation based help text for functions in the Scala REPL. Easily implement a help method in your REPL application by writing help texts into method annotations.

### Get It

In order to use the REPL helper, you can add it as a dependency to your project using [JitPack.io](https://jitpack.io/#FRosner/repl-helper). Just add it to your `build.sbt` like this:

```scala
resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += "com.github.FRosner" % "repl-helper" % "x.y.z"
```

### Use It

Define the `@Help` annotation on your method like so:

```scala
object MyReplUtil {

  @Help(
    category = "Math",
    shortDescription = "Add two numbers",
    longDescription = "Add two numbers. Integer overflow might occur!",
    parameters = "a: Int, b: Int"
  )
  def add(a: Int, b: Int) = a + b

}
```

Create a helper instance of your class:

```scala
val myHelper = Helper(MyReplUtil.getClass)
```

Print help to a `PrintStream` of your choice. You can either print all available methods or request detailed help for a particular one:

```scala
myHelper.printAllMethods(System.out)
myHelper.printMethods("add", System.out)
```
