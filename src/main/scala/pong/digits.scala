package pong

object digits {

  val zeroDigit =
    """
        |  ___
        | / _ \
        || | | |
        || |_| |
        | \___/
  """.stripMargin
  val oneDigit =
    """
        | _
        |/ |
        || |
        || |
        ||_|
    """.stripMargin
  val twoDigit =
    """
        | ____
        ||___ \
        |  __) |
        | / __/
        ||_____|
      """.stripMargin
  val threeDigit =
    """
        | _____
        ||___ /
        |  |_ \
        | ___) |
        ||____/
      """.stripMargin
  val fourDigit =
    """
        | _  _
        || || |
        || || |_
        ||__   _|
        |   |_|
      """.stripMargin
  val fiveDigit =
    """
        | ____
        || ___|
        ||___ \
        | ___) |
        ||____/
      """.stripMargin
  val sixDigit =
    """
        |  __
        | / /_
        || '_ \
        || (_) |
        | \___/
      """.stripMargin
  val sevenDigit =
    """
        | _____
        ||___  |
        |   / /
        |  / /
        | /_/
      """.stripMargin
  val eightDigit =
    """
        |  ___
        | ( _ )
        | / _ \
        || (_) |
        | \___/
      """.stripMargin
  val nineDigit =
    """
        |  ___
        | / _ \
        || (_) |
        | \__, |
        |   /_/
      """.stripMargin

  val delim = "\n\n\n○\n○\n \n \n \n \n"

  def concatMultiline(strings: List[String]): String =
    strings.foldLeft("")((acc, s) => {
      val lines = acc.split("\n")
      val symbols = s.split("\n")
      val symbolLen = symbols.map(_.length).max
      val symbWithSpaces =
        symbols.map(x => {
          x + List.fill(symbolLen - x.length)(" ").mkString("")})
      if (acc.isEmpty) symbWithSpaces.mkString("\n")
      else
        lines.zip(symbWithSpaces).map(p => p._1 + "  " + p._2).mkString("\n")
    }) + "\n"

  def fromInt(a: Int): String = {
    concatMultiline(a.toString
      .map(_.asDigit)
      .map {
        case 1 => oneDigit
        case 2 => twoDigit
        case 3 => threeDigit
        case 4 => fourDigit
        case 5 => fiveDigit
        case 6 => sixDigit
        case 7 => sevenDigit
        case 8 => eightDigit
        case 9 => nineDigit
        case _ => zeroDigit
      }.toList)
  }

  def printScore(lScore: Int, rScore: Int, fieldLength: Int): String = {
    val l = fromInt(lScore)
    val r = fromInt(rScore)
    val emptyColumns = List.fill(10)(List.fill(fieldLength/2 - 12)(" ").mkString("")).mkString("\n")
    concatMultiline(List(emptyColumns, l, delim, r))
  }

}
