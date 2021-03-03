package models

import io.swagger.v3.oas.annotations.media.Schema

case class ModelWEnum(field: OrderSize)

case class ModelWOptionalEnum(field: Option[OrderSize])

case class ModelWEnumAnnotated(@Schema(description = "enum value", `type` = "string", required = false) field: OrderSize)
