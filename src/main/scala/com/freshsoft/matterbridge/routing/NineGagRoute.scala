package com.freshsoft.matterbridge.routing

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.freshsoft.matterbridge.service.database.NineGagService
import model.DatabaseEntityJsonSupport

import scala.concurrent.ExecutionContext

/**
  * The web content specific static routes
  */
class NineGagRoute(service: NineGagService)(implicit executionContext: ExecutionContext)
    extends DatabaseEntityJsonSupport {

  val route: Route = logRequestResult("ninegag-service") {
    path("9gag") {
      get {
        parameter('search.as[String]) { search =>
          complete(search)
        }
      }
    }
  }
}
