package com.freshsoft.matterbridge.routing

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.freshsoft.matterbridge.server.IntegrationService
import data.matterbridge.NineGagDataProvider
import model.MatterBridgeEntities.ISlashCommandJsonSupport

import scala.concurrent.ExecutionContext

/**
  * The web content specific static routes
  */
class WebContentRoute(nineGagDb: NineGagDataProvider)(implicit executionContext: ExecutionContext)
    extends ISlashCommandJsonSupport {

  lazy val service = new IntegrationService()

  private val index: Route = get {
    getFromResource("content/index.html")
  }

  val route: Route = path("index.html") {
    index
  } ~ path("test") {
    get {
      complete(service.nineGag)
    }
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
