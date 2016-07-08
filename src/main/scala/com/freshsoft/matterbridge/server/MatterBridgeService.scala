package com.freshsoft.matterbridge.server

import akka.http.scaladsl.model.FormData
import com.freshsoft.matterbridge.client.MatterBridgeIntegration
import com.freshsoft.matterbridge.client.codinglove.CodingLoveIntegration
import com.freshsoft.matterbridge.client.ninegag.NineGagIntegration
import com.freshsoft.matterbridge.entity.MattermostEntities.{OutgoingResponse, SlashResponse}
import com.freshsoft.matterbridge.entity.{OutgoingHookRequest, SlashCommandRequest}
import com.freshsoft.matterbridge.util.WithConfig

import scala.concurrent.Future

/**
	* The matter bridge service which has the integration logic
	*/
class MatterBridgeService extends WithConfig with WithActorContext {

	/**
		* The matterbridge slash command integrations
		*
		* @param formData The FormData field to retrieve the request params
		* @return Option SlashResponse
		*/
	def slashCommandIntegration(formData: FormData): Future[Option[SlashResponse]] = {
		val request = SlashCommandRequest(formData)
		slashIntegration(request)
	}

	/**
		* The matterbridge outgoing integrations
		*
		* @param formData The FormData field to retrieve the request params
		* @return Option SlashResponse
		*/
	def outgoingHookIntegration(formData: FormData): Future[Option[OutgoingResponse]] = {
		val request = OutgoingHookRequest(formData)
		outgoingIntegration(request)
	}

	private def slashIntegration(request: SlashCommandRequest) = request.command match {
		case x if x.contains(codingLoveCommand) => CodingLoveIntegration.getResult(request)
		case x if x.contains(nineGagCommand) => NineGagIntegration.getResult(request)
		case x if x.contains(matterBridgeCommand) => MatterBridgeIntegration.getResult(request)
		case _ => Future { None }
	}

	private def outgoingIntegration(request: OutgoingHookRequest) = Future {
		Some(OutgoingResponse("Hallo Welt"))
	}
}
