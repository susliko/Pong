package pong.game

import monocle.macros.Lenses
import pong.io.Key
import pong.objects.{Ball, Player}

@Lenses
case class GameState(ball: Ball,
                     leftPlayer: Player,
                     rightPlayer: Player,
                     commands: Vector[Key] = Vector.empty,
                     roundBegins: Boolean = true) {
  def continue(highScore: Int): Boolean =
    !commands.contains(Key.Esc) &&
      leftPlayer.score < highScore &&
      rightPlayer.score < highScore
}
