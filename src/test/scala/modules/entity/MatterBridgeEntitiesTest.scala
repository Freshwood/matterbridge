package modules.entity

import akka.actor.ActorSystem
import akka.testkit.TestKit
import model.MatterBridgeEntities
import model.MatterBridgeEntities._
import org.scalatest.{Matchers, WordSpecLike}

/**
	* The matter bridge entities test
	* A simple creation test of all entities in this object
	*/
class MatterBridgeEntitiesTest
    extends TestKit(ActorSystem("testSystem"))
    with WordSpecLike
    with Matchers {

  "The matter bridge entities" should {

    "successful create a slash response field" in {
      val actual = MatterBridgeEntities.SlashResponseField("Title", "Value")
      val expected = MatterBridgeEntities.SlashResponseField("Title", "Value")
      actual should be(expected)
    }

    "successful create a slash response attachment" in {
      val fields = List(SlashResponseField("Title", "Value"))
      val actual = MatterBridgeEntities.SlashResponseAttachment("Title",
                                                                "Title",
                                                                "URL",
                                                                "Text",
                                                                "img_url",
                                                                fields,
                                                                "#FFFFFF")
      val expected = MatterBridgeEntities.SlashResponseAttachment("Title",
                                                                  "Title",
                                                                  "URL",
                                                                  "Text",
                                                                  "img_url",
                                                                  fields,
                                                                  "#FFFFFF")
      actual should be(expected)
    }

    "successful create a slash incoming response" in {
      val actual = MatterBridgeEntities.IncomingResponse("Test", List())
      val expected = MatterBridgeEntities.IncomingResponse("Test", List())
      actual should be(expected)
    }

    "successful create a slash response" in {
      val actual = MatterBridgeEntities.SlashResponse("Test", "Text", List())
      val expected = MatterBridgeEntities.SlashResponse("Test", "Text", List())
      actual should be(expected)
    }

    "successful create a nine gag resolve command" in {
      val actual = MatterBridgeEntities.NineGagResolveCommand()
      val expected = MatterBridgeEntities.NineGagResolveCommand()
      actual should be(expected)
    }

    "successful create a nine gag gif result" in {
      val actual = MatterBridgeEntities.NineGagGifResult("Test", "Text", "Test")
      val expected = MatterBridgeEntities.NineGagGifResult("Test", "Text", "Test")
      actual should be(expected)
    }

    "successful create a news river response entity" in {
      val actual = MatterBridgeEntities.NewsriverResponseEntity(primary = true, "Some Text")
      val expected = MatterBridgeEntities.NewsriverResponseEntity(primary = true, "Some Text")
      actual should be(expected)
    }

    "successful create a news river response" in {
      val website = MatterBridgeEntities.NewsriverRecoverWebsite("Test", 100000)
      val element = MatterBridgeEntities.NewsriverResponseEntity(primary = true, "Some Text")
      val actual = MatterBridgeEntities.NewsriverResponse("Some Id",
                                                          "Some Date",
                                                          "Title",
                                                          "Text",
                                                          "url",
                                                          List(element),
                                                          website)
      val expected = MatterBridgeEntities.NewsriverResponse("Some Id",
                                                            "Some Date",
                                                            "Title",
                                                            "Text",
                                                            "url",
                                                            List(element),
                                                            website)
      actual should be(expected)
    }

    "successful create a rss reader raw model" in {
      val actual = RssReaderModel("title", "link", "pubDate", "description")
      val expected = RssReaderModel("title", "link", "pubDate", "description")
      actual should be(expected)
    }
  }
}
