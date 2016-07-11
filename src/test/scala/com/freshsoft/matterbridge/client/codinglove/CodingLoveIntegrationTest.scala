package com.freshsoft.matterbridge.client.codinglove

import com.freshsoft.matterbridge.entity.SlashCommandRequest
import com.freshsoft.matterbridge.server.WithActorContext
import org.scalatest.{Matchers, WordSpec}

/**
	* The coding love integration test
	*/
class CodingLoveIntegrationTest extends WordSpec with Matchers with WithActorContext {

	val rightRequest = new SlashCommandRequest("codinglove", "somename", "sometext")

	"The coding love integration" should {

		"Return a slash response when a friendly request was sent" in {
			val result = CodingLoveIntegration.getResult(rightRequest)

			result onSuccess {
				case Some(x) => x.response_type shouldBe "in_channel"
			}
		}
	}
}
