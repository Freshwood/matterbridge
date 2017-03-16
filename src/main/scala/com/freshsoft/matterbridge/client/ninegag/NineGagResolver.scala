package com.freshsoft.matterbridge.client.ninegag

import akka.actor.{Actor, ActorRef, Props}
import com.freshsoft.matterbridge.client.ninegag.NineGagIntegration.NineGagGifReceiver
import model.MatterBridgeEntities.NineGagResolveCommand

/**
	* The resolver handles the worker and communicate with it
	*/
class NineGagResolver extends Actor {

  private val nineGagBaseUrl = "http://9gag.com/"

  private var actualUrl = nineGagBaseUrl

  private val nineGagCategories = Seq("funny/",
                                      "wtf/",
                                      "gif/",
                                      "gaming/",
                                      "anime-manga/",
                                      "movie-tv/",
                                      "cute/",
                                      "girl/",
                                      "awesome/",
                                      "cosplay/",
                                      "sport/",
                                      "food/",
                                      "timely/")

  private val nineGagExtraCategory = "fresh/"

  private val nineGagUrls = Seq(nineGagBaseUrl) ++ nineGagCategories
    .flatMap(e => Seq(nineGagBaseUrl + e) ++ Seq(nineGagBaseUrl + e + nineGagExtraCategory))
    .toList

  private val receiver: ActorRef =
    context.watch(context.actorOf(Props(classOf[NineGagGifReceiver]), "NineGagReceiver"))

  private val worker: ActorRef =
    context.watch(context.actorOf(Props(classOf[NineGagWorker], receiver), "NineGagWorker"))

  override def receive: Receive = {
    case NineGagResolveCommand("Resolve") =>
      actualUrl = getNextNineGagUrl(actualUrl)
      worker ! actualUrl
  }

  /**
		* Get the next url in the list
		*
		* @param previousUrl The previous url to cut
		* @return The next url as String
		*/
  private def getNextNineGagUrl(previousUrl: String): String = {
    val splitList = nineGagUrls.splitAt(nineGagUrls.indexOf(previousUrl) + 1)

    splitList._2.headOption match {
      case Some(x) => x
      case None => nineGagUrls.head
    }
  }
}
