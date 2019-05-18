package pong.objects

import monocle.macros.Lenses

import scala.util.Random

@Lenses
case class Ball(x: Double, y: Double, vx: Double, vy: Double)
    extends TableElement {
  def move(maxLength: Int, maxWidth: Int, dt: Double): Ball = {
    val newX = x + vx * dt
    val newY = y + vy * dt
    val newVy = if (newY + 1 > maxWidth || newY < 0) -vy else vy
    copy(x = newX, y = newY, vy = newVy)
  }

  def isOutOfPaddle(paddle: Paddle): Boolean =
    (y >= paddle.y + paddle.length) || (y <= paddle.y)

  def intersectRight(paddle: Paddle): Ball = {
    val (newVx, newX) =
      if (x + 1 >= paddle.x && !isOutOfPaddle(paddle)) (-vx, paddle.x - 1)
      else (vx, x)
    copy(x = newX, vx = newVx)
  }

  def intersectLeft(paddle: Paddle): Ball = {
    val (newVx, newX) =
      if (x <= paddle.x + 1 && !isOutOfPaddle(paddle)) {
        (-vx, paddle.x + 1)
      } else (vx, x)
    copy(x = newX, vx = newVx)
  }

  override def acceptVisitor[R](v: ElementVisitor[R]): R = v.doForBall(this)
}

object Ball {
  def setRandomSpeed(ball: Ball,
                     vxRange: (Double, Double),
                     vyRange: (Double, Double)): Ball = {
    val r = new Random(System.currentTimeMillis())
    val vxPos = r.shuffle(List(-1, 1)).head
    val vyPos = r.shuffle(List(-1, 1)).head
    val vx = vxPos * (vxRange._1 + r.nextDouble() * (vxRange._2 - vxRange._1))
    val vy = vyPos * (vyRange._1 + r.nextDouble() * (vyRange._2 - vyRange._1))
    ball.copy(vx = vx, vy = vy)
  }
}
