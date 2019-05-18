package pong.objects

trait TableElement {
  def acceptVisitor[R](v: ElementVisitor[R]): R
}
