package pong

import java.io.ObjectInputStream
import java.util.concurrent.Executors

import cats.effect.{IO, Resource}

import scala.concurrent.ExecutionContext

package object io {
  def blockingThreadPool: Resource[IO, ExecutionContext] =
    Resource(IO {
      val executor = Executors.newCachedThreadPool()
      val ec = ExecutionContext.fromExecutor(executor)
      (ec, IO(executor.shutdown()))
    })

  def getObjFromStream[T](s: ObjectInputStream): T = s.readObject().asInstanceOf[T]
}
