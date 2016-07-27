package com.freshsoft.matterbridge.entity

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol


/**
	* Here are all matterbridge defined entities
	*/
object MatterBridgeEntities {

	case class SlashResponse(response_type: String, text: String)

	case class NineGagResolveCommand(worker: ActorRef)

	case class NineGagGifResult(key: String = "", gifUrl: String = "")

	case class NewsriverResponseEntity(primary: Boolean, url: String)

	case class NewsriverResponse(id: String,
	                             discoverDate: String,
	                             title: String,
	                             text: String,
	                             url: String,
	                             elements: List[NewsriverResponseEntity])


	/**
		* Implicit json conversion -> Nothing to do when we complete the object
		*/
	trait ISlashCommandJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
		implicit val slashResponseFormat = jsonFormat2(SlashResponse)
		implicit val newsriverResponseEntityFormat = jsonFormat2(NewsriverResponseEntity)
		implicit val newsriverResponseFormat = jsonFormat6(NewsriverResponse)
	}
}
