package pong.menu

import cats.effect.{ContextShift, IO, Timer}
import pong.game.{GameSettings, GameState}
import pong.io.ConsoleManager
import pong.objects.Table

trait MenuEntry {
  val initState: GameState
  val table: Table
  val settings: GameSettings
  val console: ConsoleManager

  val timer: Timer[IO]
  val contextShift: ContextShift[IO]

  def start: IO[Unit]
}
