package com.freshsoft.matterbridge.client.newsriver

import akka.http.scaladsl.model.HttpHeader
import com.freshsoft.matterbridge.entity.MatterBridgeEntities.SlashResponse
import com.freshsoft.matterbridge.entity.SlashCommandRequest
import com.freshsoft.matterbridge.server.MatterBridgeContext
import com.freshsoft.matterbridge.util.MatterBridgeConfig
import org.scalatest.{Matchers, WordSpec}

/**
	* The newsriver integration test
	*/
class NewsriverIntegrationTest
    extends WordSpec
    with Matchers
    with MatterBridgeContext
    with MatterBridgeConfig {

  val rightRequest = new SlashCommandRequest("news", "somename", "test")

  "The newsriver integration" should {

    "Return a slash response when a friendly request was sent" in {
      val result = NewsriverIntegration.getResult(rightRequest)

      result onSuccess {
        case Some(x) => x shouldBe a[SlashResponse]
        case None => fail("There should be `SlashResponse` available")
      }
    }

    "have the correct api header" in {
      NewsriverIntegration.apiHeader shouldBe a[HttpHeader]
      NewsriverIntegration.apiHeader.value() should be(newsriverApiToken)
    }
  }
}
