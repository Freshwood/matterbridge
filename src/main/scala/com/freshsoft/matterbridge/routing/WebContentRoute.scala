package com.freshsoft.matterbridge.routing

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import com.freshsoft.matterbridge.service.database.WebService
import com.freshsoft.matterbridge.socket.UserActor
import com.freshsoft.matterbridge.socket.UserActor.{Connected, OutgoingMessage}
import model.DatabaseEntityJsonSupport

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

/**
  * The web content specific static routes
  */
class WebContentRoute(webService: WebService)(implicit executionContext: ExecutionContext,
                                              system: ActorSystem,
                                              materializer: Materializer)
    extends DatabaseEntityJsonSupport {

  private val index: Route = get {
    getFromResource("content/index.html")
  }

  private def flow: Flow[Message, Message, NotUsed] = {

    val actor: ActorRef = system.actorOf(Props(new UserActor(webService)))

    val incomingMessages: Sink[Message, NotUsed] = Flow[Message] map {
      // transform websocket message to domain message
      case TextMessage.Strict(msg) => UserActor.Tick(msg)
      case bm: BinaryMessage =>
        // ignore binary messages but drain content to avoid the stream being clogged
        bm.dataStream.runWith(Sink.ignore)
        Nil
      case _ => NotUsed
    } to Sink.actorRef(actor, PoisonPill)

    val outgoingMessages: Source[Message, NotUsed] = Source
      .actorRef[OutgoingMessage](16, OverflowStrategy.dropTail) mapMaterializedValue { outActor =>
      // give the User actor a way to send messages out
      actor ! Connected(outActor)
      NotUsed
    } map { someMessage =>
      // transform domain message to web socket message
      TextMessage(someMessage.data.toString)
    }

    Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
  }

  val route: Route = path("index.html") {
    index
  } ~
    pathPrefix("js") {
      get {
        getFromResourceDirectory("content/js")
      }
    } ~
    pathPrefix("css") {
      get {
        getFromResourceDirectory("content/css")
      }
    } ~
    pathPrefix("images") {
      get {
        getFromResourceDirectory("content/images")
      }
    } ~
    pathPrefix("fonts") {
      get {
        getFromResourceDirectory("content/fonts")
      }
    } ~
    pathPrefix("socket") {
      get {
        handleWebSocketMessages(flow)
      }
    } ~
    pathPrefix("service") {
      pathPrefix("count") {
        get {
          complete(webService.overallCount)
        }
      }
    } ~
    pathSingleSlash {
      index
    }
}
