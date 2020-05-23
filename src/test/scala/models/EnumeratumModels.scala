package models

import enumeratum.{Enum, EnumEntry}

case class SModelWithEnum(orderSize: OrderSize = OrderSize.TALL)

sealed abstract class OrderSize extends EnumEntry

object OrderSize extends Enum[OrderSize] {
  override def values: IndexedSeq[OrderSize] = findValues

  case object TALL extends OrderSize
  case object GRANDE extends OrderSize
  case object VENTI extends OrderSize
}