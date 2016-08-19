package com.freshsoft.matterbridge.entity

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.DateTime
import spray.json.DefaultJsonProtocol


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
	                              var lastScanTime: String = DateTime.now.minus(86400000).toRfc1123DateTimeString())

	case class RssReaderIncomingModel(rssFeedConfigEntry: RssFeedConfigEntry,
	                                  rssReaderModels: List[RssReaderModel])

	case class RssReaderModel(title: String, link: String, pubDate: String, description: String, img_url: String = "")

	object RssReaderActorModel extends Enumeration(initial = 0) {
		val Start = Value
	}

	/**
		* Implicit json conversion -> Nothing to do when we complete the object
		*/
	trait ISlashCommandJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
		implicit val slashResponseFieldFormat = jsonFormat3(SlashResponseField)
		implicit val slashResponseElementFormat = jsonFormat11(SlashResponseAttachment)
		implicit val slashResponseFormat = jsonFormat3(SlashResponse)
		implicit val incomingResponseFormat = jsonFormat2(IncomingResponse)
		implicit val newsriverRecoverWebsiteFormat = jsonFormat2(NewsriverRecoverWebsite)
		implicit val newsriverResponseEntityFormat = jsonFormat2(NewsriverResponseEntity)
		implicit val newsriverResponseFormat = jsonFormat7(NewsriverResponse)
	}
}
