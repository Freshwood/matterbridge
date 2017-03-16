package com.freshsoft.matterbridge.client.ninegag

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import model.MatterBridgeEntities.NineGagResolveCommand
import org.scalatest.{MustMatchers, WordSpecLike}

/**
	* The nine gag resolver test
	*/
class NineGagResolverTest
    extends TestKit(ActorSystem("testSystem"))
    with WordSpecLike
    with MustMatchers {

  val expectedMessage = "http://9gag.com/funny/"

  "The nine gag resolver actor" must {
    "send the next url to the worker actor" in {
      // Creation of the TestActorRef
      val probe = TestProbe()
      val source = system.actorOf(Props[NineGagResolver])
      val dest = system.actorOf(Props[NineGagWorker])

      source ! NineGagResolveCommand()
      probe.expectMsg(expectedMessage)
      probe.forward(dest)
    }

  }
}
