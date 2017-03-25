package com.freshsoft.matterbridge.routing

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import com.freshsoft.matterbridge.service.database.BotService
import model.{BotOrCategoryUpload, DatabaseEntityJsonSupport}

import scala.concurrent.ExecutionContext

/**
  * The bot specific service routes
  */
class BotRoute(service: BotService)(implicit executionContext: ExecutionContext)
    extends DatabaseEntityJsonSupport {

  val route: Route = logRequestResult("bot-route") {
    pathPrefix("bot") {
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
            entity(as[BotOrCategoryUpload]) { entity =>
              complete {
                service.add(entity.name) map (_.toString)
              }
            }
          }
        } ~
        path("allResources") {
          get {
            complete {
              service.all flatMap { bots =>
                service.allResources(bots.head.id)
              }
            }
          }
        } ~
        path("exists" / Remaining) { search =>
          get {
            complete(service.exists(search) map (_.toString))
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
