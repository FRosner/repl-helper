package de.frosner.replhelper

import java.io.PrintStream
import java.lang.reflect.Method
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import org.reflections.util.{ClasspathHelper, ConfigurationBuilder}

import scala.collection.JavaConversions

import Helper.NEWLINE

/**
 * Utility class to provide interactive help for methods that are annotated with @Help.
 * Use it to print a list of methods with help available or to print a longer description for individual methods.
 */
class Helper private (val reflections: Reflections, val filter: Class[_] => Boolean = clazz => true) {

  type Name = String
  type ShortDescription = String
  type LongDescription = String
  type ClassName = String
  type Category = String

  private def simpleClassName[T](classWithHelp: Class[T]) = classWithHelp.getSimpleName.replace("$", "")

  private[replhelper] val methods: Seq[((ClassName, Category), Seq[(Method, Help)])] = {
    val methodsThatOfferHelp = JavaConversions.asScalaSet(
      reflections.getMethodsAnnotatedWith(HelpAnnotationClassUtil.getHelpAnnotationClass)
    ).toSeq
    val methodsAndHelp = methodsThatOfferHelp.map(method => {
      val helpAnnotation = method.getAnnotations.find(annotation =>
        annotation.isInstanceOf[Help]
      ).get.asInstanceOf[Help]
      (method, helpAnnotation)
    })
    val methodsAndHelpFiltered = methodsAndHelp.filter{ case (method, help) => filter(method.getDeclaringClass) }
    methodsAndHelpFiltered.groupBy {
      case (method, help) => (simpleClassName(method.getDeclaringClass), help.category())
    }.toList.sortBy {
      case ((className, category), categoryClassMethods) => category.toLowerCase
    }.map {
      case ((className, category), categoryClassMethods) => ((className, category), categoryClassMethods.sortBy {
        case (method, help) => method.getName
      })
    }
  }

  /**
   * Prints all methods that offer help (have the [[Help]] annotation) to the given [[PrintStream]].
   *
   * @param out to print method overview to
   */
  def printAllMethods(out: PrintStream) = out.println(
    methods.map {
      case ((className, category), categoryMethods) => {
        s"${Console.BOLD}${category}${Console.RESET} [$className]" + NEWLINE + categoryMethods.map {
          case (method, help) => "- " + getMethodSignature(method, help) + s": ${help.shortDescription}"
        }.mkString(NEWLINE)
      }
    }.mkString(NEWLINE+NEWLINE)
  )

  private def getMethodSignature(method: Method, help: Help) = {
    val name = method.getName
    s"$name(${help.parameters})" +
      (if (help.parameters2() != "") { "(" + help.parameters2 + ")" } else "") +
      (if (help.parameters3() != "") { "(" + help.parameters3 + ")" } else "") +
      (if (help.parameters4() != "") { "(" + help.parameters4 + ")" } else "") +
      (if (help.parameters5() != "") { "(" + help.parameters5 + ")" } else "") +
      (if (help.parameters6() != "") { "(" + help.parameters6 + ")" } else "") +
      (if (help.parameters7() != "") { "(" + help.parameters7 + ")" } else "") +
      (if (help.parameters8() != "") { "(" + help.parameters8 + ")" } else "") +
      (if (help.parameters9() != "") { "(" + help.parameters9 + ")" } else "")
  }

  /**
   * Prints the long description of all methods having the given name to the specified [[PrintStream]].
   *
   * @param methodName to print help for
   * @param out to print help to
   */
  def printMethods(methodName: String, out: PrintStream) = {
    val methodsToPrint = methods.flatMap {
      case ((className, category), categoryMethods) => categoryMethods.filter {
        case (method, help) => method.getName == methodName
      }.map{ case (method, help) => ((className, category), method, help) }
    }
    out.println(
      methodsToPrint.sortBy {
        case ((className, category), method, help) => getMethodSignature(method, help)
      }.map {
        methodWithHelp => getLongDescriptionPrintable(methodWithHelp, out)
      }.mkString(NEWLINE+NEWLINE)
    )
  }

  private def getLongDescriptionPrintable(methodWithHelp: ((String, String), Method, Help), out: PrintStream) = {
    val ((className, category), method, help) = methodWithHelp
    s"${Console.BOLD}${getMethodSignature(method, help)}${Console.RESET} [$className]" + NEWLINE + help.longDescription
  }

}


object Helper {

  private[replhelper] val NEWLINE = System.getProperty("line.separator")

  /**
    * Factory method to provide interactive help for methods of a given class. It scans the class method definitions for
    * the ones that contain a [[Help]] annotation.
    *
    * @param clazz to scan for [[Help]] annotations
    * @param otherClazzes to scan for [[Help]] annotations
    */
  def apply(clazz: Class[_], otherClazzes: Class[_]*): Helper = {
    val allowedClasses = Set.empty[Class[_]] + clazz ++ otherClazzes
    val configuration = new ConfigurationBuilder()
      .setScanners(new MethodAnnotationsScanner())
      .setUrls(ClasspathHelper.forClass(clazz))
    new Helper(new Reflections(configuration), reflectedClass => allowedClasses.contains(reflectedClass))
  }

}
