package com.freshsoft.matterbridge.client.ninegag

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern._
import com.freshsoft.matterbridge.client.ninegag.NineGagIntegration.NineGagGifReceiver
import com.freshsoft.matterbridge.util.{MatterBridgeConfig, MatterBridgeHttpClient}
import model.MatterBridgeEntities._
import spray.json._

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

/**
	* The resolver handles the worker and communicate with it
	*/
class NineGagResolver extends Actor with ActorLogging with MatterBridgeConfig with JsonSupport {

  private val receiver: ActorRef =
    context.watch(context.actorOf(Props(classOf[NineGagGifReceiver]), "NineGagReceiver"))

  implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case NineGagResolveCommand("Resolve") =>
      MatterBridgeHttpClient.getUrlContent(nineGagApiUrl) map (_.parseJson
        .convertTo[NineGagApiResult]) pipeTo self
      ()

    case result: NineGagApiResult =>
      result.data.items foreach (item => receiver ! NineGagGifResult(item.title, item.imageURL))
  }
}
