package com.freshsoft.matterbridge.client.ninegag

import com.freshsoft.matterbridge.entity.MatterBridgeEntities.SlashResponse
import com.freshsoft.matterbridge.entity.SlashCommandRequest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Second, Span}
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

import scala.collection.mutable

/**
	* The nine gag integration test
	*/
class NineGagIntegrationTest extends WordSpec with Matchers with ScalaFutures with BeforeAndAfter {

	// Overrides default timeout used by ScalaFutures operations, like whenReady
	override implicit def patienceConfig = PatienceConfig(timeout = Span(1, Second))

	val rightRequest = new SlashCommandRequest("ninegag", "somename", "test")

	val extendedRequest = new SlashCommandRequest("ninegag", "somename", "is a")

	val complexRequest = new SlashCommandRequest("ninegag", "somename", "roflroflname")

	before {
		NineGagIntegration.nineGagGifs = mutable.LinkedHashMap.empty
		// Add a test gif
		NineGagIntegration.nineGagGifs += ("header" -> "key",
			"test" -> "gifUrl",
			"This is a big header" -> "a gif url")
	}

	"The nine gag integration" should {

		"Return a response" in {
			whenReady(NineGagIntegration.getResult(rightRequest)) {
				case Some(x) => x shouldBe a [SlashResponse]
				case None => fail("None may not returned")
			}
		}

		"Return the right filtered response" in {
			whenReady(NineGagIntegration.getResult(rightRequest)) {
				case Some(x) => assert(x.text.contains("test"))
				case None => fail("None may not returned")
			}
		}

		"Return the right filtered response which a word match" in {
			whenReady(NineGagIntegration.getResult(extendedRequest)) {
				case Some(x) => assert(x.text.contains("This is a big header"))
				case None => fail("None may not returned")
			}
		}

		"Return a random gif when the gif can not be filtered" in {
			whenReady(NineGagIntegration.getResult(complexRequest)) {
				case Some(x) => x.response_type shouldBe "in_channel"
			}
		}
	}

}
