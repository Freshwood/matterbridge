package com.freshsoft.matterbridge.entity

import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.model.Uri.Query

/**
	* The SlashRequest companion object to match FormData
	*/
object SlashRequest {

	case class SlashRequest(command: String = "", username: String = "", text: String = "")

	val extractFormDataField = (x: Query, y: String) => x.find(_._1 == y)

	def apply(formData: FormData): SlashRequest = {
		val command = extractFormDataField(formData.fields, "command")
		val username = extractFormDataField(formData.fields, "user_name")
		val text = extractFormDataField(formData.fields, "text")

		(command, username, text) match {
			case (Some(x), Some(y), Some(z)) => new SlashRequest(x._2, y._2, z._2)
			case _ => new SlashRequest
		}
	}
}
