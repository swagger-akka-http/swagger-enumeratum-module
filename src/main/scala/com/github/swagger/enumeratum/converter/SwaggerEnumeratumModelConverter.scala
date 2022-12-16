package com.github.swagger.enumeratum.converter

import java.util.Iterator
import com.github.swagger.scala.converter.{AnnotatedTypeForOption, SwaggerScalaModelConverter}
import enumeratum.{Enum, EnumEntry}
import io.swagger.v3.core.converter._
import io.swagger.v3.core.jackson.ModelResolver
import io.swagger.v3.core.util.{Json, PrimitiveType}
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema.{AccessMode, RequiredMode}
import io.swagger.v3.oas.annotations.media.{ArraySchema, Schema => SchemaAnnotation}
import io.swagger.v3.oas.models.media.Schema

import java.lang.annotation.Annotation

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
    val enum = Class.forName(cls.getName + "$").getField("MODULE$").get(null).asInstanceOf[Enum[EnumEntry]]
    enum.values.map(_.entryName)
  }

  private def setRequired(annotatedType: AnnotatedType): Unit = annotatedType match {
    case _: AnnotatedTypeForOption => // not required
    case _ => {
      val required = getRequiredSettings(annotatedType).headOption.getOrElse(true)
      if (required) {
        Option(annotatedType.getParent).foreach { parent =>
          Option(annotatedType.getPropertyName).foreach { n =>
            addRequiredItem(parent, n)
          }
        }
      }
    }
  }

  private def getRequiredSettings(annotatedType: AnnotatedType): Seq[Boolean] = annotatedType match {
    case _: AnnotatedTypeForOption => Seq.empty
    case _ => getRequiredSettings(nullSafeList(annotatedType.getCtxAnnotations))
  }

  private def getRequiredSettings(annotations: Seq[Annotation]): Seq[Boolean] = {
    val flags = annotations.collect {
      case p: Parameter => if (p.required()) RequiredMode.REQUIRED else RequiredMode.NOT_REQUIRED
      case s: SchemaAnnotation => {
        if (s.requiredMode() == RequiredMode.AUTO) {
          if (s.required()) {
            RequiredMode.REQUIRED
          } else if (SwaggerScalaModelConverter.isRequiredBasedOnAnnotation) {
            RequiredMode.NOT_REQUIRED
          } else {
            RequiredMode.AUTO
          }
        } else {
          s.requiredMode()
        }
      }
      case a: ArraySchema => {
        if (a.arraySchema().requiredMode() == RequiredMode.AUTO) {
          if (a.arraySchema().required()) {
            RequiredMode.REQUIRED
          } else if (SwaggerScalaModelConverter.isRequiredBasedOnAnnotation) {
            RequiredMode.NOT_REQUIRED
          } else {
            RequiredMode.AUTO
          }
        } else {
          a.arraySchema().requiredMode()
        }
      }
    }
    flags.flatMap {
      case RequiredMode.REQUIRED => Some(true)
      case RequiredMode.NOT_REQUIRED => Some(false)
      case _ => None
    }
  }

  private def nullSafeList[T](array: Array[T]): List[T] = Option(array) match {
    case None => List.empty[T]
    case Some(arr) => arr.toList
  }
}
