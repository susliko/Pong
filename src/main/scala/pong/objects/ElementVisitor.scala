package pong.objects

trait ElementVisitor[R] {
  def doForBall(b: Ball): R
  def doForPaddle(p: Paddle): R
}
