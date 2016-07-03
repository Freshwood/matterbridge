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
	val codingLoveCommand = config.getString("matterbridge.integrations.codinglove.command")
	val responseType = config.getString("matterbridge.integrations.codinglove.response_type")
}

/**
	* Only a trait tag
	*/
trait WithConfig extends MatterBridgeIntegrationsConfig