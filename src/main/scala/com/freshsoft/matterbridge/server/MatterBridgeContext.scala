package com.freshsoft.matterbridge.server

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

/**
	* Rest trait defining the execution context for the actors
	*/
trait MatterBridgeContext {

  implicit val system = ActorSystem("matter-bridge")

  implicit val materializer = ActorMaterializer()

  implicit val executionContext: ExecutionContext = materializer.executionContext
}
