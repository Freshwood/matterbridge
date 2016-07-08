package com.freshsoft.matterbridge.entity

import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.model.Uri.Query

/**
	* The outgoing web hook request companion object to match FormData
	*/
case class OutgoingHookRequest(username: String = "", text: String = "", triggerWord: String ="")

object OutgoingHookRequest {

	val extractFormDataField = (x: Query, y: String) => x.find(_._1 == y)

	def apply(formData: FormData): OutgoingHookRequest = {
		val triggerWord = extractFormDataField(formData.fields, "trigger_word")
		val username = extractFormDataField(formData.fields, "user_name")
		val text = extractFormDataField(formData.fields, "text")

		(username, text, triggerWord) match {
			case (Some(x), Some(y), Some(z)) => new OutgoingHookRequest(x._2, y._2, z._2)
			case _ => new OutgoingHookRequest
		}
	}
}
