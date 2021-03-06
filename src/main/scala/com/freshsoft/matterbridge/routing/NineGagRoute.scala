package com.freshsoft.matterbridge.routing

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.freshsoft.matterbridge.service.database.NineGagService
import model.{DatabaseEntityJsonSupport, NineGagUpload}

import scala.concurrent.ExecutionContext

/**
  * The nine gag specific service routes
  */
class NineGagRoute(service: NineGagService)(implicit executionContext: ExecutionContext)
    extends DatabaseEntityJsonSupport {

  val route: Route = pathPrefix("9gag") {
    path("count") {
      get {
        complete {
          service.count map (_.toString)
        }
      }
    } ~
      path("add") {
        post {
          entity(as[NineGagUpload]) { entity =>
            complete {
              service.add(entity.name, entity.gifUrl, entity.categoryId) map (_.toString)
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
        } ~
          delete {
            complete(service.delete(uuid) map (_.toString))
          }
      } ~
      path("last") {
        get {
          complete(service.last)
        }
      } ~
      path(Remaining) { name =>
        get {
          complete(service.byName(name))
        }
      }
  }
}
