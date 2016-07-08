package com.freshsoft.matterbridge.entity

import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.model.Uri.Query

/**
	* The SlashRequest companion object to match FormData
	*/
case class SlashCommandRequest(command: String = "", username: String = "", text: String = "")

object SlashCommandRequest {

	val extractFormDataField = (x: Query, y: String) => x.find(_._1 == y)

	def apply(formData: FormData): SlashCommandRequest = {
		val command = extractFormDataField(formData.fields, "command")
		val username = extractFormDataField(formData.fields, "user_name")
		val text = extractFormDataField(formData.fields, "text")

		(command, username, text) match {
			case (Some(x), Some(y), Some(z)) => new SlashCommandRequest(x._2, y._2, z._2)
			case _ => new SlashCommandRequest
		}
	}
}
