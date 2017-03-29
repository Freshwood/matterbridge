package com.freshsoft.matterbridge.routing

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import com.freshsoft.matterbridge.service.database.CodingLoveService
import model.{CodingLoveUpload, DatabaseEntityJsonSupport}

import scala.concurrent.ExecutionContext

/**
  * The bot specific service routes
  */
class CodingLoveRoute(service: CodingLoveService)(implicit executionContext: ExecutionContext)
    extends DatabaseEntityJsonSupport {

  val route: Route = logRequestResult("coding-love-route") {
    pathPrefix("codingLove") {
      path("count") {
        get {
          complete {
            service.count map (_.toString)
          }
        }
      } ~
        path("add") {
          post {
            entity(as[CodingLoveUpload]) { entity =>
              complete {
                service.add(entity.name, entity.gifUrl) map (_.toString)
              }
            }
          }
        } ~
        path("exists" / Remaining) { p =>
          get {
            complete(service.exists(p) map (_.toString))
          }
        } ~
        path(JavaUUID) { uuid =>
          get {
            complete(service.byId(uuid))
          }
        } ~
        path(Remaining) { name =>
          get {
            complete(service.byName(name))
          }
        }
    }
  }
}
