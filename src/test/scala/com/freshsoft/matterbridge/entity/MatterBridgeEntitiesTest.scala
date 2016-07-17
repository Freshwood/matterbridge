package com.freshsoft.matterbridge.entity

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestKit
import com.freshsoft.matterbridge.client.ninegag.NineGagWorker
import org.scalatest.{Matchers, WordSpecLike}

/**
	* The matter bridge entities test
	* A simple creation test of all entities in this object
	*/
class MatterBridgeEntitiesTest extends TestKit(ActorSystem("testSystem")) with WordSpecLike with Matchers {

	"The matter bridge entities" should {

		"successful create a slash response" in {
			val actual = MatterBridgeEntities.SlashResponse("Test", "Text")
			val expected = MatterBridgeEntities.SlashResponse("Test", "Text")
			actual should be (expected)
		}

		"successful create a nine gag resolve command" in {
			val sampleActorRef = system.actorOf(Props[NineGagWorker])
			val actual = MatterBridgeEntities.NineGagResolveCommand(sampleActorRef)
			val expected = MatterBridgeEntities.NineGagResolveCommand(sampleActorRef)
			actual should be (expected)
		}

		"successful creat a nine gag gif result" in {
			val actual = MatterBridgeEntities.NineGagGifResult("Test", "Text")
			val expected = MatterBridgeEntities.NineGagGifResult("Test", "Text")
			actual should be (expected)
		}
	}

}
