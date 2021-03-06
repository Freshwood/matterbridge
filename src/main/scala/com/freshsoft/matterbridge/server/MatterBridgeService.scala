package com.freshsoft.matterbridge.server

import java.util.UUID

import akka.http.scaladsl.model.FormData
import com.freshsoft.matterbridge.client.codinglove.CodingLoveIntegration
import com.freshsoft.matterbridge.client.ninegag.NineGagIntegration
import com.freshsoft.matterbridge.util.MatterBridgeConfig
import model.MatterBridgeEntities.{OutgoingResponse, SlashResponse}
import model.{BotEntity, OutgoingHookRequest, SlashCommandRequest}

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
    with MatterBridgeContext
    with BotActorService {

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
      case _ => Future { None }
    }

  override def outgoingHookIntegration(formData: FormData): Future[Option[OutgoingResponse]] =
    proofBot(formData) flatMap randomBotMessage

  private def proofBot(formData: FormData): Future[Option[BotEntity]] =
    botService.all map { bots =>
      OutgoingHookRequest(formData) flatMap { data =>
        bots find (b => data.text.contains(b.name))
      }
    }

  private def randomBotMessage(bot: Option[BotEntity]): Future[Option[OutgoingResponse]] = {
    val id = bot map (_.id) getOrElse UUID.randomUUID()
    botService.randomBotMessage(id) map { resource =>
      resource map { res =>
        OutgoingResponse(res.value)
      }
    }
  }

}
