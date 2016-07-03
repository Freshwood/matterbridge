package com.freshsoft.matterbridge.client

import com.freshsoft.matterbridge.entity.MattermostEntities.SlashResponse
import com.freshsoft.matterbridge.entity.SlashRequest.SlashRequest

import scala.concurrent.Future

/**
	* The global matter bridge result for the client integrations
	*/
trait IMatterBridgeResult {
		def getResult(request: SlashRequest): Future[Option[SlashResponse]]
}
