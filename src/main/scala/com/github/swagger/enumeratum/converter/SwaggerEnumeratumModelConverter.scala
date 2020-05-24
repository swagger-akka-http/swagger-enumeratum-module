package com.github.swagger.enumeratum.converter

import java.util.Iterator

import com.github.swagger.scala.converter.AnnotatedTypeForOption
import enumeratum.EnumEntry
import io.swagger.v3.core.converter._
import io.swagger.v3.core.jackson.ModelResolver
import io.swagger.v3.core.util.{Json, PrimitiveType}
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema.AccessMode
import io.swagger.v3.oas.annotations.media.{Schema => SchemaAnnotation}
import io.swagger.v3.oas.models.media.Schema

import scala.util.Try

class SwaggerEnumeratumModelConverter extends ModelResolver(Json.mapper()) {
  private val enumEntryClass = classOf[EnumEntry]

  override def resolve(annotatedType: AnnotatedType, context: ModelConverterContext, chain: Iterator[ModelConverter]): Schema[_] = {
    val javaType = _mapper.constructType(annotatedType.getType)
    val cls = javaType.getRawClass
    if (isEnum(cls)) {
      val sp: Schema[String] = PrimitiveType.STRING.createProperty().asInstanceOf[Schema[String]]
      setRequired(annotatedType)
      getValues(cls).foreach { v =>
        sp.addEnumItemObject(v)
      }
      nullSafeList(annotatedType.getCtxAnnotations).foreach {
        case p: Parameter => {
          Option(p.description).foreach(desc => sp.setDescription(desc))
          sp.setDeprecated(p.deprecated)
          Option(p.example).foreach(ex => sp.setExample(ex))
          Option(p.name).foreach(name => sp.setName(name))
        }
        case s: SchemaAnnotation => {
          Option(s.description).foreach(desc => sp.setDescription(desc))
          Option(s.defaultValue).foreach(df => sp.setDefault(df))
          sp.setDeprecated(s.deprecated)
          Option(s.example).foreach(ex => sp.setExample(ex))
          Option(s.name).foreach(name => sp.setName(name))
          Option(s.accessMode).foreach { accessMode =>
            accessMode match {
              case AccessMode.READ_ONLY => sp.setReadOnly(true)
              case AccessMode.WRITE_ONLY => sp.setWriteOnly(true)
              case _ =>
            }
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
    val objectClassOption = if(cls.getName.endsWith("$")) {
      Some(cls)
    } else {
      Try(Class.forName(cls.getName)).toOption
    }
    val result = objectClassOption.flatMap { objectClass =>
      Option(objectClass.getMethod("values")).map { method =>
        method.invoke(None.orNull).asInstanceOf[Seq[EnumEntry]].map(_.entryName)
      }
    }
    result.getOrElse(Seq.empty)
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
    case _ => {
      nullSafeList(annotatedType.getCtxAnnotations).collect {
        case p: Parameter => p.required()
        case s: SchemaAnnotation => s.required()
      }
    }
  }

  private def nullSafeList[T](array: Array[T]): List[T] = Option(array) match {
    case None => List.empty[T]
    case Some(arr) => arr.toList
  }
}

