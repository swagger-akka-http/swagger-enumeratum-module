package com.github.swagger.enumeratum.converter

import io.swagger.v3.core.converter._
import io.swagger.v3.oas.models.media._
import models.{
  Animal,
  Ctx,
  ModelWCtxEnum,
  ModelWCtxEnumAndAnnotation,
  ModelWEnum,
  ModelWEnumAnnotated,
  ModelWEnumSet,
  ModelWOptionalEnum,
  OrderSize
}
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.JavaConverters._

class ModelPropertyParserTest extends AnyFlatSpec with Matchers with OptionValues {
  it should "process Model with Enumeratum Enum" in {
    val converter = ModelConverters.getInstance()
    val schemas = converter.readAll(classOf[ModelWEnum]).asScala.toMap
    val model = findModel(schemas, "ModelWEnum")
    model should be(defined)
    model.get.getProperties should not be (null)
    val field = model.value.getProperties.get("field")
    field shouldBe a[StringSchema]
    nullSafeList(field.asInstanceOf[StringSchema].getEnum) shouldEqual OrderSize.values.map(_.entryName)
    nullSafeList(model.value.getRequired) shouldBe Seq("field")
  }
  it should "process Model with Optional Enumeratum Enum" in {
    val converter = ModelConverters.getInstance()
    val schemas = converter.readAll(classOf[ModelWOptionalEnum]).asScala.toMap
    val model = findModel(schemas, "ModelWOptionalEnum")
    model should be(defined)
    model.get.getProperties should not be (null)
    val field = model.value.getProperties.get("field")
    field shouldBe a[StringSchema]
    nullSafeList(field.asInstanceOf[StringSchema].getEnum) shouldEqual OrderSize.values.map(_.entryName)
    nullSafeList(model.value.getRequired) shouldBe empty
  }
  it should "process Model with Enumeratum Set" in {
    val converter = ModelConverters.getInstance()
    val schemas = converter.readAll(classOf[ModelWEnumSet]).asScala.toMap
    val model = findModel(schemas, "ModelWEnumSet")
    model should be(defined)
    model.get.getProperties should not be (null)
    val field = model.value.getProperties.get("sizes")
    field shouldBe a[ArraySchema]
    val arraySchema = field.asInstanceOf[ArraySchema]
    nullSafeList(arraySchema.getItems.getEnum) shouldEqual OrderSize.values.map(_.entryName)
    nullSafeList(model.value.getRequired) shouldEqual List("sizes")
  }
  it should "process Model with Annotated Enumeratum Enum" in {
    val converter = ModelConverters.getInstance()
    val schemas = converter.readAll(classOf[ModelWEnumAnnotated]).asScala.toMap
    val model = findModel(schemas, "ModelWEnumAnnotated")
    model should be(defined)
    model.get.getProperties should not be (null)
    val field = model.value.getProperties.get("field")
    field shouldBe a[StringSchema]
    val schema = field.asInstanceOf[StringSchema]
    schema.getDescription shouldEqual "enum value"
    nullSafeList(schema.getEnum) shouldEqual OrderSize.values.map(_.entryName)
    nullSafeList(model.value.getRequired) shouldBe Seq.empty
  }
  it should "process Model for Enumeratum Enum defined in scope of another object" in {
    val converter = ModelConverters.getInstance()
    val schemas = converter.readAll(classOf[ModelWCtxEnum]).asScala.toMap
    schemas.keys should have size 1
    val model = findModel(schemas, "ModelWCtxEnum")
    model should be(defined)
    model.get.getProperties should not be (null)

    val field = model.value.getProperties.get("field")
    field shouldBe a[StringSchema]
    field.asInstanceOf[StringSchema].getRequired shouldBe null

    val schema = field.asInstanceOf[StringSchema]
    schema.getDescription shouldEqual (null)
    schema.getDefault should be(null)
    nullSafeList(schema.getEnum) shouldEqual Ctx.Color.values.map(_.entryName)
    nullSafeList(model.value.getRequired) shouldBe Seq("field")
  }

  it should "process sealed abstract class" in {
    val converter = ModelConverters.getInstance()
    val schemas = converter.readAll(classOf[Animal]).asScala.toMap
    val catModel = findModel(schemas, "Cat")
    catModel should be(defined)
    val catProps = catModel.value.getProperties
    catProps should have size 3
    catProps.get("name") shouldBe a[StringSchema]
    catProps.get("age") shouldBe a[IntegerSchema]
    catProps.get("animalType") shouldBe a[StringSchema]
    nullSafeList(catModel.value.getRequired) shouldEqual Seq("age", "animalType", "name")
    val dogModel = findModel(schemas, "Dog")
    dogModel should be(defined)
    val dogProps = dogModel.value.getProperties
    dogProps should have size 2
    dogProps.get("name") shouldBe a[StringSchema]
    dogProps.get("animalType") shouldBe a[StringSchema]
    nullSafeList(dogModel.value.getRequired) shouldEqual Seq("animalType", "name")
  }

  it should "not add additional model when enum field is annotated" in {
    val converter = ModelConverters.getInstance()
    val schemas = converter.readAll(classOf[ModelWCtxEnumAndAnnotation]).asScala.toMap
    schemas.keys should have size 1

    val model = findModel(schemas, "ModelWCtxEnum")
    model should be(defined)
    model.get.getProperties should not be (null)
    val field = model.value.getProperties.get("field")
    field shouldBe a[StringSchema]
    val schema = field.asInstanceOf[StringSchema]
    schema.getDescription shouldEqual "An annotated field"
    schema.getName shouldEqual "field"
    schema.getDefault should be(null)
    schema.getDeprecated should be(null)
    nullSafeList(schema.getEnum) shouldEqual Ctx.Color.values.map(_.entryName)
    nullSafeList(model.value.getRequired) shouldBe Seq("field")
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
