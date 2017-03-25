package com.freshsoft.matterbridge.routing

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import com.freshsoft.matterbridge.service.database.RssConfigService
import model.{DatabaseEntityJsonSupport, RssUpload}

import scala.concurrent.ExecutionContext

/**
  * The nine gag specific static routes
  */
class RssConfigRoute(service: RssConfigService)(implicit executionContext: ExecutionContext)
    extends DatabaseEntityJsonSupport {

  val route: Route = logRequestResult("rss-config-route") {
    pathPrefix("rss") {
      pathEndOrSingleSlash {
        get {
          complete(service.all)
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
          post {
            entity(as[RssUpload]) { entity =>
              complete {
                service.add(entity.name, entity.rssUrl, entity.incomingToken) map (_.toString)
              }
            }
          }
        } ~
        path("exists" / Remaining) { p =>
          get {
            complete(service.exists(p) map (_.toString))
          }
        } ~
        path(Remaining) { p =>
          get {
            complete(service.byName(p))
          }
        } ~
        path(JavaUUID) { uuid =>
          get {
            complete(service.byId(uuid))
          }
        }
    }
  }
}
