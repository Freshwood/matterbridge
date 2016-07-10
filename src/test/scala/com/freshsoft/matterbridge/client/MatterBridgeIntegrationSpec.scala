package com.freshsoft.matterbridge.client

import com.freshsoft.matterbridge.client.ninegag.NineGagIntegration
import com.freshsoft.matterbridge.entity.MattermostEntities.{NineGagGifResult, SlashResponse}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

/**
	* The matter bridge integration test
	*/
class MatterBridgeIntegrationSpec extends WordSpec with Matchers with ScalaFutures {

	// Generate Test data
	val gifResult = new NineGagGifResult("This is a test", "http://testurl.com")

	// Expected Output
	val expectedOutput = new SlashResponse("ephemeral", "9Gag Gifs [1] Last Gif:\nThis is a test\nUrl: http://testurl.com")

	NineGagIntegration.nineGagGifs += (gifResult.key -> gifResult.gifUrl)

	NineGagIntegration.lastGif = gifResult

	"The matter bridge integration" should {

		"Return a slash response" in {
			whenReady(MatterBridgeIntegration.getResult(null)) {
				result => result shouldBe a [Option[SlashResponse]]
			}
		}

		"Return a slash response from the last added nine gag gif" in {
			whenReady(MatterBridgeIntegration.getResult(null)) {
				result => result shouldBe Some(expectedOutput)
			}
		}
	}


}
