package com.freshsoft.matterbridge.client.ninegag

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestKit
import org.scalatest.{Matchers, WordSpecLike}

/**
	* Created by Freshwood on 14.07.2016.
	*/
class NineGagWorkerTest extends TestKit(ActorSystem("testSystem")) with WordSpecLike with Matchers {

	val expectedMessage = "http://9gag.com/funny/"

	val nineGagWorker = system.actorOf(Props(classOf[NineGagWorker]))

	"The nine gag worker actor" should {
		"forward messages to other actors" in {
			nineGagWorker ! expectedMessage
			expectNoMsg()
		}
	}
}