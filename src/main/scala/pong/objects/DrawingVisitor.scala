package pong.objects

case class DrawingVisitor(tableLength: Int)
    extends ElementVisitor[List[(Int, Char)]] {

  def doForBall(b: Ball): List[(Int, Char)] = {
    import b._
    val newX: Int =
      if (x < 0) 0 else if (x > tableLength - 2) tableLength - 2 else x.toInt
    List((y.round.toInt * tableLength + newX, '●'))
  }

  def doForPaddle(p: Paddle): List[(Int, Char)] = {
    import p._
    (0 until length)
      .map(elInd => ((y.round.toInt + elInd) * tableLength + x.toInt, '█'))
      .toList
  }
}
