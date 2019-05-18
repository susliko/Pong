package pong.menu

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.net.{InetAddress, Socket}
import java.util.concurrent.TimeoutException

import cats.Monad
import cats.effect.{ContextShift, IO, Resource, Timer}
import pong.game.{Game, GameSettings, GameState}
import pong.io.{ConsoleManager, Key}
import cats.syntax.flatMap._
import cats.syntax.applicative._
import pong.objects.Table

import scala.concurrent.duration._

case class MultiPlayerClient(initState: GameState,
                             table: Table,
                             settings: GameSettings,
                             console: ConsoleManager)(
    implicit val timer: Timer[IO],
    val contextShift: ContextShift[IO])
    extends Game {

  def serverName: IO[String] = for {
    _ <- console.output("Please, enter server IP or domain name (e.g. localhost):")
    _ <- console.toCookedMode
    name <- console.readLine
    _ <- console.toRawMode
  } yield name

  def serverSock(address: String): Resource[IO, Socket] =
    Resource.make(
      IO.race(
          IO.sleep(settings.clientWaitingTimeout),
          IO {
            val name = InetAddress.getByName(address)
            println("Trying to connect to server...")
            val socket = new Socket(name, settings.multiplayerPort)
            socket
          }
        )
        .flatMap {
          case Left(_)  => IO.raiseError(new TimeoutException)
          case Right(x) => IO.pure(x)
        })(socket => IO(socket.close()))

  def receiveGameState(socket: Socket): IO[GameState] =
    IO {
      val in = new ObjectInputStream(socket.getInputStream)
      pong.io.getObjFromStream[GameState](in)
    }

  def sendPlayerCommands(socket: Socket, commands: Vector[Key]): IO[Unit] =
    IO {
      val out = new ObjectOutputStream(socket.getOutputStream)
      out.writeObject(commands)
    }

  override def gameLost(finalState: GameState): Option[Boolean] =
    if (finalState.rightPlayer.score == settings.highScore) Some(false)
    else if (finalState.leftPlayer.score == settings.highScore) Some(true)
    else None

  override def runGame: IO[GameState] =
    for {
      server <- serverName
      serverSocket = serverSock(server)
      state <- serverSocket.use(
        socket =>
          Monad[IO].iterateWhile(for {

            state <- receiveGameState(socket)
            _ <- draw(state)
            _ <- (IO
              .sleep(settings.roundSleepTime.milliseconds) >> console.getKeysPressed)
              .whenA(state.roundBegins)
            commands <- console.getKeysPressed
            _ <- sendPlayerCommands(socket, commands)
          } yield state.copy(commands = commands))(state =>
            state.continue(settings.highScore)
              && !state.commands.contains(Key.Esc)))
    } yield state

}
