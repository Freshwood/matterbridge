package com.freshsoft.matterbridge.client.codinglove

import model.MatterBridgeEntities.SlashResponse
import com.freshsoft.matterbridge.server.MatterBridgeContext
import model.SlashCommandRequest
import org.scalatest.{Matchers, WordSpec}

import scala.util.{Failure, Success}

/**
	* The coding love integration test
	*/
class CodingLoveIntegrationTest extends WordSpec with Matchers with MatterBridgeContext {

  val rightRequest = new SlashCommandRequest("codinglove", "somename", "sometext")

  "The coding love integration" should {

    "Return a slash response when a friendly request was sent" in {
      val result = CodingLoveIntegration.getResult(rightRequest)

      result onComplete {
        case Success(Some(x)) => x shouldBe a[SlashResponse]
        case Success(None)    => fail("Expected a slash response")
        case Failure(_)       => fail("Unexpected behaviour")
      }
    }
  }
}
