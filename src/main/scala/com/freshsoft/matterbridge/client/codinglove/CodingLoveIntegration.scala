package com.freshsoft.matterbridge.client.codinglove

import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse, StatusCodes}
import akka.util.ByteString
import com.freshsoft.matterbridge.client.IMatterBridgeResult
import com.freshsoft.matterbridge.entity.MatterBridgeEntities.SlashResponse
import com.freshsoft.matterbridge.entity.SlashCommandRequest
import com.freshsoft.matterbridge.server.WithActorContext
import com.freshsoft.matterbridge.util.WithConfig
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.concurrent.Future

/**
	* The matter bridge client which is raising the integrations
	*/
object CodingLoveIntegration extends IMatterBridgeResult
	with WithConfig
	with WithActorContext {

	val log = Logging.getLogger(system, this)

	val randomUrl = "http://thecodinglove.com/random"


	override def getResult(request: SlashCommandRequest): Future[Option[SlashResponse]] = {
		getDataFromWebsite(randomUrl, request)
	}

	/**
		* Get the response from thecodinglove web page
		*
		* @param uri     The uri we are calling
		* @param request The SlashRequest to work with
		* @return Future as Option from SlashResponse
		*/
	private def getDataFromWebsite(uri: String, request: SlashCommandRequest): Future[Option[SlashResponse]] = {
		Http().singleRequest(HttpRequest(uri = uri)).flatMap {
			case HttpResponse(StatusCodes.OK, headers, entity, _) =>
				entity.dataBytes.runFold(ByteString(""))(_ ++ _).flatMap {
					x => Future {
						x.decodeString("UTF-8") match {
							case response if response.isEmpty => log.info("NONE RESULT"); None
							case response if !response.isEmpty => log.info(response)
								Some(new SlashResponse(codingLoveResponseType, getCodingLoveResponseContent(response, request)))
						}
					}
				}
			case HttpResponse(StatusCodes.Found, headers, entity, _) =>

				val locationHeader: Option[HttpHeader] = headers.find(h => h.isInstanceOf[Location])

				locationHeader match {
					case Some(x) => getDataFromWebsite(x.asInstanceOf[Location].uri.toString, request)
					case _ => Future {
						None
					}
				}
		}
	}

	/**
		* Filter the content to fit our needs
		*
		* @param htmlContent The web result to retrieve the information
		* @param request     The request to build the final response
		* @return The response message as String
		*/
	private def getCodingLoveResponseContent(htmlContent: String, request: SlashCommandRequest) = {
		val browser = JsoupBrowser()
		val doc = browser.parseString(htmlContent)

		val text = doc >> element("div div h3")
		val gif = doc >> element("div div p img")

		text.innerHtml + "\n" + gif.attr("src") + "\nSearched for " + request.text
	}
}
