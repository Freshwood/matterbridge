package com.freshsoft.matterbridge.routing

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import com.freshsoft.matterbridge.service.database.BotService
import model.{BotOrCategoryUpload, BotResourceUpload, DatabaseEntityJsonSupport}

import scala.concurrent.ExecutionContext

/**
  * The bot specific service routes
  */
class BotRoute(service: BotService)(implicit executionContext: ExecutionContext)
    extends DatabaseEntityJsonSupport {

  val route: Route = pathPrefix("bot") {
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
      path("deleted") {
        get {
          complete {
            service.allDeleted
          }
        }
      } ~
      path("add") {
        post {
          entity(as[BotResourceUpload]) { entity =>
            complete {
              service.addResource(entity.botId, entity.name) map (_.toString)
            }
          } ~
            entity(as[BotOrCategoryUpload]) { entity =>
              complete {
                service.add(entity.name) map (_.toString)
              }
            }
        }
      } ~
      path("resources" / JavaUUID) { id =>
        get {
          complete(service.allResources(id))
        } ~
          delete {
            complete(service.deleteResource(id) map (_.toString))
          }
      } ~
      path("restore" / JavaUUID) { id =>
        get {
          complete(service.restore(id) map (_.toString))
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
        } ~
          delete {
            complete(service.delete(uuid) map (_.toString))
          }
      }
  }

}
