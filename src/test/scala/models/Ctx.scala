package models

import enumeratum.{Enum, EnumEntry}
import io.swagger.v3.oas.annotations.media.Schema

object Ctx {
  sealed abstract class Color(override val entryName: String) extends EnumEntry

  object Color extends Enum[Color] {

    val values = findValues

    case object Red extends Color("red")
    case object Green extends Color("green")
    case object Blue extends Color("blue")
  }
}

case class ModelWCtxEnum(field: Ctx.Color)

case class ModelWCtxEnumAndAnnotation(@Schema(description = "An annotated field", required = true) field: Ctx.Color)
