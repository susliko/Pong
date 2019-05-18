package pong.objects

import pong.digits

import scala.util.Try

case class Table(width: Int, length: Int) {
  val emptyLine: String = List.fill(length)(" ").mkString("")
  val border: String = List.fill(length)("█").mkString("")
  val actualLength: Int = length + 1

  val drawer = DrawingVisitor(actualLength)

  val emptyTable: String =
    ((border :: List.fill(width)(emptyLine)) :::
      List(border)).mkString("\n")

  def draw(lScore: Int, rScore: Int, drawables: List[TableElement]): String = {
    val score = digits.printScore(lScore, rScore, length)

    val updates = drawables.flatMap(_.acceptVisitor(drawer))
    score + updates.foldLeft(emptyTable)(
      (res, upd) =>
        Try(res.updated(actualLength + upd._1, upd._2)).toOption
          .getOrElse(res))
  }
}
