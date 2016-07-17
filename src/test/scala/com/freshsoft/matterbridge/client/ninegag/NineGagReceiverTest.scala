package com.freshsoft.matterbridge.client.ninegag

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestKit
import com.freshsoft.matterbridge.entity.MatterBridgeEntities.NineGagGifResult
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}

import scala.collection.mutable

/**
	* Created by Freshwood on 15.07.2016.
	*/
class NineGagReceiverTest extends TestKit(ActorSystem("testSystem"))
	with WordSpecLike
	with Matchers
	with BeforeAndAfter {

	val expectedMessage = NineGagGifResult("Sample", "Some String")

	val nineGagReceiver = system.actorOf(Props(classOf[NineGagIntegration.NineGagGifReceiver]))

	before {
		NineGagIntegration.nineGagGifs = mutable.LinkedHashMap.empty
		NineGagIntegration.lastGif = new NineGagGifResult
	}

	"The nine gag receiver actor" should {
		"not send a message back" in {
			nineGagReceiver ! expectedMessage
			expectNoMsg()
		}

		"store a gif" in {
			nineGagReceiver ! expectedMessage
			expectNoMsg()
			NineGagIntegration.nineGagGifs.size should be (1)
			NineGagIntegration.lastGif should be (expectedMessage)
		}

		"adjust the gif store" in {
			// In the test settings the maximum gif store is at 10
			for(i <- 1 to 100) {
				// prepare message
				val message = NineGagGifResult(expectedMessage.key + i, expectedMessage.gifUrl)
				nineGagReceiver ! message
			}

			val lastMessage = NineGagGifResult(expectedMessage.key + 100, expectedMessage.gifUrl)
			expectNoMsg()
			NineGagIntegration.nineGagGifs.size should be (10)
			NineGagIntegration.lastGif should be (lastMessage)
		}
	}
}
