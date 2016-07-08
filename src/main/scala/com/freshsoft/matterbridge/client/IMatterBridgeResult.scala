package com.freshsoft.matterbridge.client

import com.freshsoft.matterbridge.entity.MattermostEntities.SlashResponse
import com.freshsoft.matterbridge.entity.SlashCommandRequest

import scala.concurrent.Future

/**
	* The global matter bridge result for the client integrations
	*/
trait IMatterBridgeResult {

	/**
		* Get the SlashResponse result for this integration
 *
		* @param request The SlashRequest to build the response
		* @return A Future of SlashResponse
		*/
		def getResult(request: SlashCommandRequest): Future[Option[SlashResponse]]
}
