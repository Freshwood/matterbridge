package com.freshsoft.matterbridge.server

import akka.http.scaladsl.model.FormData
import com.freshsoft.matterbridge.client.MatterBridgeIntegration
import com.freshsoft.matterbridge.client.codinglove.CodingLoveIntegration
import com.freshsoft.matterbridge.client.newsriver.NewsriverIntegration
import com.freshsoft.matterbridge.client.ninegag.NineGagIntegration
import com.freshsoft.matterbridge.util.MatterBridgeConfig
import model.MatterBridgeEntities.{OutgoingResponse, SlashResponse}
import model.{OutgoingHookRequest, SlashCommandRequest}

import scala.concurrent.Future

trait MatterBridgeServiceIntegration {
  def slashCommandIntegration(formData: FormData): Future[Option[SlashResponse]]

  def outgoingHookIntegration(formData: FormData): Future[Option[OutgoingResponse]]
}

/**
	* The matter bridge service which has the integration logic
	*/
class MatterBridgeService
    extends MatterBridgeServiceIntegration
    with MatterBridgeConfig
    with MatterBridgeContext {

  /**
		* The matterbridge slash command integrations
		*
		* @param formData The FormData field to retrieve the request params
		* @return Option SlashResponse
		*/
  override def slashCommandIntegration(formData: FormData): Future[Option[SlashResponse]] = {
    val request = SlashCommandRequest(formData)
    slashIntegration(request)
  }

  private def slashIntegration(request: SlashCommandRequest) =
    request.command match {
      case x if x.contains(codingLoveCommand) =>
        CodingLoveIntegration.getResult(request)
      case x if x.contains(nineGagCommand) =>
        NineGagIntegration.getResult(request)
      case x if x.contains(matterBridgeCommand) =>
        MatterBridgeIntegration.getResult(request)
      case x if x.contains(newsriverCommand) =>
        NewsriverIntegration.getResult(request)
      case _ => Future { None }
    }

  override def outgoingHookIntegration(formData: FormData): Future[Option[OutgoingResponse]] =
    Future.successful {
      OutgoingHookRequest(formData) flatMap { data =>
        botMap.keys.toList collectFirst {
          case x if data.text contains x => OutgoingResponse(botMap(x))
        }
      }
    }
}
