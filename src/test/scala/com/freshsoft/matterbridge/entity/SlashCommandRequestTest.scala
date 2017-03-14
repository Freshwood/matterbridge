package com.freshsoft.matterbridge.entity

import akka.http.scaladsl.model.FormData
import model.SlashCommandRequest
import org.scalatest.{Matchers, WordSpec}

/**
	* The slash command request companion object test
	*/
class SlashCommandRequestTest extends WordSpec with Matchers {

  "The slash command request object" should {

    "successful create a pre initialized object" in {
      val actual = new SlashCommandRequest
      actual.command should be("")
      actual.text should be("")
      actual.username should be("")
    }

    "extract form data values and parse the values to the object" in {
      val sampleFormData =
        FormData("command" -> "test", "user_name" -> "username", "text" -> "text")
      val actual = SlashCommandRequest(sampleFormData)
      actual.command should be("test")
      actual.username should be("username")
      actual.text should be("text")
    }
  }
}
