package com.freshsoft.matterbridge.entity

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol


/**
	* Here are all matterbridge defined entities
	*/
object MatterBridgeEntities {

	case class SlashResponseField(title: String, value: String, short: Boolean = false)

	case class SlashResponse(response_type: String,
	                         text: String,
	                         attachments: List[SlashResponseAttachment])

	case class SlashResponseAttachment(title: String,
	                                   text: String,
	                                   image_url: String,
	                                   fields: List[SlashResponseField],
	                                   color: String,
	                                   footer: String = "by matterbridge service")

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


	/**
		* Implicit json conversion -> Nothing to do when we complete the object
		*/
	trait ISlashCommandJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
		implicit val slashResponseFieldFormat = jsonFormat3(SlashResponseField)
		implicit val slashResponseElementFormat = jsonFormat6(SlashResponseAttachment)
		implicit val slashResponseFormat = jsonFormat3(SlashResponse)
		implicit val newsriverRecoverWebsiteFormat = jsonFormat2(NewsriverRecoverWebsite)
		implicit val newsriverResponseEntityFormat = jsonFormat2(NewsriverResponseEntity)
		implicit val newsriverResponseFormat = jsonFormat7(NewsriverResponse)
	}
}
