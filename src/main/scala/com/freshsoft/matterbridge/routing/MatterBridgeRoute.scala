package com.freshsoft.matterbridge.routing

import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.freshsoft.matterbridge.entity.MattermostEntities.ISlashCommandJsonSupport
import com.freshsoft.matterbridge.server.MatterBridgeService

/**
	* The matter bridge application routing definition
	*/
class MatterBridgeRoute extends ISlashCommandJsonSupport {

	val matterBridgeService = new MatterBridgeService

	val routes: Route = logRequestResult("matter-bridge") {
		pathPrefix("api") {
			pathPrefix("matterbridge") {
				pathEnd {
					post {
						entity(as[FormData]) { entity =>
							complete(matterBridgeService.slashCommandIntegration(entity))
						}
					} ~ get {
						complete("The matterbridge service is online!")
					}
				}
			}
		}
	}
}
