package de.frosner.replhelper

object DummyObjectThatIsNoCompanion {

  @Help(
    category = "c",
    shortDescription = "s",
    longDescription = "l"
  )
  def method = ???

  val name = this.getClass.getSimpleName.replace("$", "")
  val category = "c"

}
