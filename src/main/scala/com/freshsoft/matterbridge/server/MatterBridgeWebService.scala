package com.freshsoft.matterbridge.server

import akka.http.scaladsl.server.Route
import com.freshsoft.matterbridge.routing.WebContentRoute

import scala.concurrent.ExecutionContext

/**
  * The service definition for the matter bridge web service
  */
trait MatterBridgeWebService {
  implicit def executionContext: ExecutionContext

  val routes: Route = new WebContentRoute().route
}
