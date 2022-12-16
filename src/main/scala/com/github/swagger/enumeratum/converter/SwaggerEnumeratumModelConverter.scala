package com.github.swagger.enumeratum.converter

import java.util.Iterator
import com.github.swagger.scala.converter.{AnnotatedTypeForOption, SwaggerScalaModelConverter}
import enumeratum.{Enum, EnumEntry}
import io.swagger.v3.core.converter._
import io.swagger.v3.core.jackson.ModelResolver
import io.swagger.v3.core.util.{Json, PrimitiveType}
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema.AccessMode
import io.swagger.v3.oas.annotations.media.{Schema => SchemaAnnotation}
import io.swagger.v3.oas.models.media.Schema

class SwaggerEnumeratumModelConverter extends ModelResolver(Json.mapper()) {
  private val enumEntryClass = classOf[EnumEntry]

  private def noneIfEmpty(s: String): Option[String] = Option(s).filter(_.trim.nonEmpty)

  override def resolve(annotatedType: AnnotatedType, context: ModelConverterContext, chain: Iterator[ModelConverter]): Schema[_] = {
    val javaType = _mapper.constructType(annotatedType.getType)
    val cls = javaType.getRawClass
    if (isEnum(cls)) {
      val sp: Schema[String] = PrimitiveType.STRING.createProperty().asInstanceOf[Schema[String]]
      setRequired(annotatedType)
      getValues(cls).foreach { v: String =>
        sp.addEnumItemObject(v)
      }
      nullSafeList(annotatedType.getCtxAnnotations).foreach {
        case p: Parameter => {
          noneIfEmpty(p.description).foreach(desc => sp.setDescription(desc))
          if (p.deprecated) sp.setDeprecated(true)
          noneIfEmpty(p.example).foreach(ex => sp.setExample(ex))
          noneIfEmpty(p.name).foreach(name => sp.setName(name))
        }
        case s: SchemaAnnotation => {
          noneIfEmpty(s.description).foreach(desc => sp.setDescription(desc))
          noneIfEmpty(s.defaultValue).foreach(df => sp.setDefault(df))
          if (s.deprecated) sp.setDeprecated(true)
          noneIfEmpty(s.example).foreach(ex => sp.setExample(ex))
          noneIfEmpty(s.name).foreach(name => sp.setName(name))
          Option(s.accessMode).foreach {
            case AccessMode.READ_ONLY => sp.setReadOnly(true)
            case AccessMode.WRITE_ONLY => sp.setWriteOnly(true)
            case _ =>
          }
        }
        case _ =>
      }
      sp
    } else if (chain.hasNext) {
      val nextResolved = Option(chain.next().resolve(annotatedType, context, chain))
      nextResolved match {
        case Some(property) => {
          setRequired(annotatedType)
          property
        }
        case None => None.orNull
      }
    } else {
      None.orNull
    }
  }

  private def isEnum(cls: Class[_]): Boolean = enumEntryClass.isAssignableFrom(cls)

  private def getValues(cls: Class[_]): Seq[String] = {
    val enumEntry = Class.forName(cls.getName + "$").getField("MODULE$").get(null).asInstanceOf[Enum[EnumEntry]]
    enumEntry.values.map(_.entryName)
  }

  private def setRequired(annotatedType: AnnotatedType): Unit = annotatedType match {
    case _: AnnotatedTypeForOption => // not required
    case _ => {
      val required = SwaggerScalaModelConverter.getRequiredSettings(annotatedType).headOption.getOrElse(true)
      if (required) {
        Option(annotatedType.getParent).foreach { parent =>
          Option(annotatedType.getPropertyName).foreach { n =>
            addRequiredItem(parent, n)
          }
        }
      }
    }
  }

  private def nullSafeList[T](array: Array[T]): List[T] = Option(array) match {
    case None => List.empty[T]
    case Some(arr) => arr.toList
  }
}
