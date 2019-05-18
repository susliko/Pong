package pong.menu
import cats.effect.{ContextShift, IO, Timer}
import cats.syntax.option._
import pong.game.{GameSettings, GameState}
import pong.io.{ConsoleManager, Key}
import pong.objects.Table

case class FinishMenu(initState: GameState,
                      table: Table,
                      settings: GameSettings,
                      console: ConsoleManager,
                      gameLost: Boolean)(implicit val timer: Timer[IO],
                                         val contextShift: ContextShift[IO])
    extends MenuEntry {

  val youLoseText =
    """
      |__     __           _                  _
      |\ \   / /          | |                | |
      | \ \_/ /__  _   _  | | ___  ___  ___  | |
      |  \   / _ \| | | | | |/ _ \/ __|/ _ \ | |
      |   | | (_) | |_| | | | (_) \__ \  __/ |_|
      |   |_|\___/ \__,_| |_|\___/|___/\___| (_)
      |
    """.stripMargin

  val youWonText =
    """
      |__     __                                 _
      |\ \   / /                                | |
      | \ \_/ /__  _   _  __      _____  _ __   | |
      |  \   / _ \| | | | \ \ /\ / / _ \| '_ \  | |
      |   | | (_) | |_| |  \ V  V / (_) | | | | |_|
      |   |_|\___/ \__,_|   \_/\_/ \___/|_| |_| (_)
    """.stripMargin

  val continueText =
    """
      |Press Enter to play again
      |Press Esc to quit the game
    """.stripMargin

  val allowedKeys = Vector(Key.Enter, Key.Esc)

  override def start: IO[Unit] =
    for {
      _ <- console.clearOutput
      _ <- console.output(if (gameLost) youLoseText else youWonText)
      key <- console.waitForKeysPressed(Some(continueText), allowedKeys)
      nextMenu = key match {
        case Key.Enter => StartMenu(initState, table, settings, console).some
        case _         => None
      }
      _ <- nextMenu.map(_.start).getOrElse(IO.unit)
    } yield ()
}
