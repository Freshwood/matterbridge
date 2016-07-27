package com.freshsoft.matterbridge.server

import akka.http.scaladsl.model.FormData
import com.freshsoft.matterbridge.client.MatterBridgeIntegration
import com.freshsoft.matterbridge.client.codinglove.CodingLoveIntegration
import com.freshsoft.matterbridge.client.newsriver.NewsriverIntegration
import com.freshsoft.matterbridge.client.ninegag.NineGagIntegration
import com.freshsoft.matterbridge.entity.MatterBridgeEntities.SlashResponse
import com.freshsoft.matterbridge.entity.SlashCommandRequest
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

	private def slashIntegration(request: SlashCommandRequest) = request.command match {
		case x if x.contains(codingLoveCommand) => CodingLoveIntegration.getResult(request)
		case x if x.contains(nineGagCommand) => NineGagIntegration.getResult(request)
		case x if x.contains(matterBridgeCommand) => MatterBridgeIntegration.getResult(request)
		case x if x.contains(newsriverCommand) => NewsriverIntegration.getResult(request)
		case _ => Future { None }
	}
}
