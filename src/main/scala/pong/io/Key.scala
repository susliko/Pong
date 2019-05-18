package pong.io

import enumeratum.{Enum, EnumEntry}

sealed abstract class Key extends EnumEntry with Serializable

object Key extends Enum[Key] {
  def values = findValues

  case object One extends Key
  case object Two extends Key
  case object Three extends Key
  case object w extends Key
  case object s extends Key
  case object Enter extends Key
  case object Esc extends Key
  case object Other extends Key

  def fromCode(code: Int): Key = code match {
    case 10  => Enter
    case 27  => Esc
    case 49 => One
    case 50 => Two
    case 51 => Three
    case 115 => s
    case 119 => w
    case _   => Other
  }

}
