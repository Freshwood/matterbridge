package com.freshsoft.matterbridge.client.ninegag

import akka.actor.Actor
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.util.ByteString
import com.freshsoft.matterbridge.entity.MattermostEntities.{NineGagGifResult, NineGagResolveCommand}
import com.freshsoft.matterbridge.server.WithActorContext
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
		nineGagRawResult(url) flatMap {
			case x if x.isEmpty => log.warning(s"Get no content from $url"); Future {Nil}
			case x if !x.isEmpty => Future {
				resolveGifsFromContent(x)
			}
		}
	}

	/**
		* Get a raw http result from the provided url
		* @param url The url to retrieve the result
		* @return Raw HttpResponse UTF-8 conform String as a future
		*/
	private def nineGagRawResult(url: String) = Http().singleRequest(HttpRequest(uri = url)).flatMap {
		case HttpResponse(StatusCodes.OK, headers, entity, _) =>
			entity.dataBytes.runFold(ByteString(""))(_ ++ _).map {
				x => x.decodeString("UTF-8")
			}
		// Just return nothing when the site is down or we don't receive what we want
		// The further handling is above
		case _ => Future {""}
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