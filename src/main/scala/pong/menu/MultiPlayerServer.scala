package pong.menu

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.net.{ServerSocket, Socket}
import java.util.concurrent.TimeoutException

import cats.Monad
import cats.effect.{ContextShift, IO, Resource, Timer}
import pong.game.{Game, GameSettings, GameState}
import pong.io.{ConsoleManager, Key}
import pong.objects.Table
import cats.syntax.applicative._
import cats.syntax.flatMap._
import scala.concurrent.duration._

case class MultiPlayerServer(initState: GameState,
                             table: Table,
                             settings: GameSettings,
                             console: ConsoleManager)(
    implicit val timer: Timer[IO],
    val contextShift: ContextShift[IO])
    extends Game {

  def clientSock: Resource[IO, Socket] =
    Resource.make(
      IO.race(
          IO.sleep(settings.clientWaitingTimeout),
          IO {
            val server = new ServerSocket(settings.multiplayerPort)
            println("Waiting for client to connect...")
            server.accept()
          }
        )
        .flatMap {
          case Left(_)  => IO.raiseError(new TimeoutException)
          case Right(x) => IO.pure(x)
        })(socket => IO(socket.close()))

  def receivePlayerCommands(socket: Socket): IO[Vector[Key]] =
    IO {
      val in = new ObjectInputStream(socket.getInputStream)
      pong.io.getObjFromStream[Vector[Key]](in)
    }

  def sendGameState(state: GameState, socket: Socket): IO[Unit] =
    IO {
      val out = new ObjectOutputStream(socket.getOutputStream)
      out.writeObject(state)
    }

  override def gameLost(finalState: GameState): Option[Boolean] =
    if (finalState.leftPlayer.score == settings.highScore) Some(false)
    else if (finalState.rightPlayer.score == settings.highScore) Some(true)
    else None

  override def runGame: IO[GameState] =
    clientSock.use { socket =>
      for {
        finalState <- Monad[IO]
          .iterateWhileM(initState)(state =>
            for {
              _ <- sendGameState(state, socket)
              _ <- draw(state)
              _ <- (IO
                .sleep(settings.roundSleepTime.milliseconds) >> console.getKeysPressed)
                .whenA(state.roundBegins)
              startTime <- timer.clock.realTime(MILLISECONDS)
              tpfMillis = settings.tpf * 1000
              leftCommands <- console.getKeysPressed
              rightCommands <- receivePlayerCommands(socket)
              newState = evolveState(state, leftCommands, rightCommands)
              endTime <- timer.clock.realTime(MILLISECONDS)
              elapsed = endTime - startTime
              _ <- IO
                .sleep((tpfMillis - elapsed).toInt.milliseconds)
                .whenA(elapsed < tpfMillis)
            } yield newState)(state =>
            state.continue(settings.highScore)
              && !state.commands.contains(Key.Esc))
        _ <- sendGameState(finalState, socket)
      } yield finalState
    }

}
