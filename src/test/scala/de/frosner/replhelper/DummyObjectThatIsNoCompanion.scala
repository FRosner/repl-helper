package de.frosner.replhelper

object DummyObjectThatIsNoCompanion {

  @Help(
    category = "c",
    shortDescription = "s",
    longDescription = "DummyObjectThatIsNoCompanionLong"
  )
  def method = ???

  val name = this.getClass.getSimpleName.replace("$", "")
  val category = "c"

  val expectedMethodShortDescription = "- method(): s"

  val expectedShortDescription = Array(
    s"${Console.BOLD}c${Console.RESET} [${DummyObjectThatIsNoCompanion.name}]",
    expectedMethodShortDescription,
    ""
  )

  val expectedMethodLongDescription = "DummyObjectThatIsNoCompanionLong"

  val expectedLongDescription = Array(
    s"${Console.BOLD}method()${Console.RESET} [${DummyObjectThatIsNoCompanion.name}]",
    expectedMethodLongDescription,
    ""
  )

}
