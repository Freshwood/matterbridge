package com.freshsoft.matterbridge.server

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

/**
	* Rest trait defining the execution context for the actors
	*/
trait IRest {
	implicit val system = ActorSystem("matter-bridge")

	implicit def executionContext: ExecutionContext

	implicit val materializer = ActorMaterializer()
}
