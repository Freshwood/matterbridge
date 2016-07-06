package com.freshsoft.matterbridge.client

import com.freshsoft.matterbridge.client.ninegag.NineGagIntegration
import com.freshsoft.matterbridge.entity.MattermostEntities.SlashResponse
import com.freshsoft.matterbridge.entity.SlashRequest
import com.freshsoft.matterbridge.server.IRest
import com.freshsoft.matterbridge.util.WithConfig

import scala.concurrent.{ExecutionContext, Future}

/**
	* The global matter bridge integration
	*/
object MatterBridgeIntegration extends IMatterBridgeResult with WithConfig with IRest {

	override implicit def executionContext: ExecutionContext = materializer.executionContext

	val getLastEntryFromMap = (x: Map[String, String]) => s"9Gag Gifs [${x.size}]\nLast gif ${x.last._2}"

	/**
		* Get the SlashResponse result for this integration
		*
		* @param request The SlashRequest to build the response
		* @return A Future of SlashResponse
		*/
	override def getResult(request: SlashRequest): Future[Option[SlashResponse]] = {
		Future {
			Some(new SlashResponse(getLastEntryFromMap(NineGagIntegration.nineGagGifs), matterBridgeResponseType))
		}
	}
}
