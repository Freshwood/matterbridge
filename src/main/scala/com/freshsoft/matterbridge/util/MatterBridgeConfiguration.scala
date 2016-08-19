package com.freshsoft.matterbridge.util

import com.freshsoft.matterbridge.entity.MatterBridgeEntities.RssFeedConfigEntry
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._
import scala.language.postfixOps

/**
	* The global config property holder
	*/
trait MatterBridgeConfigHolder {

	val config = ConfigFactory.load()
}

/**
	* The matter bridge server configuration
	*/
trait MatterBridgeServerConfig extends MatterBridgeConfigHolder {
	val host = config.getString("http.host")
	val port = config.getInt("http.port")
}

/**
	* The trait which includes all matterbridge integration configurations
	*/
trait MatterBridgeIntegrationsConfig extends MatterBridgeServerConfig {
	val matterBridgeCommand = config.getString("matterbridge.command")
	val matterBridgeResponseType = config.getString("matterbridge.response_type")
	val codingLoveCommand = config.getString("matterbridge.integrations.codinglove.command")
	val codingLoveResponseType = config.getString("matterbridge.integrations.codinglove.response_type")
	val nineGagCommand = config.getString("matterbridge.integrations.ninegag.command")
	val nineGagResponseType = config.getString("matterbridge.integrations.ninegag.response_type")
	val nineGagMaximumGifStore = config.getInt("matterbridge.integrations.ninegag.max_gif_store")
	val newsriverIncomingTokenUrl = config.getString("matterbridge.integrations.newsriver.incoming_token")
	val newsriverCommand = config.getString("matterbridge.integrations.newsriver.command")
	val newsriverResponseType = config.getString("matterbridge.integrations.newsriver.response_type")
	val rssFeedList: List[RssFeedConfigEntry] = config.getConfigList("matterbridge.integrations.rss") map { p =>
			RssFeedConfigEntry(p.getString("url"), p.getString("incoming_token"), p.getString("name")) } toList
}

/**
	* Only a trait tag
	*/
trait WithConfig extends MatterBridgeIntegrationsConfig