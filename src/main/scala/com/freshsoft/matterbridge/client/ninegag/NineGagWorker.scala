package com.freshsoft.matterbridge.client.ninegag

import akka.actor.Actor
import akka.event.Logging
import com.freshsoft.matterbridge.entity.MatterBridgeEntities.{NineGagGifResult, NineGagResolveCommand}
import com.freshsoft.matterbridge.server.WithActorContext
import com.freshsoft.matterbridge.util.MatterBridgeHttpClient
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
	* The nine gag worker which is resolving the urls
	*/
class NineGagWorker extends Actor with WithActorContext {

	val log = Logging.getLogger(system, this)

	override def receive: Receive = {
		case url: String =>
			getNineGagGifs(url) onComplete {
				case Success(result) => log.info(s"Found ${result.size} gifs from $url")

					if (result.isEmpty) {
						log.info("No gifs found, proceed with next url")
						NineGagIntegration.nineGagResolver ! NineGagResolveCommand(self)
					} else {
						result.foreach(u => NineGagIntegration.nineGagGifReceiver ! u)
					}

				case Failure(ex) =>
					log.error(ex, s"Could not parse html content from url [$url]")
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
			case x if x.isEmpty => log.warning(s"Get no content from $url"); Future {Nil}
			case x if !x.isEmpty => Future {
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
