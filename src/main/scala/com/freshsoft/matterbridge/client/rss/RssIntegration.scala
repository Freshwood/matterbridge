package com.freshsoft.matterbridge.client.rss

import akka.actor.{ActorRef, Props}
import com.freshsoft.matterbridge.server.MatterBridgeContext
import com.freshsoft.matterbridge.util.MatterBridgeConfig

/**
  * The rss reader integration which does not produce a matter bridge result
  * cause it only send incoming requests to slack/mattermost
  */
object RssIntegration extends MatterBridgeConfig with MatterBridgeContext {

  val rssReaderActor: ActorRef = system.actorOf(Props(classOf[RssReaderActor]))
}
