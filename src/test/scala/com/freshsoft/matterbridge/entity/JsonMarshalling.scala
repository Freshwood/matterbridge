package com.freshsoft.matterbridge.entity

import com.freshsoft.matterbridge.entity.MatterBridgeEntities._
import com.freshsoft.matterbridge.server.MatterBridgeContext
import org.scalatest.{Matchers, WordSpec}
import spray.json._

/**
	* Test the json unmarshaller/marshaller from `ISlashCommandJsonSupport`
	*/
class JsonMarshalling
    extends WordSpec
    with Matchers
    with MatterBridgeContext
    with ISlashCommandJsonSupport {

  "The spray json marshalling" should {

    val slashResponseField = SlashResponseField("Title", "String")

    val slashResponseElement = SlashResponseAttachment("Title",
                                                       "Title",
                                                       "URL",
                                                       "Text",
                                                       "img_url",
                                                       List(slashResponseField),
                                                       "#FFFFFF")

    val slashResponse = SlashResponse("in_channel", "Text", List(slashResponseElement))

    val incomingResponse = IncomingResponse("Text", List(slashResponseElement))

    val newsriverRecoverWebsite = NewsriverRecoverWebsite("http://some.com", 10000)

    val newsriverResponseEntity = NewsriverResponseEntity(primary = true, "http://something")

    val newsriverResponse = NewsriverResponse("Some Id",
                                              "Some Date",
                                              "Title",
                                              "Text",
                                              "url",
                                              List(newsriverResponseEntity),
                                              newsriverRecoverWebsite)

    "correct marshal slash response field" in {
      val actual = slashResponseField.toJson.toString
      slashResponseField should be(actual.parseJson.convertTo[SlashResponseField])
    }

    "correct marshal slash response element" in {
      val actual = slashResponseElement.toJson.toString
      slashResponseElement should be(actual.parseJson.convertTo[SlashResponseAttachment])
    }

    "correct marshal incoming response" in {
      val actual = incomingResponse.toJson.toString
      incomingResponse should be(actual.parseJson.convertTo[IncomingResponse])
    }

    "correct marshal slash response" in {
      val actual = slashResponse.toJson.toString
      slashResponse should be(actual.parseJson.convertTo[SlashResponse])
    }

    "correct marshal newsriver recover website object" in {
      val actual = newsriverRecoverWebsite.toJson.toString
      newsriverRecoverWebsite should be(actual.parseJson.convertTo[NewsriverRecoverWebsite])
    }

    "correct marshal newsriver entity" in {
      val actual = newsriverResponseEntity.toJson.toString
      newsriverResponseEntity should be(actual.parseJson.convertTo[NewsriverResponseEntity])
    }

    "correct marshal newsriver response" in {
      val actual = newsriverResponse.toJson.toString
      newsriverResponse should be(actual.parseJson.convertTo[NewsriverResponse])
    }
  }
}
