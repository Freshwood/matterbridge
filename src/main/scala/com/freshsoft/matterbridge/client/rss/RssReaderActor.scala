package com.freshsoft.matterbridge.client.rss

import akka.actor.Actor
import akka.event.Logging
import com.freshsoft.matterbridge.entity.MatterBridgeEntities.RssReaderActorModel
import com.freshsoft.matterbridge.server.WithActorContext
import com.freshsoft.matterbridge.util.WithConfig

/**
	* The rss reader actor looks for the rss reader configurations and triggers the worker actor
	*/
class RssReaderActor extends Actor with WithActorContext with WithConfig {

	val log = Logging.getLogger(system, this)

	override def receive: Receive = {
		case RssReaderActorModel.Start => rssFeedList foreach { feed =>
			log.info(s"Start reading rss feed from ${feed.url} \nScan Time ${feed.lastScanTime}")
			RssIntegration.rssReaderWorkerActor ! feed
			log.info(s"Reading ${feed.url} done")
		}
	}
}
