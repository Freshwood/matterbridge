package com.freshsoft.matterbridge.client.rss

import akka.actor.{Actor, ActorRef, Props}
import akka.event.{Logging, LoggingAdapter}
import com.freshsoft.matterbridge.server.{MatterBridgeContext, RssConfigActorService}
import model.MatterBridgeEntities.RssReaderActorModel
import model.RssEntity

import scala.concurrent.Future

/**
	* The rss reader actor looks for the rss reader configurations and triggers the worker actor
	*/
class RssReaderActor extends Actor with MatterBridgeContext with RssConfigActorService {

  val log: LoggingAdapter = Logging.getLogger(system, this)

  val worker: ActorRef = context.actorOf(Props(classOf[RssReaderWorkerActor]))

  override def receive: Receive = {
    case RssReaderActorModel.Start =>
      feedList foreach { feeds =>
        feeds foreach { feed =>
          log.info(s"Start reading rss feed from ${feed.rssUrl} \nScan Time ${feed.updatedAt}")
          worker ! feed
          log.info(s"Reading ${feed.rssUrl} done")
        }
      }
  }

  private def feedList: Future[Seq[RssEntity]] = rssConfigService.all
}
