package com.freshsoft.matterbridge.client

import com.freshsoft.matterbridge.client.ninegag.NineGagIntegration
import model.MatterBridgeEntities.{NineGagGifResult, SlashResponse}
import com.freshsoft.matterbridge.server.MatterBridgeContext
import com.freshsoft.matterbridge.util.MatterBridgeConfig
import model.SlashCommandRequest

import scala.concurrent.Future

/**
	* The global matter bridge integration
	*/
object MatterBridgeIntegration
    extends IMatterBridgeResult
    with MatterBridgeConfig
    with MatterBridgeContext {

  private val responseMessage = (x: Int, y: NineGagGifResult) =>
    s"9Gag Gifs [$x] Last Gif:\n${y.key}\nUrl: ${y.gifUrl}"

  /**
		* Get the SlashResponse result for this integration
		*
		* @param request The SlashRequest to build the response
		* @return A Future of SlashResponse
		*/
  override def getResult(request: SlashCommandRequest): Future[Option[SlashResponse]] = {
    Future {
      Some(
        SlashResponse(matterBridgeResponseType,
                      responseMessage(NineGagIntegration.nineGagGifs.size,
                                      NineGagIntegration.lastGif),
                      List()))
    }
  }
}
