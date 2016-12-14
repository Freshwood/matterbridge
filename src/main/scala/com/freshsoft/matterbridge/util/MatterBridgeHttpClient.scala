package com.freshsoft.matterbridge.util

import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import com.freshsoft.matterbridge.client.ninegag.NineGagIntegration.{
  newsriverIncomingTokenUrl => _
}
import com.freshsoft.matterbridge.entity.MatterBridgeEntities.{
  ISlashCommandJsonSupport,
  IncomingResponse
}
import com.freshsoft.matterbridge.server.MatterBridgeContext
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
    Http().singleRequest(
      HttpRequest(uri = url,
                  method = HttpMethods.POST,
                  headers = Nil,
                  entity = HttpEntity(incomingResponse.toJson.toString)
                    .withContentType(MediaTypes.`application/json`))) map {
      case HttpResponse(StatusCodes.OK, _, _, _) =>
        log.info(s"Successfully send data to $url with data ${incomingResponse.toJson.toString}")

      // Something went wrong while sending information to incoming token url
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
    Http().singleRequest(HttpRequest(uri = url)) flatMap {

      case HttpResponse(StatusCodes.OK, headers, entity, _) =>
        val isGzipContent =
          (x: HttpHeader) => x.name() == "Content-Encoding" && x.value() == "gzip"

        headers.find(isGzipContent) match {
          case Some(_) =>
            entity.dataBytes
              .via(Gzip.decoderFlow)
              .map(_.decodeString("UTF-8"))
              .runWith(Sink.fold("")(_ ++ _))

          case None =>
            entity.dataBytes.runFold(ByteString(""))(_ ++ _).map { x =>
              x.decodeString("UTF-8")
            }
        }

      case _ =>
        Future.successful {
          ""
        }
    }
}
