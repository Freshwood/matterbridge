package com.freshsoft.matterbridge.util

import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.coding.{Deflate, Gzip, NoCoding}
import akka.http.scaladsl.model.HttpEntity.Chunked
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.HttpEncodings
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import com.freshsoft.matterbridge.server.MatterBridgeContext
import model.MatterBridgeEntities.{ISlashCommandJsonSupport, IncomingResponse}
import net.softler.client.ClientRequest
import spray.json._

import scala.concurrent.Future

/**
  * A simple http client to send request external services
  */
object MatterBridgeHttpClient extends ISlashCommandJsonSupport with MatterBridgeContext {

  val log: LoggingAdapter = Logging.getLogger(system, this)

  /**
    * Try to post the incoming token response object to the given url
    *
    * @param url              The requested url
    * @param incomingResponse The post data content
    * @return Nothing => only logs the result
    */
  def postToIncomingWebhook(url: String, incomingResponse: IncomingResponse): Future[Unit] =
    ClientRequest(url)
      .entity(HttpEntity(incomingResponse.toJson.toString))
      .asJson
      .post()
      .map(_.response)
      .map {
        case HttpResponse(StatusCodes.OK, _, _, _) =>
          log.info(s"Successfully send data to $url with data ${incomingResponse.toJson.toString}")
        case _ =>
          log.warning(
            s"Could not send data to token url $url with data ${incomingResponse.toJson.toString}")
      }

  /**
    * Get a raw http result from the provided url
    *
    * @param url The url to retrieve the result
    * @return Raw HttpResponse UTF-8 conform String as a future
    */
  def getUrlContent(url: String): Future[String] =
    ClientRequest(url).get().map(_.response).map(decodeResponse) flatMap {

      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        entity.dataBytes.runFold(ByteString(""))(_ ++ _).map { x =>
          x.decodeString("UTF-8")
        }

      // We have to consume the response, cause we need a valid akka.http flow
      // https://stackoverflow.com/questions/40447805/how-to-handle-akka-http-client-response-failure
      case HttpResponse(_, _, entity, _) =>
        entity.dataBytes.runFold(ByteString(""))(_ ++ _).map { x =>
          x.decodeString("UTF-8")
        }
    }

  def decodeResponse(response: HttpResponse): HttpResponse = response.encoding match {
    case HttpEncodings.gzip    => Gzip.decodeMessage(response)
    case HttpEncodings.deflate => Deflate.decodeMessage(response)
    case _                     => NoCoding.decodeMessage(response)
  }
}
