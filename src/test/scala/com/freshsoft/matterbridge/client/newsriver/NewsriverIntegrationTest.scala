package com.freshsoft.matterbridge.client.newsriver

import com.freshsoft.matterbridge.entity.MatterBridgeEntities.SlashResponse
import com.freshsoft.matterbridge.entity.SlashCommandRequest
import com.freshsoft.matterbridge.server.WithActorContext
import org.scalatest.{Matchers, WordSpec}


/**
	* The coding love integration test
	*/
class NewsriverIntegrationTest extends WordSpec with Matchers with WithActorContext {

	val rightRequest = new SlashCommandRequest("newsriver", "somename", "sometext")

	"The coding love integration" should {

		"Return a slash response when a friendly request was sent" in {
			val result = NewsriverIntegration.getResult(rightRequest)

			result onSuccess {
				case Some(x) => x shouldBe a [SlashResponse]
			}
		}
	}
}