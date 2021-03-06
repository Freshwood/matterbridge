package com.freshsoft.matterbridge.client.ninegag

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.freshsoft.matterbridge.client.IMatterBridgeResult
import com.freshsoft.matterbridge.server.{MatterBridgeContext, NineGagActorService}
import com.freshsoft.matterbridge.util.MatterBridgeConfig
import model.MatterBridgeEntities.{NineGagGifResult, SlashResponse}
import model.SlashCommandRequest

import scala.concurrent.Future

/**
	* The nine gag integration which is searching in the background for gifs
	*/
object NineGagIntegration
    extends IMatterBridgeResult
    with MatterBridgeConfig
    with MatterBridgeContext
    with NineGagActorService {

  val nineGagResolver: ActorRef =
    system.actorOf(Props(classOf[NineGagResolver]), "NineGagResolver")

  /**
		* The gif receiving actor which is handling the receiving of NineGagGifResult's
		*/
  class NineGagGifReceiver extends Actor with ActorLogging {

    override def receive: Receive = {
      case x: NineGagGifResult =>
        categoryDb.all foreach { cats =>
          cats find (_.name == x.categoryName) map { result =>
            nineGagService.add(x.key, x.gifUrl, result.id) map {
              case true => log.info(s"Added 9 Gag gif '${x.key}' with url '${x.gifUrl}'")
              case _    => log.debug(s"Could not add 9 Gag gif with the name [${x.key}]")
            }
          }
        }
    }
  }

  override def getResult(request: SlashCommandRequest): Future[Option[SlashResponse]] =
    nineGagService.byName(request.text) map {
      _.headOption map { gif =>
        SlashResponse(nineGagResponseType,
                      s"${gif.name}\n${gif.gifUrl}\nSearched for ${request.text}",
                      List())
      }
    }

}
