package com.freshsoft.matterbridge.routing

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.freshsoft.matterbridge.service.database.NineGagService
import model.DatabaseEntityJsonSupport

import scala.concurrent.ExecutionContext

/**
  * The nine gag specific static routes
  */
class NineGagRoute(service: NineGagService)(implicit executionContext: ExecutionContext)
    extends DatabaseEntityJsonSupport {

  val route: Route = logRequestResult("ninegag-service") {
    path("9gag") {
      get {
        parameter('search.as[String]) { search =>
          complete(service.byName(search))
        }
      }
    }
  }
}
