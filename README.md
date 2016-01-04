## REPL Helper [![Build Status](https://travis-ci.org/FRosner/repl-helper.svg?branch=master)](https://travis-ci.org/FRosner/repl-helper) [![Codacy Badge](https://api.codacy.com/project/badge/grade/ee24c8670c944d3aab3d2008206df0c7)](https://www.codacy.com/app/frank_7/repl-helper) [![codecov.io](https://codecov.io/github/FRosner/repl-helper/coverage.svg?branch=master)](https://codecov.io/github/FRosner/repl-helper?branch=master)

### What is it?

Small library allowing to provide annotation based help text for functions in the Scala REPL. Easily implement a help method in your REPL application by writing help texts into method annotations.

### Get It!

In order to use the REPL helper, you can add it as a dependency to your project using [JitPack.io](https://jitpack.io/#FRosner/repl-helper). Just add it to your `build.sbt` like this:

```scala
resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += "com.github.FRosner" % "repl-helper" % "x.y.z"
```

### Use It!

1. Define the `@Help` annotation on your methods.

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

2. Create a helper instance of your class.

   ```scala
   val myHelper = Helper(MyReplUtil.getClass)
   ```

3. Print help to a `PrintStream` of your choice. You can either print all available methods or request detailed help for a particular one.

   ```scala
   myHelper.printAllMethods(System.out)
   myHelper.printMethods("add", System.out)
   ```
