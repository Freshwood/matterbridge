package com.freshsoft.matterbridge.client.ninegag

import akka.actor.{Actor, ActorRef, Props}
import akka.event.{Logging, LoggingAdapter}
import com.freshsoft.matterbridge.client.IMatterBridgeResult
import model.MatterBridgeEntities.{NineGagGifResult, SlashResponse}
import com.freshsoft.matterbridge.server.MatterBridgeContext
import com.freshsoft.matterbridge.util.MatterBridgeConfig
import model.SlashCommandRequest

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.Random

/**
	* The nine gag integration which is searching in the background for gifs
  * TODO: Refactor this whole integration -> This is not an efficient actor usage
	*/
object NineGagIntegration
    extends IMatterBridgeResult
    with MatterBridgeConfig
    with MatterBridgeContext {

  val log: LoggingAdapter = Logging.getLogger(system, this)

  val nineGagResolver: ActorRef = system.actorOf(Props(classOf[NineGagResolver]))

  val nineGagWorker: ActorRef = system.actorOf(Props(classOf[NineGagWorker]))

  val nineGagGifReceiver: ActorRef = system.actorOf(Props(classOf[NineGagGifReceiver]))

  var nineGagGifs: mutable.LinkedHashMap[String, String] =
    mutable.LinkedHashMap.empty

  var lastGif: NineGagGifResult = new NineGagGifResult

  /**
		* The gif receiving actor which is handling the receiving of NineGagGifResult's
		*/
  class NineGagGifReceiver extends Actor {

    override def receive: Receive = {
      case x: NineGagGifResult =>
        addGif(x)
        adjustGifStore()
        log.info(s"Actual size [${nineGagGifs.size}]")
    }
  }

  /**
		* Adjust the gif store. Almost 100000 gifs should be enough to cache
		*/
  private def adjustGifStore() = {
    val dropSize = nineGagGifs.size - nineGagMaximumGifStore
    if (dropSize > 0) {
      log.info(s"Dropping $dropSize gifs, cause the gif store limit is reached")
      nineGagGifs = nineGagGifs.drop(dropSize)
    }
  }

  /**
		* Add a new NineGagGifResult for the matterbridge api
		* Also check if it is a new gif and set is at last added gif
		* @param gif A NineGagGifResult
		*/
  private def addGif(gif: NineGagGifResult) = {
    val oldSize = nineGagGifs.size
    nineGagGifs += (gif.key -> gif.gifUrl)
    if (oldSize != nineGagGifs.size) {
      log.info(s"Added new gif [${gif.key}]\n[${gif.gifUrl}]")
      lastGif = gif
    }
  }

  override def getResult(request: SlashCommandRequest): Future[Option[SlashResponse]] = {

    val responseText = (x: (String, String), y: String) => s"${x._1} \n ${x._2}\nSearched for $y"

    val getSpecificMap = (x: Vector[(String, String)]) => x(Random.nextInt(x.size))

    // Search for every word to as fallback
    val words = request.text.split("\\W+")

    Future {
      nineGagGifs.filter(p =>
        p._1.contains(request.text) || p._1.contains(request.text) || p._1.contains(words)) match {
        case x: mutable.LinkedHashMap[String, String] if x.nonEmpty =>
          Some(
            SlashResponse(nineGagResponseType,
                          responseText(getSpecificMap(x.toVector), request.text),
                          List()))
        // Nothing found then search further
        case _ =>
          getSpecificMap(nineGagGifs.toVector) match {
            case y: (String, String) =>
              Some(SlashResponse(nineGagResponseType, responseText(y, request.text), List()))
            case _ => None
          }
      }
    }
  }
}
