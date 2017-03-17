package com.freshsoft.matterbridge.client.ninegag

import akka.actor.{Actor, ActorRef, Props}
import akka.event.{Logging, LoggingAdapter}
import com.freshsoft.matterbridge.client.IMatterBridgeResult
import com.freshsoft.matterbridge.server.{MatterBridgeContext, NineGagActorService}
import com.freshsoft.matterbridge.util.MatterBridgeConfig
import model.MatterBridgeEntities.{NineGagGifResult, SlashResponse}
import model.SlashCommandRequest

import scala.concurrent.Future

/**
	* The nine gag integration which is searching in the background for gifs
  * TODO: Refactor this whole integration -> This is not an efficient actor usage
	*/
object NineGagIntegration
    extends IMatterBridgeResult
    with MatterBridgeConfig
    with MatterBridgeContext
    with NineGagActorService {

  val log: LoggingAdapter = Logging.getLogger(system, this)
  val nineGagResolver: ActorRef =
    system.actorOf(Props(classOf[NineGagResolver]), "NineGagResolver")

  /**
		* The gif receiving actor which is handling the receiving of NineGagGifResult's
		*/
  class NineGagGifReceiver extends Actor {

    override def receive: Receive = {
      case x: NineGagGifResult =>
        nineGagService.add(x.key, x.gifUrl) map {
          case true => log.info(s"Added 9 Gag gif '${x.key}' with url '${x.gifUrl}'")
        }
    }
  }

  override def getResult(request: SlashCommandRequest): Future[Option[SlashResponse]] = {

    nineGagService.byName(request.text) map { gifs =>
      gifs.headOption map { gif =>
        SlashResponse(nineGagResponseType,
                      s"${gif.name}\n${gif.gifUrl}\nSearched for ${request.text}",
                      List())
      }
    }
  }
}
