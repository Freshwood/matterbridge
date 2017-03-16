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

  val route: Route = logRequestResult("ninegag-route") {
    path("9gag" / Remaining) { p =>
      get {
        complete(service.byName(p))
      }
    } ~
      path("count") {
        get {
          complete {
            service.count map (_.toString)
          }
        }
      } ~
      path("add") {
        get {
          complete {
            service.add("Test", "TestUrl") map (_.toString)
          }
        }
      } ~
      path("exists" / Remaining) { p =>
        get {
          complete(
            service
              .exists("https://img-9gag-fun.9cache.com/photo/aOzQ4RD_460sa.gif") map (_.toString))
        }
      }
  }
}
