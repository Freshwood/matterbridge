package com.freshsoft.matterbridge.client.newsriver

import akka.http.scaladsl.model.HttpHeader
import com.freshsoft.matterbridge.entity.MatterBridgeEntities.SlashResponse
import com.freshsoft.matterbridge.entity.SlashCommandRequest
import com.freshsoft.matterbridge.server.WithActorContext
import org.scalatest.{Matchers, WordSpec}


/**
	* The newsriver integration test
	*/
class NewsriverIntegrationTest extends WordSpec with Matchers with WithActorContext {

	val rightRequest = new SlashCommandRequest("newsriver", "somename", "sometext")

	"The newsriver integration" should {

		"Return a slash response when a friendly request was sent" in {
			val result = NewsriverIntegration.getResult(rightRequest)

			result onSuccess {
				case Some(x) => x shouldBe a [SlashResponse]
			}
		}

		"have the correct api header" in {
			NewsriverIntegration.apiHeader shouldBe a [HttpHeader]
			NewsriverIntegration.apiHeader.value() should be (NewsriverIntegration.apiToken)
		}
	}
}