package com.freshsoft.matterbridge.client.ninegag

import akka.actor.{Actor, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.util.ByteString
import com.freshsoft.matterbridge.client.IMatterBridgeResult
import com.freshsoft.matterbridge.entity.MattermostEntities.{NineGagGifResult, SlashResponse, StartNineGagGifSearch, StartNineGagIntegration}
import com.freshsoft.matterbridge.entity.SlashRequest
import com.freshsoft.matterbridge.server.WithActorContext
import com.freshsoft.matterbridge.util.WithConfig
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.concurrent.Future
import scala.util.{Failure, Random, Success}

/**
	* The nine gag integration which is searching in the background for gifs
	*/
object NineGagIntegration extends IMatterBridgeResult with WithConfig with WithActorContext{

	val log = Logging.getLogger(system, this)

	val nineGagResolver = system.actorOf(Props(classOf[NineGagResolver]))

	val nineGagWorker = system.actorOf(Props(classOf[NineGagWorker]))

	var nineGagGifs: Map[String, String] = Map.empty

	private val nineGagBaseUrl = "http://9gag.com/"

	private var previousUrl = nineGagBaseUrl

	private val nineGagCategories = Seq("funny/", "wtf/", "gif/", "gaming/", "anime-manga/",
		"movie-tv/", "cute/", "girl/", "awesome/", "cosplay/", "sport/", "food/", "timely/")

	private val nineGagExtraCategory = "fresh/"

	private val nineGagUrls = Seq(nineGagBaseUrl) ++ nineGagCategories.flatMap(e => Seq(nineGagBaseUrl + e) ++ Seq(nineGagBaseUrl + e + nineGagExtraCategory)).toList

	/**
		* Get the next url in the list
		*
		* @param previousUrl The previous url to cut
		* @return The next url as String
		*/
	private def getNextNineGagUrl(previousUrl: String): String = {
		val list = nineGagUrls.splitAt(nineGagUrls.indexOf(previousUrl) + 1)

		list._2.headOption match {
			case Some(x) => setNextNineGagUrl(x); x
			case None => setNextNineGagUrl(nineGagUrls.head); nineGagUrls.head
		}
	}

	private def setNextNineGagUrl(nextUrl: String) = {
		previousUrl = nextUrl
	}

	private def nineGagRawResult(url: String) = Http().singleRequest(HttpRequest(uri = url)).flatMap {
		case HttpResponse(StatusCodes.OK, headers, entity, _) =>
			entity.dataBytes.runFold(ByteString(""))(_ ++ _).map {
				x => x.decodeString("UTF-8")
			}
	}

	/**
		* Add a new NineGagGifResult for the matterbridge api
		*
		* @param gif A NineGagGifResult
		*/
	private def addGif(gif: NineGagGifResult) = {
		val oldSize = nineGagGifs.size
		nineGagGifs += (gif.key -> gif.gifUrl)
		if (oldSize != nineGagGifs.size) log.info(s"Added new gif [${gif.gifUrl}]. " +
			s"Actual size [${nineGagGifs.size}]")
	}

	/**
		* The resolver handles the worker and communicate with it
		*/
	class NineGagResolver extends Actor {
		override def receive: Receive = {
			case x: StartNineGagIntegration => x.worker ! StartNineGagGifSearch(x.command)
			case x: NineGagGifResult => addGif(x)
		}
	}

	class NineGagWorker extends Actor {

		override def receive: Receive = {
			case x: StartNineGagGifSearch =>
				val nextUrl = getNextNineGagUrl(previousUrl)
				getNineGagGifs(nextUrl) onComplete {
					case Success(result) => result.foreach(u => nineGagResolver ! u)
					case Failure(ex) =>
						log.error(ex, "Could not parse html content")
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

	override def getResult(request: SlashRequest): Future[Option[SlashResponse]] = {

		val responseText = (x: (String, String), y: String) => s"${x._1} \n ${x._2}\nSearched for $y"

		val getSpecificMap = (x: Vector[(String, String)]) => x(Random.nextInt(x.size))

		// Search for every word to as fallback
		val words = request.text.split("\\W+")

		 Future {
			 nineGagGifs.filter(p => p._1.contains(request.text) || p._1.contains(request.text) || p._1.contains(words)) match {
				 case x: Map[String, String] if x.nonEmpty => Some(SlashResponse(nineGagResponseType, responseText(getSpecificMap(x.toVector), request.text)))
				 // Nothing found then search further
				 case _ => getSpecificMap(nineGagGifs.toVector) match {
					 case y: (String, String) => Some(SlashResponse(nineGagResponseType, responseText(y, request.text)))
					 case _ => None
				 }
			 }
		 }
	}
}
