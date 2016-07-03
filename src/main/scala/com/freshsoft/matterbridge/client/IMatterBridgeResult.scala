package com.freshsoft.matterbridge.client

import com.freshsoft.matterbridge.entity.MattermostEntities.{SlashRequest, SlashResponse}

import scala.concurrent.Future

/**
	* The global matter bridge result for the client integrations
	*/
trait IMatterBridgeResult {
		def getResult(request: SlashRequest): Future[Option[SlashResponse]]
}
