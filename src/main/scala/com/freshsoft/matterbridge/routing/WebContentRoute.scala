package com.freshsoft.matterbridge.routing

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Source}
import com.freshsoft.matterbridge.service.database.WebService
import model.DatabaseEntityJsonSupport

import scala.concurrent.ExecutionContext

/**
  * The web content specific static routes
  */
class WebContentRoute(webService: WebService)(implicit executionContext: ExecutionContext)
    extends DatabaseEntityJsonSupport {

  private val index: Route = get {
    getFromResource("content/index.html")
  }

  private val socketConnection: Flow[Message, Message, Any] = Flow[Message] collect {
    case tm: TextMessage => TextMessage(Source.single("Hello") ++ tm.textStream)
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
        handleWebSocketMessages(socketConnection)
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
