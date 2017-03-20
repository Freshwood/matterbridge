package com.freshsoft.matterbridge.routing

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import com.freshsoft.matterbridge.service.database.RssConfigService
import model.DatabaseEntityJsonSupport

import scala.concurrent.ExecutionContext

/**
  * The nine gag specific static routes
  */
class RssConfigRoute(service: RssConfigService)(implicit executionContext: ExecutionContext)
    extends DatabaseEntityJsonSupport {

  val route: Route = logRequestResult("rss-config-route") {
    path("rss" / Remaining) { p =>
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
            service.add("Test", "TestUrl", "token") map (_.toString)
          }
        }
      } ~
      path("exists" / Remaining) { p =>
        get {
          complete(
            service
              .exists("https://img-9gag-fun.9cache.com/photo/aOzQ4RD_460sa.gif") map (_.toString))
        }
      } ~
      path("all") {
        get {
          complete(service.all)
        }
      }
  }
}
