package com.freshsoft.matterbridge.client.rss

import akka.actor.{Actor, Props}
import com.freshsoft.matterbridge.entity.MatterBridgeEntities.IncomingResponse
import com.freshsoft.matterbridge.server.WithActorContext
import com.freshsoft.matterbridge.util.{MatterBridgeHttpClient, WithConfig}

/**
	* Created by Freshwood on 16.08.2016.
	*/
object RssIntegration extends WithConfig with WithActorContext {

	val rssReaderActor = system.actorOf(Props(classOf[RssReaderActor]))

	class RssReaderActor extends Actor {
		override def receive: Receive = {
			case "Start" => rssFeedList.map {
				rss => MatterBridgeHttpClient.postToIncomingWebhook(rss.incoming_token, IncomingResponse("Test " + rss.url, List()))
			}
		}
	}
}
