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
    val expectedShortOutput = Array(
      s"${Console.BOLD}a${Console.RESET} [${classOf[TestClass].getSimpleName}]",
      "- help(): short help",
      "- xhelp(): short help",
      "",
      s"${Console.BOLD}bbb${Console.RESET} [${classOf[TestClass].getSimpleName}]",
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
    val expectedShortOutput = Array(
      s"${Console.BOLD}category${Console.RESET} [${classOf[TestClass2].getSimpleName}]",
      "- method(): without parameters",
      "- method(s: String): with parameters",
      ""
    )

    val expectedLongOutput = Array(
      s"${Console.BOLD}method()${Console.RESET} [${classOf[TestClass2].getSimpleName}]",
      "method without parameters",
      "",
      s"${Console.BOLD}method(s: String)${Console.RESET} [${classOf[TestClass2].getSimpleName}]",
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

  "A helper" should "offer only help for methods with the correct annotation" in {
    val testClass = classOf[TestClass]
    val testClassName = testClass.getSimpleName
    val helper = Helper(testClass)

    val expectedHelpMethod = testClass.getMethod("help")
    val expectedHelpAnnotation = expectedHelpMethod.getAnnotation(HelpAnnotationClassUtil.getHelpAnnotationClass)

    val expectedXHelpMethod = testClass.getMethod("xhelp")
    val expectedXHelpAnnotation = expectedXHelpMethod.getAnnotation(HelpAnnotationClassUtil.getHelpAnnotationClass)

    val expectedHelpWithParametersMethod = testClass.getMethod("helpWithParameters", classOf[Integer])
    val expectedHelpWithParametersAnnotation = expectedHelpWithParametersMethod.getAnnotation(HelpAnnotationClassUtil.getHelpAnnotationClass)

    helper.methods shouldBe Seq(
      (testClassName, "a") -> Seq(
        (expectedHelpMethod, expectedHelpAnnotation),
        (expectedXHelpMethod, expectedXHelpAnnotation)
      ),
      (testClassName, "bbb") -> Seq(
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

  class B {
    @Help(
      category = "category",
      shortDescription = "",
      longDescription = ""
    )
    def method = ???
  }

  it should "group correctly based on category and class" in {
    val aClass = classOf[A]
    val aClassName = aClass.getSimpleName
    val bClass = classOf[B]
    val bClassName = bClass.getSimpleName
    val helper = Helper(aClass, bClass)

    val expectedAMethod = aClass.getMethod("method")
    val expectedAAnnotation = expectedAMethod.getAnnotation(HelpAnnotationClassUtil.getHelpAnnotationClass)
    val expectedBMethod = bClass.getMethod("method")
    val expectedBAnnotation = expectedBMethod.getAnnotation(HelpAnnotationClassUtil.getHelpAnnotationClass)

    val expectedCategory = "category"

    helper.methods shouldBe Seq(
      (aClassName, expectedCategory) -> Seq(
        (expectedAMethod, expectedAAnnotation)
      ),
      (bClassName, expectedCategory) -> Seq(
        (expectedBMethod, expectedBAnnotation)
      )
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
        s"${Console.BOLD}help()${Console.RESET} [TestClass]",
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
      s"${Console.BOLD}category${Console.RESET} [TestClass3]",
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
      s"${Console.BOLD}method(1)(2)(3)(4)(5)(6)(7)(8)(9)${Console.RESET} [TestClass3]",
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
      s"${Console.BOLD}method()${Console.RESET} [DummyObjectThatIsNoCompanion]",
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
      s"${Console.BOLD}c${Console.RESET} [DummyObjectThatIsNoCompanion]",
      "- method(): s",
      ""
    )
  }

}
