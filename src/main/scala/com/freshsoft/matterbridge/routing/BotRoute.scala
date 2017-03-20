package com.freshsoft.matterbridge.routing

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import com.freshsoft.matterbridge.service.database.BotService
import model.DatabaseEntityJsonSupport

import scala.concurrent.ExecutionContext

/**
  * The nine gag specific static routes
  */
class BotRoute(service: BotService)(implicit executionContext: ExecutionContext)
    extends DatabaseEntityJsonSupport {

  val route: Route = logRequestResult("bot-route") {
    pathPrefix("bot") {
      pathEndOrSingleSlash {
        get {
          complete(service.byName("bot"))
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
              service.add("Bot neu") map (_.toString)
            }
          }
        } ~
        path("all") {
          get {
            complete(service.all)
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
        }
    }
  }
}
