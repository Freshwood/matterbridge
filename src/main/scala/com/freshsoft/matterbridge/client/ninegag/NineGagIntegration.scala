package com.freshsoft.matterbridge.client.ninegag

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.util.ByteString
import com.freshsoft.matterbridge.entity.MattermostEntities.{NineGagGifResult, StartNineGagGifSearch, StartNineGagIntegration}
import com.freshsoft.matterbridge.server.IRest
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.concurrent.{ExecutionContext, Future}

/**
	* The nine gag integration which is searching in the background for gifs
	*/
object NineGagIntegration extends IRest{

	override implicit def executionContext: ExecutionContext = system.dispatcher

	val nineGagResolver = system.actorOf(Props(classOf[NineGagResolver]))

	val nineGagWorker = system.actorOf(Props(classOf[NineGagWorker]))

	var nineGagGifs: Map[String, String] = Map.empty

	private val nineGagBaseUrl = "http://9gag.com/"

	private val nineGagCategories = Seq("funny/", "wtf/", "gif/", "gaming/", "anime-manga/",
		"movie-tv/", "cute/", "girl/", "awesome/", "cosplay/", "sport/", "food/", "timely/")

	private val nineGagExtraCategory = "fresh/"

	private def nineGagRawResult(url: String) = Http().singleRequest(HttpRequest(uri = url)).flatMap {
		case HttpResponse(StatusCodes.OK, headers, entity, _) =>
			entity.dataBytes.runFold(ByteString(""))(_ ++ _).map {
				x => x.decodeString("UTF-8")
			}
	}

	private def addGif(gif: NineGagGifResult) = {
		nineGagGifs += (gif.key -> gif.gifUrl)
	}

	class NineGagResolver extends Actor {
		override def receive: Receive = {
			case x: StartNineGagIntegration => x.worker ! StartNineGagGifSearch(x.command)
			case x: NineGagGifResult => addGif(x)
		}
	}

	class NineGagWorker extends Actor {

		override def receive: Receive = {
			case x: StartNineGagGifSearch => getNineGagGifs onSuccess {
				case result => result.foreach(u => nineGagResolver ! u)
			}
		}

		private def getNineGagGifs: Future[List[NineGagGifResult]] = {
			nineGagRawResult(nineGagBaseUrl) flatMap {
				case x if x.isEmpty => Future {Nil}
				case x if !x.isEmpty => Future {
					resolveGifsFromContent(x)
				}
			}
		}

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
}
