package com.freshsoft.matterbridge.client.rss

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import akka.actor.{Actor, Props}
import akka.event.Logging
import akka.http.scaladsl.model.DateTime
import com.freshsoft.matterbridge.entity.MatterBridgeEntities._
import com.freshsoft.matterbridge.server.WithActorContext
import com.freshsoft.matterbridge.util.{MatterBridgeHttpClient, WithConfig}

import scala.xml.XML

/**
	* The rss reader integration which does not produce a matter bridge result
	* cause it only send incoming requests to slack/mattermost
	*/
object RssIntegration extends WithConfig with WithActorContext {

	val log = Logging.getLogger(system, this)

	val rssReaderActor = system.actorOf(Props(classOf[RssReaderActor]))

	val rssReaderWorkerActor = system.actorOf(Props(classOf[RssReaderWorkerActor]))

	val rssReaderSenderActor = system.actorOf(Props(classOf[RssReaderSenderActor]))

	class RssReaderActor extends Actor {
		override def receive: Receive = {
			case RssReaderActorModel.Start => rssFeedList foreach { feed =>
				log.info(s"Start reading rss feed from ${feed.url} \nScan Time ${feed.lastScanTime}")
				rssReaderWorkerActor ! feed
				log.info(s"Reading ${feed.url} done")
			}
		}
	}

	class RssReaderWorkerActor extends Actor {

		override def receive: Receive = {
			case x: RssFeedConfigEntry => retrieveRssData(x) map {
				case Some(model) => rssReaderSenderActor ! model
				case None => log.info("Got no rss items to send. No rss content was sent")
			}
		}

		private def retrieveRssData(rssConfig: RssFeedConfigEntry) = {
			val rssData = MatterBridgeHttpClient.getUrlContent(rssConfig.url)
			rssData map { rssContent =>
				if (rssContent.nonEmpty) buildRssModel(rssConfig, rssContent) else None
			}
		}

		private def getParsedTime(pubDate: String) = {
			OffsetDateTime.parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME)
		}

		private def isArticleNew(actualPubDate: String, lastScanDate: String) = {
			getParsedTime(actualPubDate).isAfter(getParsedTime(lastScanDate))
		}

		private def buildRssModel(rssConfig: RssFeedConfigEntry, content: String): Option[RssReaderIncomingModel] = {

			try {
				val xml = XML.loadString(content)
				val items = xml \\ "item"

				val allRssModels = (for {
					i <- items
					title = (i \ "title").text
					link = (i \ "link").text
					pubDate = (i \ "pubDate").text
					description = (i \ "description").text
				} yield RssReaderModel(title, link, pubDate, description)).toList

				val rssModels = allRssModels.filter(m => isArticleNew(m.pubDate, rssConfig.lastScanTime))
				rssConfig.lastScanTime = DateTime.now.toRfc1123DateTimeString()

				// TODO: Fix UTC Bug

				Some(RssReaderIncomingModel(rssConfig, rssModels))
			} catch {
				case e: Exception => log.error(s"Could not parse rss content $content", e)
					None
			}
		}
	}

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
				} yield SlashResponseAttachment(m.title, m.title, m.link, m.description, m.img_url, fields = Nil)
			}

			IncomingResponse(text, buildMessageAttachments(rssReaderModels))
		}
	}
}
