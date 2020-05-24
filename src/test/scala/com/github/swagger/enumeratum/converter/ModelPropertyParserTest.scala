package com.github.swagger.enumeratum.converter

import io.swagger.v3.core.converter._
import io.swagger.v3.oas.models.media._
import models.{ModelWEnum, ModelWEnumAnnotated, ModelWOptionalEnum, OrderSize}
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.JavaConverters._

class ModelPropertyParserTest extends AnyFlatSpec with Matchers with OptionValues {
  it should "process Model with Enumeratum Enum" in {
    val converter = ModelConverters.getInstance()
    val schemas = converter.readAll(classOf[ModelWEnum]).asScala.toMap
    val model = findModel(schemas, "ModelWEnum")
    model should be (defined)
    model.get.getProperties should not be (null)
    val field = model.value.getProperties.get("field")
    field shouldBe a [StringSchema]
    field.asInstanceOf[StringSchema].getEnum.asScala shouldEqual OrderSize.values.map(_.entryName)
    nullSafeList(model.value.getRequired) shouldBe Seq("field")
  }
  it should "process Model with Optional Enumeratum Enum" in {
    val converter = ModelConverters.getInstance()
    val schemas = converter.readAll(classOf[ModelWOptionalEnum]).asScala.toMap
    val model = findModel(schemas, "ModelWOptionalEnum")
    model should be (defined)
    model.get.getProperties should not be (null)
    val field = model.value.getProperties.get("field")
    field shouldBe a [StringSchema]
    field.asInstanceOf[StringSchema].getEnum.asScala shouldEqual OrderSize.values.map(_.entryName)
    nullSafeList(model.value.getRequired) shouldBe empty
  }
  it should "process Model with Annotated Enumeratum Enum" in {
    val converter = ModelConverters.getInstance()
    val schemas = converter.readAll(classOf[ModelWEnumAnnotated]).asScala.toMap
    val model = findModel(schemas, "ModelWEnumAnnotated")
    model should be (defined)
    model.get.getProperties should not be (null)
    val field = model.value.getProperties.get("field")
    field shouldBe a [StringSchema]
    val schema = field.asInstanceOf[StringSchema]
    schema.getDescription shouldEqual "enum value"
    schema.getEnum.asScala shouldEqual OrderSize.values.map(_.entryName)
    nullSafeList(model.value.getRequired) shouldBe Seq.empty
  }

  def findModel(schemas: Map[String, Schema[_]], name: String): Option[Schema[_]] = {
    schemas.get(name) match {
      case Some(m) => Some(m)
      case None =>
        schemas.keys.find { case k => k.startsWith(name) } match {
          case Some(key) => schemas.get(key)
          case None => schemas.values.headOption
        }
    }
  }

  def nullSafeList[T](list: java.util.List[T]): List[T] = Option(list) match {
    case None => List[T]()
    case Some(l) => l.asScala.toList
  }
}
