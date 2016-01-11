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
    val testClass = new TestClass().getClass
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

  it should "print only the short description in the method listing" in {
    val result = new ByteArrayOutputStream()
    val out = new PrintStream(result)
    val helper = Helper(new TestClass().getClass)
    helper.printAllMethods(out)
    result.toString.split(NEWLINE, -1) shouldBe Array(
        s"${Console.BOLD}a${Console.RESET} [TestClass]",
        "- help(): short help",
        "- xhelp(): short help",
        "",
        s"${Console.BOLD}bbb${Console.RESET} [TestClass]",
        "- helpWithParameters(i: Integer)(s: String): sph",
        ""
      )
  }

  it should "print the long description if a method help is requested" in {
    val result = new ByteArrayOutputStream()
    val out = new PrintStream(result)
    val helper = Helper(new TestClass().getClass)
    helper.printMethods("help", out)
    result.toString.split(NEWLINE, -1) shouldBe Array(
        s"${Console.BOLD}help()${Console.RESET} [TestClass]",
        "long help",
        ""
      )
  }

  it should "print help for multiple methods if there are multiple methods with the same name but different parameters" in {
    val result = new ByteArrayOutputStream()
    val out = new PrintStream(result)
    val helper = Helper(new TestClass2().getClass)
    helper.printMethods("method", out)
    println(result.toString)
    result.toString.split(NEWLINE, -1) shouldBe Array(
      s"${Console.BOLD}method()${Console.RESET} [TestClass2]",
      "method without parameters",
      "",
      s"${Console.BOLD}method(s: String)${Console.RESET} [TestClass2]",
      "method with one parameter",
      ""
    )
  }

  "Curried parameters" should "be printed in correct order in the short description" in {
    val result = new ByteArrayOutputStream()
    val out = new PrintStream(result)
    val helper = Helper(new TestClass3().getClass)
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
    val helper = Helper(new TestClass3().getClass)
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
