package com.freshsoft.matterbridge.client.codinglove

import java.util.UUID

import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.util.ByteString
import com.freshsoft.matterbridge.client.IMatterBridgeResult
import model.MatterBridgeEntities.SlashResponse
import com.freshsoft.matterbridge.server.{CodingLoveActorService, MatterBridgeContext}
import com.freshsoft.matterbridge.util.MatterBridgeConfig
import model.{CodingLoveEntity, SlashCommandRequest}
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
    with MatterBridgeContext
    with CodingLoveActorService {

  private val log = Logging.getLogger(system, this)

  private val randomUrl = "http://thecodinglove.com/random"

  private lazy val browser = JsoupBrowser()

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
          buildSlashResponse((response.decodeString("UTF-8"), request))
        }
      case HttpResponse(StatusCodes.Found, headers, _, _) =>
        val result: Future[Option[SlashResponse]] = headers.find(_.isInstanceOf[Location]) map {
          case header: Location => getDataFromWebsite(header.uri.toString, request)
        } getOrElse Future.successful(None)

        result
    }
  }

  private def buildSlashResponse
    : PartialFunction[(String, SlashCommandRequest), Option[SlashResponse]] = {
    case (response, _) if response.isEmpty =>
      log.info(s"Got no result from coding love"); None
    case (response, x) if response.nonEmpty =>
      log.debug(response)
      val codingLoveEntity = getCodingLoveResponseContent(response)
      persistCodingLoveResult(codingLoveEntity)
      Some(
        SlashResponse(
          codingLoveResponseType,
          s"${codingLoveEntity.name}\n${codingLoveEntity.gifUrl}\nSearched for ${x.text}",
          List()))
  }

  private def persistCodingLoveResult(codingLoveEntity: CodingLoveEntity): Future[Boolean] =
    codingLoveService.add(codingLoveEntity.name, codingLoveEntity.gifUrl)

  /**
		* Filter the content to fit our needs
		*
		* @param htmlContent The web result to retrieve the information
		* @return The response message as String
		*/
  private def getCodingLoveResponseContent(htmlContent: String): CodingLoveEntity = {
    val doc = browser.parseString(htmlContent)

    val text = doc >> element("div div h3")
    val gif = doc >> element("div div p img")

    CodingLoveEntity(UUID.randomUUID(), text.innerHtml, gif.attr("src"), None, None)
  }
}
