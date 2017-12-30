package com.freshsoft.matterbridge.routing

import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import model.MatterBridgeEntities.JsonSupport
import com.freshsoft.matterbridge.server.{MatterBridgeService, MatterBridgeServiceIntegration}

/**
  * The matter bridge application routing definition
  */
class MatterBridgeRoute extends JsonSupport {

  val matterBridgeService: MatterBridgeServiceIntegration =
    new MatterBridgeService

  val routes: Route = pathPrefix("api") {
    pathPrefix("matterbridge") {
      pathEnd {
        post {
          entity(as[FormData]) { entity =>
            complete(matterBridgeService.slashCommandIntegration(entity))
          }
        } ~ get {
          complete("The matterbridge service is online!")
        }
      } ~ pathPrefix("out") {
        pathEnd {
          post {
            entity(as[FormData]) { entity =>
              complete(matterBridgeService.outgoingHookIntegration(entity))
            }
          }
        }
      }
    }
  }
}
