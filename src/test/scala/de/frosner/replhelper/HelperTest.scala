package de.frosner.replhelper

import java.io.{ByteArrayOutputStream, PrintStream}

import org.scalatest.{FlatSpec, Matchers}
import Helper.NEWLINE

class HelperTest extends FlatSpec with Matchers {
  
  class TestClass {
    def noHelp = ???

    @Help(
      category = "a",
      shortDescription = "short help",
      longDescription = "long help"
    )
    def help = ???

    @Help(
      category = "a",
      shortDescription = "short help",
      longDescription = "long help"
    )
    def xhelp = ???

    @Help(
      category = "bbb",
      shortDescription = "sph",
      longDescription = "long parameter help",
      parameters = "i: Integer",
      parameters2 = "s: String"
    )
    def helpWithParameters(i: Integer) = ???
  }

  object TestClass {
    val categoryA = "a"
    val categoryB = "bbb"
    val name = classOf[TestClass].getSimpleName

    val expectedShortOutput = Array(
      s"${Console.BOLD}$categoryA${Console.RESET} [$name]",
      "- help(): short help",
      "- xhelp(): short help",
      "",
      s"${Console.BOLD}$categoryB${Console.RESET} [$name]",
      "- helpWithParameters(i: Integer)(s: String): sph",
      ""
    )
  }

  class TestClass2 {
    @Help(
      category = "category",
      shortDescription = "without parameters",
      longDescription = "method without parameters"
    )
    def method = ???
    @Help(
      category = "category",
      shortDescription = "with parameters",
      longDescription = "method with one parameter",
      parameters = "s: String"
    )
    def method(s: String) = ???
  }

  object TestClass2 {
    val category = "category"
    val name = classOf[TestClass2].getSimpleName

    val expectedShortOutput = Array(
      s"${Console.BOLD}$category${Console.RESET} [$name]",
      "- method(): without parameters",
      "- method(s: String): with parameters",
      ""
    )

    val expectedLongOutput = Array(
      s"${Console.BOLD}method()${Console.RESET} [$name]",
      "method without parameters",
      "",
      s"${Console.BOLD}method(s: String)${Console.RESET} [$name]",
      "method with one parameter",
      ""
    )
  }

  class TestClass3 {
    @Help(
      category = "category",
      shortDescription = "short",
      longDescription = "long",
      parameters = "1",
      parameters2 = "2",
      parameters3 = "3",
      parameters4 = "4",
      parameters5 = "5",
      parameters6 = "6",
      parameters7 = "7",
      parameters8 = "8",
      parameters9 = "9"
    )
    def method = ???
  }

  object TestClass3 {
    val name = classOf[TestClass3].getSimpleName
    val category = "category"
  }

  "A helper" should "offer only help for methods with the correct annotation" in {
    val testClass = classOf[TestClass]
    val helper = Helper(testClass)

    val expectedHelpMethod = testClass.getMethod("help")
    val expectedHelpAnnotation = expectedHelpMethod.getAnnotation(HelpAnnotationClassUtil.getHelpAnnotationClass)

    val expectedXHelpMethod = testClass.getMethod("xhelp")
    val expectedXHelpAnnotation = expectedXHelpMethod.getAnnotation(HelpAnnotationClassUtil.getHelpAnnotationClass)

    val expectedHelpWithParametersMethod = testClass.getMethod("helpWithParameters", classOf[Integer])
    val expectedHelpWithParametersAnnotation = expectedHelpWithParametersMethod.getAnnotation(HelpAnnotationClassUtil.getHelpAnnotationClass)

    helper.methods shouldBe Seq(
      (TestClass.name, TestClass.categoryA) -> Seq(
        (expectedHelpMethod, expectedHelpAnnotation),
        (expectedXHelpMethod, expectedXHelpAnnotation)
      ),
      (TestClass.name, TestClass.categoryB) -> Seq(
        (expectedHelpWithParametersMethod, expectedHelpWithParametersAnnotation)
      )
    )
  }

  class A {
    @Help(
      category = "category",
      shortDescription = "",
      longDescription = ""
    )
    def method = ???
  }

  object A {
    val name = classOf[A].getSimpleName
    val category = "category"
  }

  class B {
    @Help(
      category = "category",
      shortDescription = "",
      longDescription = ""
    )
    def method = ???
  }

  object B {
    val name = classOf[B].getSimpleName
    val category = "category"
  }

  it should "group correctly based on category and class" in {
    val aClass = classOf[A]
    val bClass = classOf[B]
    val helper = Helper(aClass, bClass)

    val expectedAMethod = aClass.getMethod("method")
    val expectedAAnnotation = expectedAMethod.getAnnotation(HelpAnnotationClassUtil.getHelpAnnotationClass)
    val expectedBMethod = bClass.getMethod("method")
    val expectedBAnnotation = expectedBMethod.getAnnotation(HelpAnnotationClassUtil.getHelpAnnotationClass)

    helper.methods shouldBe Seq(
      (A.name, A.category) -> Seq(
        (expectedAMethod, expectedAAnnotation)
      ),
      (B.name, B.category) -> Seq(
        (expectedBMethod, expectedBAnnotation)
      )
    )
  }

  it should "take all methods when no classes are specified" in {
    val helper = Helper()
    helper.methods.map{ case (key, value) => key }.toSet shouldBe Set(
      (TestClass.name, TestClass.categoryA),
      (TestClass.name, TestClass.categoryB),
      (TestClass2.name, TestClass2.category),
      (TestClass3.name, TestClass3.category),
      (DummyObjectThatIsNoCompanion.name, DummyObjectThatIsNoCompanion.category),
      (A.name, A.category),
      (B.name, B.category)
    )
  }

  "When all methods are requested, it" should "show the short description for a single class" in {
    val result = new ByteArrayOutputStream()
    val out = new PrintStream(result)
    val helper = Helper(classOf[TestClass])
    helper.printAllMethods(out)
    result.toString.split(NEWLINE, -1) shouldBe TestClass.expectedShortOutput
  }

  it should "show the short description for multiple classes" in {
    val result = new ByteArrayOutputStream()
    val out = new PrintStream(result)
    val helper = Helper(classOf[TestClass], classOf[TestClass2])
    helper.printAllMethods(out)
    result.toString.split(NEWLINE, -1) shouldBe TestClass.expectedShortOutput ++ TestClass2.expectedShortOutput
  }

  "When specific methods are requested, it" should "show the long description" in {
    val result = new ByteArrayOutputStream()
    val out = new PrintStream(result)
    val helper = Helper(classOf[TestClass])
    helper.printMethods("help", out)
    result.toString.split(NEWLINE, -1) shouldBe Array(
        s"${Console.BOLD}help()${Console.RESET} [${TestClass.name}]",
        "long help",
        ""
      )
  }

  it should "contain multiple methods with the same name but different parameters" in {
    val result = new ByteArrayOutputStream()
    val out = new PrintStream(result)
    val helper = Helper(classOf[TestClass2])
    helper.printMethods("method", out)
    println(result.toString)
    result.toString.split(NEWLINE, -1) shouldBe TestClass2.expectedLongOutput
  }

  "Curried parameters" should "be printed in correct order in the short description" in {
    val result = new ByteArrayOutputStream()
    val out = new PrintStream(result)
    val helper = Helper(classOf[TestClass3])
    helper.printAllMethods(out)
    result.toString.split(NEWLINE, -1) shouldBe Array(
      s"${Console.BOLD}category${Console.RESET} [${TestClass3.name}]",
      "- method(1)(2)(3)(4)(5)(6)(7)(8)(9): short",
      ""
    )
  }

  it should "be printed in correct order in the long description" in {
    val result = new ByteArrayOutputStream()
    val out = new PrintStream(result)
    val helper = Helper(classOf[TestClass3])
    helper.printMethods("method", out)
    result.toString.split(NEWLINE, -1) shouldBe Array(
      s"${Console.BOLD}method(1)(2)(3)(4)(5)(6)(7)(8)(9)${Console.RESET} [${TestClass3.name}]",
      "long",
      ""
    )
  }

  "Solitary objects" should "not contain $ in their name when long description is printed" in {
    val result = new ByteArrayOutputStream()
    val out = new PrintStream(result)
    val helper = Helper(DummyObjectThatIsNoCompanion.getClass)
    helper.printMethods("method", out)
    result.toString.split(NEWLINE, -1) shouldBe Array(
      s"${Console.BOLD}method()${Console.RESET} [${DummyObjectThatIsNoCompanion.name}]",
      "l",
      ""
    )
  }

  it should "not contain $ in their name when short description is printed" in {
    val result = new ByteArrayOutputStream()
    val out = new PrintStream(result)
    val helper = Helper(DummyObjectThatIsNoCompanion.getClass)
    helper.printAllMethods(out)
    result.toString.split(NEWLINE, -1) shouldBe Array(
      s"${Console.BOLD}c${Console.RESET} [${DummyObjectThatIsNoCompanion.name}]",
      "- method(): s",
      ""
    )
  }

}
