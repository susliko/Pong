package pong.game

import enumeratum.{Enum, EnumEntry}

sealed trait GameStatus extends EnumEntry

object GameStatus extends Enum[GameStatus] {
  override def values = findValues

  case object LeftWon extends GameStatus
  case object RightWon extends GameStatus
  case object Continues extends GameStatus
}
