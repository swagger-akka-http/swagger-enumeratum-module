package models

import enumeratum.{Enum, EnumEntry}

sealed abstract class OrderSize extends EnumEntry

object OrderSize extends Enum[OrderSize] {
  override def values: scala.collection.immutable.IndexedSeq[OrderSize] = findValues

  case object TALL extends OrderSize
  case object GRANDE extends OrderSize
  case object VENTI extends OrderSize
}
