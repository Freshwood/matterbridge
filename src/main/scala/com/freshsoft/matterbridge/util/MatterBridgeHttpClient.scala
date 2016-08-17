package com.freshsoft.matterbridge.util

import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import com.freshsoft.matterbridge.client.ninegag.NineGagIntegration.{newsriverIncomingTokenUrl => _}
import com.freshsoft.matterbridge.entity.MatterBridgeEntities.{ISlashCommandJsonSupport, IncomingResponse}
import com.freshsoft.matterbridge.server.WithActorContext
import spray.json._

/**
	* A simple http client to send request external services
	*/
object MatterBridgeHttpClient extends ISlashCommandJsonSupport with WithActorContext {

	val log = Logging.getLogger(system, this)

	def postToIncomingWebhook(url: String, incomingResponse: IncomingResponse) =
		Http().singleRequest(HttpRequest(uri = url, method = HttpMethods.POST, headers = Nil,
			entity = HttpEntity(incomingResponse.toJson.toString).withContentType(MediaTypes.`application/json`))) map {
			case HttpResponse(StatusCodes.OK, _, _, _) => log.info(s"Successfully send data to $url with data ${incomingResponse.toJson.toString}")

			// Something went wrong while sending information to incoming token url
			case _ => log.warning(s"Could not send data to token url $url with data ${incomingResponse.toJson.toString}")
		}
}
