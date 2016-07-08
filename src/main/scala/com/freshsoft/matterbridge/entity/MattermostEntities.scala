package com.freshsoft.matterbridge.entity

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.Uri.Query
import spray.json.DefaultJsonProtocol

/**
	* Here are all matterbridge defined entities
	*/
object MattermostEntities {

	val extractFormDataField = (x: Query, y: String) => x.find(_._1 == y)

	case class SlashResponse(response_type: String, text: String)

	case class OutgoingResponse(text: String)

	case class StartNineGagIntegration(command: String, worker: ActorRef)

	case class StartNineGagGifSearch(command: String)

	case class NineGagGifResult(key: String, gifUrl: String)

	/**
		* Implicit json conversion -> Nothing to do when we complete the object
		*/
	trait ISlashCommandJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
		implicit val slashResponseFormat = jsonFormat2(SlashResponse)
		implicit val outgoingResponseFormat = jsonFormat1(OutgoingResponse)
	}
}
