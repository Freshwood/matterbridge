package com.freshsoft.matterbridge.client.rss

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import akka.actor.Actor
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.DateTime
import com.freshsoft.matterbridge.entity.MatterBridgeEntities.{
  RssFeedConfigEntry,
  RssReaderIncomingModel,
  RssReaderModel
}
import com.freshsoft.matterbridge.server.MatterBridgeContext
import com.freshsoft.matterbridge.util.MatterBridgeHttpClient
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.xml.{NodeSeq, XML}

/**
	* The rss reader worker actor fetch the rss feed and build a Message for the rssReaderSenderActor
	*/
class RssReaderWorkerActor extends Actor with MatterBridgeContext {

  val log: LoggingAdapter = Logging.getLogger(system, this)

  override def receive: Receive = {
    case x: RssFeedConfigEntry =>
      retrieveRssData(x) map {
        case Some(model) => RssIntegration.rssReaderSenderActor ! model
        case None =>
          log.info("Got no rss items to send. No rss content was sent")
      }
  }

  /**
		* Retrieves the raw rss feed data and build the raw rss model to send
		*
		* @param rssConfig the rss configuration
		* @return A rss reader incoming model or none when parsing was not successful
		*/
  private def retrieveRssData(rssConfig: RssFeedConfigEntry) = {
    val rawRssData = MatterBridgeHttpClient.getUrlContent(rssConfig.url)
    rawRssData map { rssContent =>
      if (rssContent.nonEmpty) buildRssModel(rssConfig, rssContent) else None
    }
  }

  /**
		* Check if the given pudDate String of an article is newer then the old search time
		*
		* @param actualPubDate The actual rss feed item article time
		* @param lastScanDate  The last actor run time saved in a model
		* @param isRssFeed Indicator if it is an rss feed or an atom feed
		* @return true when the actual rss item (pubDate) is new
		*/
  private def isArticleNew(actualPubDate: String, lastScanDate: String, isRssFeed: Boolean) = {
    val rssFeedTime = (x: String) => OffsetDateTime.parse(x, DateTimeFormatter.RFC_1123_DATE_TIME)
    val atomFeedTime = (x: String) => OffsetDateTime.parse(x, DateTimeFormatter.ISO_DATE_TIME)

    if (isRssFeed) {
      rssFeedTime(actualPubDate).isAfter(rssFeedTime(lastScanDate))
    } else {
      atomFeedTime(actualPubDate).isAfter(rssFeedTime(lastScanDate))
    }
  }

  /**
		* Looks in the description tag for a image link
		*
		* @param description The content of a rss item description tag
		* @return A link of an image otherwise an empty string (compatibility)
		*/
  private def extractImageFromContent(description: String) = {
    try {
      val doc = JsoupBrowser().parseString(description)
      (doc >> element("img")).attr("src")
    } catch {
      case _: Throwable => ""
    }
  }

  /**
		* Build a optional RssReaderIncomingModel which belongs to a rss feed config entry
		*
		* @param rssConfig The rss config entry to retrieve the necessary information
		* @param content   The raw rss feed content as string
		* @return A optional RssReaderIncomingModel
		*/
  private def buildRssModel(rssConfig: RssFeedConfigEntry,
                            content: String): Option[RssReaderIncomingModel] = {

    try {
      val xml = XML.loadString(content)
      val items = xml \\ "item"
      val entries = xml \\ "entry"

      val isRssFeed = items.nonEmpty

      val allRssModels =
        if (isRssFeed) getRssFeedModels(rssConfig, items)
        else getAtomFeedModels(rssConfig, entries)

      val rssModels =
        allRssModels.filter(m => isArticleNew(m.pubDate, rssConfig.lastScanTime, isRssFeed))
      if (rssModels.nonEmpty)
        rssConfig.lastScanTime = DateTime.now.toRfc1123DateTimeString()

      Some(RssReaderIncomingModel(rssConfig, rssModels))

    } catch {
      case e: Throwable =>
        log.error(s"Could not parse rss content $content", e)
        None
    }
  }

  private def getRssFeedModels(rssConfig: RssFeedConfigEntry, rssNodeSeq: NodeSeq) = {
    (for {
      i <- rssNodeSeq
      title = (i \ "title").text
      link = (i \ "link").text
      pubDate = (i \ "pubDate").text
      description = (i \ "description").text
      imageLink = extractImageFromContent(description)
    } yield RssReaderModel(title, link, pubDate, description, imageLink, rssConfig.name)).toList
  }

  private def getAtomFeedModels(rssConfig: RssFeedConfigEntry, atomNodeSeq: NodeSeq) = {
    (for {
      i <- atomNodeSeq
      title = (i \ "title").text
      link = (i \ "link").text
      pubDate = (i \ "updated").text
      description = (i \ "summary").text
      imageLink = extractImageFromContent(description)
    } yield RssReaderModel(title, link, pubDate, description, imageLink, rssConfig.name)).toList
  }
}
