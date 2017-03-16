package com.freshsoft.matterbridge.client.ninegag

import akka.actor.{Actor, ActorRef}
import akka.event.{Logging, LoggingAdapter}
import com.freshsoft.matterbridge.util.MatterBridgeHttpClient
import model.MatterBridgeEntities.{NineGagGifResult, NineGagResolveCommand}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.concurrent.{ExecutionContext, Future}

/**
	* The nine gag worker which is resolving the urls
	*/
class NineGagWorker(nineGagReceiver: ActorRef) extends Actor {

  val log: LoggingAdapter = Logging.getLogger(context.system, this)

  implicit val executionContext: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case url: String =>
      val from = sender

      getNineGagGifs(url) map { result =>
        log.info(s"Found ${result.size} gifs from $url")

        if (result.isEmpty) {
          log.info("No gifs found, proceed with next url")
          from ! NineGagResolveCommand()
        } else {
          result.foreach(u => nineGagReceiver ! u)
        }
      }
  }

  /**
		* Get concurrent the List of gifs from NineGag
		*
		* @param url The url to retrieve the gifs
		* @return A Future list of NineGagGifResult
		*/
  private def getNineGagGifs(url: String): Future[List[NineGagGifResult]] = {

    MatterBridgeHttpClient.getUrlContent(url) flatMap {

      case x if x.isEmpty => log.warning(s"Get no content from $url"); Future { Nil }

      case x if !x.isEmpty =>
        Future {
          resolveGifsFromContent(x)
        }
    }
  }

  /**
		* Get the gifs from the content
		*
		* @param htmlContent The web result to retrieve the information
		* @return A list of NineGagGifResult
		*/
  @throws[Exception]
  private def resolveGifsFromContent(htmlContent: String) = {
    val browser = JsoupBrowser()
    val doc = browser.parseString(htmlContent)

    val articles = doc >> elements("article")

    val gifResults = for {
      article <- articles
      headline <- article("header h2 a")
      gifSrc <- (article >> elementList("div a div") >?> attr("data-image")("div")).flatten
    } yield (headline, gifSrc)

    gifResults.map(r => NineGagGifResult(r._1, r._2)).toList
  }
}
