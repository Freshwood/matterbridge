package model

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.joda.time.DateTime
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, RootJsonFormat}

/**
  * The data base specific entities
  */
trait DbEntity {
  def id: UUID

  def createdAt: Option[DateTime]

  def deletedAt: Option[DateTime]
}

trait GifEntity extends DbEntity {
  def name: String

  def gifUrl: String
}

case class NineGagEntity(id: UUID,
                         name: String,
                         gifUrl: String,
                         categoryId: UUID,
                         createdAt: Option[DateTime],
                         deletedAt: Option[DateTime])
    extends GifEntity

case class CodingLoveEntity(id: UUID,
                            name: String,
                            gifUrl: String,
                            createdAt: Option[DateTime],
                            deletedAt: Option[DateTime])
    extends GifEntity

case class RssEntity(id: UUID,
                     name: String,
                     rssUrl: String,
                     incomingToken: String,
                     createdAt: Option[DateTime],
                     updatedAt: Option[DateTime],
                     deletedAt: Option[DateTime])
    extends DbEntity

case class BotEntity(id: UUID,
                     name: String,
                     createdAt: Option[DateTime],
                     updatedAt: Option[DateTime],
                     deletedAt: Option[DateTime])
    extends DbEntity

case class BotEntityResource(id: UUID,
                             botId: UUID,
                             value: String,
                             createdAt: Option[DateTime],
                             updatedAt: Option[DateTime],
                             deletedAt: Option[DateTime])
    extends DbEntity

case class CategoryEntity(id: UUID,
                          name: String,
                          createdAt: Option[DateTime],
                          updatedAt: Option[DateTime],
                          deletedAt: Option[DateTime])
    extends DbEntity

trait JsonProtocol extends DefaultJsonProtocol {
  implicit val uuidJsonFormat: JsonFormat[UUID] = new JsonFormat[UUID] {
    override def write(x: UUID): JsValue = JsString(x.toString)

    override def read(value: JsValue): UUID = value match {
      case JsString(x) => UUID.fromString(x)
      case x =>
        throw new IllegalArgumentException("Expected UUID as JsString, but got " + x.getClass)
    }
  }

  implicit val jodaDateTimeJsonFormat: JsonFormat[DateTime] =
    new JsonFormat[DateTime] {
      override def write(x: DateTime): JsValue = JsString(x.toDateTime.toString)

      override def read(value: JsValue): DateTime = value match {
        case JsString(x) => DateTime.parse(x)
        case x => throw new IllegalArgumentException("Wrong time format of " + x)
      }
    }

}

/**
  * Implicit json conversion -> Nothing to do when we complete the object
  */
trait DatabaseEntityJsonSupport extends SprayJsonSupport with JsonProtocol {

  implicit val nineGagEntityFormat: RootJsonFormat[NineGagEntity] = jsonFormat6(NineGagEntity)
  implicit val codingLoveEntityFormat: RootJsonFormat[CodingLoveEntity] = jsonFormat5(
    CodingLoveEntity)

  implicit val rssEntityFormat: RootJsonFormat[RssEntity] = jsonFormat7(RssEntity)
  implicit val botEntityFormat: RootJsonFormat[BotEntity] = jsonFormat5(BotEntity)
  implicit val botEntityResourceFormat: RootJsonFormat[BotEntityResource] = jsonFormat6(
    BotEntityResource)
  implicit val categoryEntityFormat: RootJsonFormat[CategoryEntity] = jsonFormat5(CategoryEntity)
}
