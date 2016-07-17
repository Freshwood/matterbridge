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

	/**
		* Implicit json conversion -> Nothing to do when we complete the object
		*/
	trait ISlashCommandJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
		implicit val slashResponseFormat = jsonFormat2(SlashResponse)
	}
}
