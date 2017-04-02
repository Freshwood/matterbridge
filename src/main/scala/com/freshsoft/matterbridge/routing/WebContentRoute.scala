package com.freshsoft.matterbridge.routing

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import com.freshsoft.matterbridge.routing.TestActor.{Connected, OutgoingMessage, Tick}
import com.freshsoft.matterbridge.service.database.WebService
import model.DatabaseEntityJsonSupport
import org.reactivestreams.Publisher

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

  private val socketConnection: Flow[Message, Message, Any] = Flow[Message] collect {
    case tm: TextMessage => TextMessage(Source.single("Hello") ++ tm.textStream)
  }

  private def connection: (ActorRef, Publisher[Message]) =
    Source
      .actorRef(16, OverflowStrategy.dropTail)
      .toMat(Sink.asPublisher[Message](fanout = false))(Keep.both)
      .run()

  // source is what comes in: browser ws events -> play -> publisher -> userActor
  // sink is what comes out:  userActor -> websocketOut -> play -> browser ws events
  /*def flow(): Flow[Message, Message, Any] = {
    val (webSocketOut: ActorRef, webSocketIn: Publisher[Message]) = connection

    val actor: ActorRef =
      system.actorOf(Props(new TestActor(webSocketOut, webService)), "WebSocketActor")

    val sink = Sink.actorRef(actor, akka.actor.Status.Success(()))

    val source = Source.fromPublisher(webSocketIn)

    Flow.fromSinkAndSource(sink, source).watchTermination() { (_, future) =>
      future.foreach { _ =>
        system.stop(actor)
      }
      NotUsed
    }
  }*/

  def flow: Flow[Message, Message, NotUsed] = {

    val actor: ActorRef = system.actorOf(Props(new TestActor(webService)))

    val incomingMessages: Sink[Message, NotUsed] = Flow[Message] map {
      case TextMessage.Strict(msg) => TestActor.Tick(msg)
    } to Sink.actorRef(actor, PoisonPill)

    val outGoingMessages: Source[Message, NotUsed] = Source
      .actorRef[OutgoingMessage](16, OverflowStrategy.dropTail) mapMaterializedValue { outActor =>
      actor ! Connected(outActor)
      NotUsed
    } map { someMessage =>
      TextMessage(someMessage.msg)
    }

    Flow.fromSinkAndSource(incomingMessages, outGoingMessages)
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

class TestActor(service: WebService)(implicit executionContext: ExecutionContext) extends Actor {

  import scala.concurrent.duration._

  override def receive: Receive = {
    case Connected(out) => context.become(connected(out))
  }

  def connected(outgoing: ActorRef): Receive = {
    case Tick(_) =>
      context.system.scheduler.scheduleOnce(5 second, self, Tick("Start"))
      service.overallCount.map(result => outgoing ! OutgoingMessage(result.toString))
    case "Start" => self ! Tick("Hallo")
  }
}

object TestActor {
  case class Tick(msg: String)
  case class Connected(outActor: ActorRef)
  case class OutgoingMessage(msg: String)
}
