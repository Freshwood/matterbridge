package com.freshsoft.matterbridge.client.ninegag

import akka.actor.Actor
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
	* Created by Freshwood on 03.07.2016.
	*/
object NineGagIntegration extends IRest{

	override implicit def executionContext: ExecutionContext = system.dispatcher

	val nineGagGifs: Map[String, String] = Map.empty

	private val nineGagUrl = "http://9gag.com/"

	private val nineGagRawResult = Http().singleRequest(HttpRequest(uri = nineGagUrl)).flatMap {
		case HttpResponse(StatusCodes.OK, headers, entity, _) =>
			entity.dataBytes.runFold(ByteString(""))(_ ++ _).map {
				x => x.decodeString("UTF-8")
			}
	}

	private def addGif(gif: NineGagGifResult) = {
		nineGagGifs ++ Map(gif.key -> gif.gifUrl)
	}

	class NineGagResolver extends Actor {
		override def receive: Receive = {
			case x: StartNineGagIntegration => x.worker ! StartNineGagGifSearch("Start")
			case x: NineGagGifResult => addGif(x)
		}
	}

	class NineGagWorker extends Actor {
		override def receive: Receive = {
			case x: StartNineGagGifSearch => getNineGagGifs onSuccess {
				case result => result.foreach(u => sender() ! u)
			}
		}

		private def getNineGagGifs: Future[List[NineGagGifResult]] = {
			nineGagRawResult flatMap {
				case x if x.isEmpty => Future {Nil}
				case x if !x.isEmpty => Future {
					resolveGifsFromContent(x)
				}
			}
		}

		private def resolveGifsFromContent(htmlContent: String) = {
			val browser = JsoupBrowser()
			val doc = browser.parseString(htmlContent)

			val items = doc >> element("article")

			val headers = items("header h2 a")
			val gifs = items >> elements("div a div")

			(for(h <- headers; g <- gifs) yield NineGagGifResult(h, g.attr("data-image"))).toList
		}
	}
}
