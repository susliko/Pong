import cats.effect.{ExitCode, IO, IOApp, Resource}
import fs2.concurrent.Queue
import pong.game.{GameSettings, GameState}
import pong.io._
import pong.menu.StartMenu
import pong.objects._
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext

object Main extends IOApp {

  val blockingEC: Resource[IO, ExecutionContext] = pong.io.blockingThreadPool

  val settings = GameSettings(
    tpf = 1.0 / 20.0,
    sensitivity = 1.0,
    highScore = 5,
    vxRange = (20, 30),
    vyRange = (5, 10),
    multiplayerPort = 8642,
    roundSleepTime = 500,
    rightHumanPaddle = HumanPaddle(x = 79, y = 8, length = 4),
    rightAIPaddle = AIPaddle(x = 79, y = 6, speed = 6, length = 14),
    clientWaitingTimeout = 20.seconds
  )
  val table = Table(width = 20, length = 80)
  val initState =
    GameState(
      ball = Ball(x = 40, y = 10, vx = -40, vy = 4),
      leftPlayer = Player(paddle = HumanPaddle(x = 0, y = 8, length = 4)),
      rightPlayer =
        Player(paddle = AIPaddle(x = 79, y = 8, speed = 6, length = 10))
    )

  def runGame(blEc: ExecutionContext) =
    for {
      queue <- Queue.unbounded[IO, Key]
      cs = IO.contextShift(blEc)
      console = ConsoleManager(queue)(cs)
      startMenu = StartMenu(initState, table, settings, console)
      _ <- console.toRawMode
      _ <- startMenu.start
      _ <- console.toCookedMode
    } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- blockingEC.use(ec => runGame(ec))
    } yield ExitCode.Success
}
