package pong.menu

import cats.effect.{ContextShift, IO, Timer}
import cats.syntax.applicative._
import cats.syntax.option._
import pong.game._
import pong.io.{ConsoleManager, Key}
import pong.objects.{Player, Table}

case class StartMenu(initState: GameState,
                     table: Table,
                     settings: GameSettings,
                     console: ConsoleManager)(
    implicit val timer: Timer[IO],
    val contextShift: ContextShift[IO])
    extends MenuEntry {

  val allowedKeys = Vector(Key.One, Key.Two, Key.Three, Key.Esc)

  val gameInfo =
    """
      |Welcome to the Pong game!
      |
      |Rules:
      |Move your paddle and bounce the ball.
      |If you miss the ball, your opponent gets one point.
      |Game is going until someone gains 5 points.
      |
      |Settings:
      |`w` - move paddle up
      |`s` - move paddle down
      |`Esc` - stop the game
      |
      |Choose game mode:
      |
      |1. SinglePlayer
      |2. MultiPlayer server
      |3. MultiPlayer client
    """.stripMargin

  val incorrectInputMessage =
    """
      |Please, press one of these keys: 1, 2, 3, Esc
    """.stripMargin


  val multiPlayerState = (GameState.rightPlayer ^|-> Player.paddle)
    .set(settings.rightHumanPaddle)(initState)

  val aiPlayerState = (GameState.rightPlayer ^|-> Player.paddle)
    .set(settings.rightAIPaddle)(initState)

  override def start: IO[Unit] =
    for {
      _ <- console.clearOutput
      _ <- console.output(gameInfo)
      key <- console.waitForKeysPressed(Some(incorrectInputMessage),
                                        allowedKeys)
      nextMenu = key match {
        case Key.One =>
          SinglePlayer(aiPlayerState, table, settings, console).some
        case Key.Two =>
          MultiPlayerServer(multiPlayerState, table, settings, console).some
        case Key.Three =>
          MultiPlayerClient(multiPlayerState, table, settings, console).some
        case _ => None
      }
      _ <- nextMenu
        .map(_.start)
        .getOrElse(IO.unit)
    } yield ()
}
