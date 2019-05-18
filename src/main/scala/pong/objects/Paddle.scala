package pong.objects

import monocle.macros.Lenses
import pong.io.Key

trait Paddle extends TableElement {
  val x: Double
  val y: Double
  val length: Int

  override def acceptVisitor[R](v: ElementVisitor[R]): R = v.doForPaddle(this)

  def processCollisions(maxY: Int, y: Double, length: Int): Double =
    if (y < 0) 0 else if (y + length > maxY) maxY - length else y

  def moveAndCollide(maxY: Int, dt: Double, sensitivity: Double, commands: Vector[Key]): Paddle
}

@Lenses
case class AIPaddle(x: Double,
                    y: Double,
                    speed: Double,
                    length: Int)
    extends Paddle {

  override def moveAndCollide(maxY: Int, dt: Double, sensitivity: Double, commands: Vector[Key]): AIPaddle = {
    val newY = y + dt * speed
    val newSpeed = if (newY + length > maxY || newY < 0) -speed else speed
    val setY = processCollisions(maxY, newY, length)

    copy(y = setY, speed = newSpeed)
  }
}

@Lenses
case class HumanPaddle(x: Double,
                       y: Double,
                       length: Int)
    extends Paddle {

  override def moveAndCollide(maxY: Int, dt: Double, sensitivity: Double, commands: Vector[Key]): HumanPaddle = {
    val newY = commands.foldLeft(y)((res, key) =>
      key match {
        case Key.s => res + sensitivity
        case Key.w => res - sensitivity
        case _     => res
    })
    val setY = processCollisions(maxY, newY, length)
    copy(y = setY)
  }
}
