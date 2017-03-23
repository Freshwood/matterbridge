package com.freshsoft.matterbridge.client.ninegag

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestKit
import model.MatterBridgeEntities.NineGagGifResult
import org.scalatest._

/**
	* The nine gag receiver test
	*/
class NineGagReceiverTest
    extends TestKit(ActorSystem("testSystem"))
    with WordSpecLike
    with Matchers
    with BeforeAndAfter {

  val expectedMessage = NineGagGifResult("Sample", "Some String", "Test")

  val nineGagReceiver: ActorRef =
    system.actorOf(Props(classOf[NineGagIntegration.NineGagGifReceiver]))

  before {}

  "The nine gag receiver actor" should {
    "not send a message back" in {
      nineGagReceiver ! expectedMessage
      expectNoMsg()
    }

    "store a gif" in {
      nineGagReceiver ! expectedMessage
      expectNoMsg()
    }
  }
}
