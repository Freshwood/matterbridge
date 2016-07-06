package com.freshsoft.matterbridge.server

import akka.http.scaladsl.model.FormData
import com.freshsoft.matterbridge.client.MatterBridgeClient.CodingLoveIntegration
import com.freshsoft.matterbridge.client.MatterBridgeIntegration
import com.freshsoft.matterbridge.client.ninegag.NineGagIntegration
import com.freshsoft.matterbridge.entity.MattermostEntities.SlashResponse
import com.freshsoft.matterbridge.entity.SlashRequest
import com.freshsoft.matterbridge.util.WithConfig

import scala.concurrent.{ExecutionContext, Future}

/**
	* The matter bridge service which has the integration logic
	*/
class MatterBridgeService extends WithConfig with IRest {

	implicit def executionContext: ExecutionContext = system.dispatcher

	val codingLoveIntegration = new CodingLoveIntegration

	/**
		* The matterbridge integrations -> more are coming soon
		* @param formData The FormData field to retrieve the request params
		* @return Option SlashResponse
		*/
	def matterBridgeIntegration(formData: FormData): Future[Option[SlashResponse]] = {

		val request = SlashRequest(formData)

		startIntegration(request)
	}

	private def startIntegration(request: SlashRequest) = request.command match {
		case x if x.contains(codingLoveCommand) => codingLoveIntegration.getResult(request)
		case x if x.contains(nineGagCommand) => NineGagIntegration.getResult(request)
		case x if x.contains(matterBridgeCommand) => MatterBridgeIntegration.getResult(request)
		case _ => Future { None }
	}

}
