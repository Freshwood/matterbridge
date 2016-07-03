package com.freshsoft.matterbridge.entity

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.Uri.Query
import spray.json.DefaultJsonProtocol

/**
	* Here are all matterbridge defined entities
	*/
object MattermostEntities {

	val extractFormDataField = (x: Query, y: String) => x.find(_._1 == y)

	case class SlashResponse(response_type: String, text: String)

	/**
		* Implicit json conversion -> Nothing to do when we complete the object
		*/
	trait ISlashCommandJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
		implicit val slashResponseFormat = jsonFormat2(SlashResponse)
	}
}
