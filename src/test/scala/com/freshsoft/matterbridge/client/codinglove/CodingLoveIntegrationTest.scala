package com.freshsoft.matterbridge.client.codinglove

import com.freshsoft.matterbridge.entity.MatterBridgeEntities.SlashResponse
import com.freshsoft.matterbridge.entity.SlashCommandRequest
import com.freshsoft.matterbridge.server.MatterBridgeContext
import org.scalatest.{Matchers, WordSpec}

/**
	* The coding love integration test
	*/
class CodingLoveIntegrationTest extends WordSpec with Matchers with MatterBridgeContext {

  val rightRequest = new SlashCommandRequest("codinglove", "somename", "sometext")

  "The coding love integration" should {

    "Return a slash response when a friendly request was sent" in {
      val result = CodingLoveIntegration.getResult(rightRequest)

      result onSuccess {
        case Some(x) => x shouldBe a[SlashResponse]
      }
    }
  }
}
