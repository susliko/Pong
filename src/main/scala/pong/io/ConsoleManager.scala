package pong.io

import cats.Monad
import cats.effect.{ContextShift, Fiber, IO}
import cats.syntax.flatMap._
import fs2.concurrent.Queue
import java.io.BufferedReader
import java.io.InputStreamReader

import scala.sys.process._
import scala.util.Try

case class ConsoleManager(queue: Queue[IO, Key])(
    implicit cs: ContextShift[IO]) {

  /**
    * If some key was pressed till the moment, returns it. Does not block on input
    */
  def tryReadKey(bufferedStdIn: BufferedReader): IO[Option[Key]] = IO {
    if (bufferedStdIn.ready()) {
      Try(bufferedStdIn.read).toOption.map(Key.fromCode)
    } else None
  }

  def readKey: IO[Key] =
    IO(Try(Console.in.read).toOption.map(Key.fromCode).getOrElse(Key.Other))

  /**
    * Enables raw mode of console input (input after each key press)
    */
  def toRawMode: IO[Unit] = IO {
    Seq("sh", "-c", "stty -icanon min 1 < /dev/tty").!
    Seq("sh", "-c", "stty -echo < /dev/tty").!
  }

  /**
    * Enables cooked mode of console input (input only after Enter pressed)
    */
  def toCookedMode: IO[Unit] = IO {
    Seq("sh", "-c", "stty echo < /dev/tty").!
    Seq("sh", "-c", "stty sane < /dev/tty").!
  }

  /**
    * Blocks until line is read
    */
  def readLine: IO[String] = IO(Console.in.readLine())

  /**
    * Clears console screen
    */
  def clearOutput: IO[Unit] = output("\u001b[H\u001b[2J")

  /**
    * Prints to the screen
    */
  def output(text: String): IO[Unit] = IO(println(text))

  /**
    * Starts to read all pressed keys into the queue. Runs on a provided by ContextShift ThreadPool
    */
  def readKeysAsync: IO[Fiber[IO, Unit]] =
    for {
      _ <- getKeysPressed
      stdInputStream = new InputStreamReader(System.in)
      bufferedStdIn = new BufferedReader(stdInputStream)
      token <- Monad[IO]
        .iterateWhile(
          for {
            key <- tryReadKey(bufferedStdIn)
            _ <- key.map(k => queue.enqueue1(k)).getOrElse(IO.unit)
          } yield ()
        )(_ => true)
        .start(cs)
    } yield token

  /**
    *  Returns all unprocessed pressed keys at the moment
    */
  def getKeysPressed: IO[Vector[Key]] =
    Monad[IO].tailRecM[Vector[Key], Vector[Key]](Vector.empty)(acc =>
      queue.tryDequeue1.map {
        case Some(i) => Left(i +: acc)
        case None    => Right(acc)
    })

  /**
    * Waits until one of listed keys is being pressed.
    * Returns None if input was terminated by pressing Esc
    * and Esc key was not expected to appear in `keys`
    */
  def waitForKeysPressed(infoMessage: Option[String] = None,
                         keys: Vector[Key]): IO[Key] =
    for {
      key <- Monad[IO].iterateUntil {
        infoMessage.map(m => output(m)).getOrElse(IO.unit) >>
          readKey
      }(keys.contains)
    } yield key
}
