package com.freshsoft.matterbridge.routing

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

/**
  * The web content specific static routes
  */
class WebContentRoute(implicit executionContext: ExecutionContext) {

  private val index: Route = get {
    getFromResource("content/index.html")
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
    } ~ pathSingleSlash {
    index
  }
}
