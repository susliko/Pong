package pong.menu

import cats.Monad
import cats.effect.{ContextShift, IO, Timer}
import pong.game.{Game, GameSettings, GameState}
import pong.io.{ConsoleManager, Key}
import pong.objects.Table
import cats.syntax.applicative._
import cats.syntax.flatMap._

import scala.concurrent.duration._

case class SinglePlayer(
    initState: GameState,
    table: Table,
    settings: GameSettings,
    console: ConsoleManager)(implicit ioTimer: Timer[IO], cs: ContextShift[IO])
    extends Game {

  override val timer: Timer[IO] = ioTimer
  override val contextShift: ContextShift[IO] = cs

  override def gameLost(finalState: GameState): Option[Boolean] =
    if (finalState.leftPlayer.score == settings.highScore) Some(false)
    else if (finalState.rightPlayer.score == settings.highScore) Some(true)
    else None

  override def runGame: IO[GameState] =
    Monad[IO]
      .iterateWhileM(initState)(state =>
        for {
          _ <- draw(state)
          _ <- (IO
            .sleep(settings.roundSleepTime.milliseconds) >> console.getKeysPressed)
            .whenA(state.roundBegins)
          startTime <- timer.clock.realTime(MILLISECONDS)
          tpfMillis = settings.tpf * 1000
          commands <- console.getKeysPressed
          newState = evolveState(state, commands)
          endTime <- timer.clock.realTime(MILLISECONDS)
          elapsed = endTime - startTime
          _ <- IO
            .sleep((tpfMillis - elapsed).toInt.milliseconds)
            .whenA(elapsed < tpfMillis)
        } yield newState)(state =>
        state.continue(settings.highScore)
          && !state.commands.contains(Key.Esc))
}
