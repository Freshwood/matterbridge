package com.freshsoft.matterbridge.socket

import akka.actor.{Actor, ActorRef}
import com.freshsoft.matterbridge.service.database.WebService
import com.freshsoft.matterbridge.socket.UserActor.{Connected, OutgoingMessage, Tick}
import model.DatabaseEntityJsonSupport
import spray.json.JsValue
import spray.json._

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

/**
  * Created by Freshwood on 02.04.2017.
  */
class UserActor(service: WebService)(implicit executionContext: ExecutionContext)
    extends Actor
    with DatabaseEntityJsonSupport {

  import scala.concurrent.duration._

  override def receive: Receive = {
    case Connected(out) => context.become(connected(out))
  }

  def connected(outgoing: ActorRef): Receive = {
    case Tick(_) =>
      context.system.scheduler.scheduleOnce(5 second, self, Tick("Start"))
      service.overallCount.map(result => outgoing ! OutgoingMessage(result.toJson))
  }
}

object UserActor {
  case class Tick(msg: String)
  case class Connected(outActor: ActorRef)
  case class OutgoingMessage(data: JsValue)
}
