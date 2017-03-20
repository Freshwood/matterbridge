package com.freshsoft.matterbridge.client.rss

import akka.actor.Actor
import akka.event.{Logging, LoggingAdapter}
import model.MatterBridgeEntities.{
  IncomingResponse,
  RssReaderIncomingModel,
  RssReaderModel,
  SlashResponseAttachment
}
import com.freshsoft.matterbridge.server.MatterBridgeContext
import com.freshsoft.matterbridge.util.MatterBridgeHttpClient

/**
	* The rss reader sender actor which is sending incoming requests to slack/mattermost
	*/
class RssReaderSenderActor extends Actor with MatterBridgeContext {

  val log: LoggingAdapter = Logging.getLogger(system, this)

  override def receive: Receive = {
    case x: RssReaderIncomingModel if x.rssReaderModels.nonEmpty =>
      val incomingResponseData = buildIncomingResponseFromRssModel(x.rssReaderModels)
      MatterBridgeHttpClient
        .postToIncomingWebhook(x.rssFeedConfigEntry.incomingToken, incomingResponseData)

    case y: RssReaderIncomingModel if y.rssReaderModels.isEmpty =>
      log.warning(s"Actual there are no rss items from ${y.rssFeedConfigEntry.rssUrl} to send")
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
