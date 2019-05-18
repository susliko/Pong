package pong.game

import java.net.{BindException, ConnectException, SocketException}

import cats.effect.IO
import cats.syntax.option._
import pong.io.Key
import pong.menu.{FinishMenu, MenuEntry}
import pong.objects.{Ball, Player}

import scala.concurrent.TimeoutException

trait Game extends MenuEntry {

  override def start: IO[Unit] =
    for {
      _ <- console.clearOutput
      token <- console.readKeysAsync
      finalState <- runGame.map(_.some).handleErrorWith { e =>
        val msg = e match {
          case _: TimeoutException =>
            s"Connection timeout exceeded (${settings.clientWaitingTimeout} seconds)"
          case _: ConnectException =>
            "Could not connect to server. Check that program is running on the specified address"
          case _: BindException =>
            s"Port ${settings.multiplayerPort} is already in use"
          case _ =>
            "Connection was broken"
        }
        console.output(msg).map(_ => None)
      }
      _ <- token.cancel
      nextMenu = finalState
        .flatMap(gameLost)
        .map(
          lost =>
            FinishMenu(initState, table, settings, console, lost)(timer,
                                                                  contextShift))
      _ <- nextMenu.map(_.start).getOrElse(IO.unit)
    } yield ()

  def gameLost(finalState: GameState): Option[Boolean]

  def runGame: IO[GameState]

  def evolveState(state: GameState,
                  leftCommands: Vector[Key],
                  rightCommands: Vector[Key] = Vector.empty): GameState = {
    val lPaddle =
      state.leftPlayer.paddle.moveAndCollide(table.width,
                                             settings.tpf,
                                             settings.sensitivity,
                                             leftCommands)
    val rPaddle = state.rightPlayer.paddle.moveAndCollide(table.width,
                                                          settings.tpf,
                                                          settings.sensitivity,
                                                          rightCommands)
    val ball = state.ball
      .move(table.length, table.width, settings.tpf)
      .intersectLeft(lPaddle)
      .intersectRight(rPaddle)

    Function.chain(
      Seq(
        GameState.ball.set(ball),
        (GameState.leftPlayer ^|-> Player.paddle).set(lPaddle),
        (GameState.rightPlayer ^|-> Player.paddle).set(rPaddle),
        GameState.commands.set(leftCommands),
        updateScores(_)
      ))(state)
  }

  def updateScores(state: GameState): GameState = {
    import state._

    val toInitState = (lScore: Int, rScore: Int) =>
      Function.chain(
        Seq(
          (GameState.leftPlayer ^|-> Player.score).set(lScore),
          (GameState.rightPlayer ^|-> Player.score).set(rScore),
          GameState.ball.modify(
            Ball.setRandomSpeed(_, settings.vxRange, settings.vyRange)),
          GameState.commands.set(commands)
        ))

    val status =
      if (ball.x <= leftPlayer.paddle.x + 1 && ball.isOutOfPaddle(
            leftPlayer.paddle))
        GameStatus.RightWon
      else if (ball.x + 1 >= rightPlayer.paddle.x && ball.isOutOfPaddle(
                 rightPlayer.paddle)) GameStatus.LeftWon
      else GameStatus.Continues

    status match {
      case GameStatus.LeftWon =>
        toInitState(leftPlayer.score + 1, rightPlayer.score)(initState)
      case GameStatus.RightWon =>
        toInitState(leftPlayer.score, rightPlayer.score + 1)(initState)
      case GameStatus.Continues => GameState.roundBegins.set(false)(state)
    }
  }

  protected def draw(state: GameState): IO[Unit] =
    for {
      _ <- console.clearOutput
      _ <- console.output(
        table.draw(
          state.leftPlayer.score,
          state.rightPlayer.score,
          List(state.ball, state.leftPlayer.paddle, state.rightPlayer.paddle)))
    } yield ()
}
