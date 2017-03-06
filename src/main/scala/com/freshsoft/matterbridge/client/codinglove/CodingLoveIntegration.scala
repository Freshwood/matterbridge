package com.freshsoft.matterbridge.client.codinglove

import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.util.ByteString
import com.freshsoft.matterbridge.client.IMatterBridgeResult
import com.freshsoft.matterbridge.entity.MatterBridgeEntities.SlashResponse
import com.freshsoft.matterbridge.entity.SlashCommandRequest
import com.freshsoft.matterbridge.server.MatterBridgeContext
import com.freshsoft.matterbridge.util.MatterBridgeConfig
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.concurrent.Future

/**
	* The matter bridge client which is raising the integrations
	*/
object CodingLoveIntegration
    extends IMatterBridgeResult
    with MatterBridgeConfig
    with MatterBridgeContext {

  private val log = Logging.getLogger(system, this)

  private val randomUrl = "http://thecodinglove.com/random"

  private val browser = JsoupBrowser()

  override def getResult(request: SlashCommandRequest): Future[Option[SlashResponse]] =
    getDataFromWebsite(randomUrl, request)

  /**
		* Get the response from thecodinglove web page
		*
		* @param uri     The uri we are calling
		* @param request The SlashRequest to work with
		* @return Future as Option from SlashResponse
		*/
  private def getDataFromWebsite(uri: String,
                                 request: SlashCommandRequest): Future[Option[SlashResponse]] = {
    Http().singleRequest(HttpRequest(uri = uri)).flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { response =>
          buildSlashResponse(response.decodeString("UTF-8"))
        }
      case HttpResponse(StatusCodes.Found, headers, _, _) =>
        val result: Future[Option[SlashResponse]] = headers.find(_.isInstanceOf[Location]) map {
          case header: Location => getDataFromWebsite(header.uri.toString, request)
        } getOrElse Future.successful(None)

        result
    }
  }

  private def buildSlashResponse: PartialFunction[String, Option[SlashResponse]] = {
    case response if response.isEmpty =>
      log.info(s"Got no result from coding love"); None
    case response if response.nonEmpty =>
      log.debug(response)
      Some(
        SlashResponse(codingLoveResponseType,
                      getCodingLoveResponseContent(response, null),
                      List()))
  }

  /**
		* Filter the content to fit our needs
		*
		* @param htmlContent The web result to retrieve the information
		* @param request     The request to build the final response
		* @return The response message as String
		*/
  private def getCodingLoveResponseContent(htmlContent: String, request: SlashCommandRequest) = {
    val doc = browser.parseString(htmlContent)

    val text = doc >> element("div div h3")
    val gif = doc >> element("div div p img")

    text.innerHtml + "\n" + gif.attr("src") + "\nSearched for " + request.text
  }
}
