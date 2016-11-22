package com.freshsoft.matterbridge.entity

import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.model.Uri.Query

/**
	* The SlashRequest companion object to match FormData
	*/
case class SlashCommandRequest(command: String = "", username: String = "", text: String = "")

object SlashCommandRequest {

  val extractFormDataField: (Query, String) => Option[(String, String)] = (x: Query, y: String) => x.find(_._1 == y)

  def apply(formData: FormData): SlashCommandRequest = {
    val command = extractFormDataField(formData.fields, "command")
    val username = extractFormDataField(formData.fields, "user_name")
    val text = extractFormDataField(formData.fields, "text")

    (command, username, text) match {
      case (Some(x), Some(y), Some(z)) =>
        new SlashCommandRequest(x._2, y._2, z._2)
      case _ => new SlashCommandRequest
    }
  }
}

/**
	* token=XXXXXXXXXXXXXXXXXX
		team_id=T0001
		team_domain=example
		channel_id=C2147483705
		channel_name=test
		timestamp=1355517523.000005
		user_id=U2147483697
		user_name=Steve
		text=googlebot: What is the air-speed velocity of an unladen swallow?
		trigger_word=googlebot:
	*/
case class OutgoingHookRequest(username: String = "", text: String = "", triggerWord: String = "")

object OutgoingHookRequest {

  val extractFormDataField: (Query, String) => Option[(String, String)] = (x: Query, y: String) => x.find(_._1 == y)

  def apply(formData: FormData): Option[OutgoingHookRequest] = {
    val triggerWord = extractFormDataField(formData.fields, "trigger_word")
    val username = extractFormDataField(formData.fields, "user_name")
    val text = extractFormDataField(formData.fields, "text")

    (username, text) match {
      case (Some(x), Some(y)) =>
        if (triggerWord.isDefined) {
          Some(OutgoingHookRequest(x._2, y._2, triggerWord.get._2))
        } else {
          Some(OutgoingHookRequest(x._2, y._2))
        }
      case _ => None
    }
  }
}
