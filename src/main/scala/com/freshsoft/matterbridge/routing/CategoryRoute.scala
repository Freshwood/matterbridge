package com.freshsoft.matterbridge.routing

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import com.freshsoft.matterbridge.service.database.CategoryService
import model.DatabaseEntityJsonSupport

import scala.concurrent.ExecutionContext

/**
  * The nine gag specific static routes
  */
class CategoryRoute(service: CategoryService)(implicit executionContext: ExecutionContext)
    extends DatabaseEntityJsonSupport {

  val route: Route = logRequestResult("category-route") {
    pathPrefix("category") {
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
          get {
            complete {
              service.add("Bot neu") map (_.toString)
            }
          }
        } ~
        path("exists" / Remaining) { search =>
          get {
            complete(service.exists(search) map (_.toString))
          }
        }
    }
  }
}
