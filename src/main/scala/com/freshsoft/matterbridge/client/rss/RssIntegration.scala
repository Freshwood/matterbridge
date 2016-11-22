package com.freshsoft.matterbridge.client.rss

import akka.actor.{Actor, ActorRef, Props}
import akka.event.{Logging, LoggingAdapter}
import com.freshsoft.matterbridge.entity.MatterBridgeEntities._
import com.freshsoft.matterbridge.server.WithActorContext
import com.freshsoft.matterbridge.util.{MatterBridgeConfig, MatterBridgeHttpClient}

/**
  * The rss reader integration which does not produce a matter bridge result
  * cause it only send incoming requests to slack/mattermost
  */
object RssIntegration extends MatterBridgeConfig with WithActorContext {

  val log: LoggingAdapter = Logging.getLogger(system, this)

  val rssReaderActor: ActorRef = system.actorOf(Props(classOf[RssReaderActor]))

  val rssReaderWorkerActor: ActorRef =
    system.actorOf(Props(classOf[RssReaderWorkerActor]))

  val rssReaderSenderActor: ActorRef =
    system.actorOf(Props(classOf[RssReaderSenderActor]))

  class RssReaderSenderActor extends Actor {
    override def receive: Receive = {
      case x: RssReaderIncomingModel if x.rssReaderModels.nonEmpty =>
        val incomingResponseData = buildIncomingResponseFromRssModel(x.rssReaderModels)
        MatterBridgeHttpClient.postToIncomingWebhook(x.rssFeedConfigEntry.incoming_token, incomingResponseData)

      case y: RssReaderIncomingModel if y.rssReaderModels.isEmpty =>
        log.warning(s"Actual there are no rss items from ${y.rssFeedConfigEntry.url} to send")
    }

    private def buildIncomingResponseFromRssModel(rssReaderModels: List[RssReaderModel]) = {
      val text = s"Found ${rssReaderModels.length} rss items"

      def buildMessageAttachments(rssReaderModels: List[RssReaderModel]) = {
        for {
          m <- rssReaderModels
        } yield
          SlashResponseAttachment(m.title,
                                  m.title,
                                  m.link,
                                  m.description,
                                  m.img_url,
                                  fields = Nil,
                                  author_name = m.author)
      }

      IncomingResponse(text, buildMessageAttachments(rssReaderModels))
    }
  }

}
