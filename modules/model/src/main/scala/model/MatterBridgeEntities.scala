package model

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.DateTime
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/**
	* Here are all matterbridge defined entities
	*/
object MatterBridgeEntities {

  case class SlashResponseField(title: String, value: String, short: Boolean = false)

  case class SlashResponseAttachment(fallback: String,
                                     title: String,
                                     title_link: String,
                                     text: String,
                                     image_url: String,
                                     fields: List[SlashResponseField],
                                     color: String = "#764FA5",
                                     pretext: String = "",
                                     author_name: String = "",
                                     author_icon: String = "",
                                     author_link: String = "")

  case class SlashResponse(response_type: String,
                           text: String,
                           attachments: List[SlashResponseAttachment])

  case class IncomingResponse(text: String, attachments: List[SlashResponseAttachment])

  case class OutgoingResponse(text: String)

  case class NineGagResolveCommand(worker: ActorRef)

  case class NineGagGifResult(key: String = "", gifUrl: String = "")

  case class NewsriverRecoverWebsite(domainName: String, rankingGlobal: Int)

  case class NewsriverResponseEntity(primary: Boolean, url: String)

  case class NewsriverResponse(id: String,
                               discoverDate: String,
                               title: String,
                               text: String,
                               url: String,
                               elements: List[NewsriverResponseEntity],
                               website: NewsriverRecoverWebsite)

  case class RssFeedConfigEntry(url: String,
                                incoming_token: String,
                                name: String,
                                var lastScanTime: String =
                                  DateTime.now.minus(86400000).toRfc1123DateTimeString())

  case class RssReaderIncomingModel(rssFeedConfigEntry: RssFeedConfigEntry,
                                    rssReaderModels: List[RssReaderModel])

  case class RssReaderModel(title: String,
                            link: String,
                            pubDate: String,
                            description: String,
                            img_url: String = "",
                            author: String = "")

  object RssReaderActorModel extends Enumeration(initial = 0) {
    val Start = Value
  }

  /**
		* Implicit json conversion -> Nothing to do when we complete the object
		*/
  trait ISlashCommandJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
    implicit val slashResponseFieldFormat: RootJsonFormat[SlashResponseField] = jsonFormat3(
      SlashResponseField)
    implicit val slashResponseElementFormat: RootJsonFormat[SlashResponseAttachment] =
      jsonFormat11(SlashResponseAttachment)
    implicit val slashResponseFormat: RootJsonFormat[SlashResponse] = jsonFormat3(SlashResponse)
    implicit val incomingResponseFormat: RootJsonFormat[IncomingResponse] = jsonFormat2(
      IncomingResponse)
    implicit val outgoingResponseFormat: RootJsonFormat[OutgoingResponse] = jsonFormat1(
      OutgoingResponse)
    implicit val newsriverRecoverWebsiteFormat: RootJsonFormat[NewsriverRecoverWebsite] =
      jsonFormat2(NewsriverRecoverWebsite)
    implicit val newsriverResponseEntityFormat: RootJsonFormat[NewsriverResponseEntity] =
      jsonFormat2(NewsriverResponseEntity)
    implicit val newsriverResponseFormat: RootJsonFormat[NewsriverResponse] = jsonFormat7(
      NewsriverResponse)
    implicit val nineGagGifResultResponseFormat: RootJsonFormat[NineGagGifResult] = jsonFormat2(
      NineGagGifResult)
  }
}
