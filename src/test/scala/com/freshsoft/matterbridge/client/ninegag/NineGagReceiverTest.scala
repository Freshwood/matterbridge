package com.freshsoft.matterbridge.client.ninegag

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestKit
import com.freshsoft.matterbridge.entity.MattermostEntities.NineGagGifResult
import org.scalatest.{Matchers, WordSpecLike}

/**
	* Created by Freshwood on 15.07.2016.
	*/
class NineGagReceiverTest extends TestKit(ActorSystem("testSystem")) with WordSpecLike with Matchers {

	val expectedMessage = NineGagGifResult("Sample", "Some String")

	val nineGagReceiver = system.actorOf(Props(classOf[NineGagIntegration.NineGagGifReceiver]))

	"The nine gag receiver actor" should {
		"not send a message back" in {
			nineGagReceiver ! expectedMessage
			expectNoMsg()
		}
	}
}
