package com.freshsoft.matterbridge.client.rss

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestKit
import model.MatterBridgeEntities.{RssReaderActorModel, RssReaderIncomingModel, RssReaderModel}
import model.RssEntity
import org.joda.time.DateTime
import org.scalatest.{Matchers, WordSpecLike}

/**
	* The rss integration tests
	*/
class RssIntegrationTest
    extends TestKit(ActorSystem("testSystem"))
    with WordSpecLike
    with Matchers {

  val rssReaderActor: ActorRef = system.actorOf(Props(classOf[RssReaderActor]))

  val rssReaderWorkerActor: ActorRef = system.actorOf(Props(classOf[RssReaderWorkerActor]))

  val rssReaderSenderActor: ActorRef = system.actorOf(Props(classOf[RssReaderSenderActor]))

  val testRssConfigEntry =
    RssEntity(UUID.randomUUID(),
              "http://www.fbrssfeed.com/feed/5L2C8G7eoF1ihHlKpwuzvWgXPI40VdtNAyqmkjan",
              "token",
              "name",
              Some(DateTime.now()),
              None,
              None)

  val testRssReaderModel = RssReaderModel("title", "link", "pubDate", "description")

  "The rss reader actor" should {
    "not get a message back" in {
      rssReaderActor ! RssReaderActorModel.Start
      expectNoMsg()
    }
  }

  "The rss reader worker actor" should {
    "receive a config but should not send a message back" in {
      rssReaderWorkerActor ! testRssConfigEntry
      expectNoMsg()
    }
  }

  "The rss reader sender actor" should {
    "receive a rss model but should not send a message back" in {
      rssReaderSenderActor ! RssReaderIncomingModel(testRssConfigEntry, List(testRssReaderModel))
      expectNoMsg()
    }
  }
}
