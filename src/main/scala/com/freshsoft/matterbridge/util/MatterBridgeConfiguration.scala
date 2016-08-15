package com.freshsoft.matterbridge.util

import com.typesafe.config.ConfigFactory

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
}

/**
	* Only a trait tag
	*/
trait WithConfig extends MatterBridgeIntegrationsConfig