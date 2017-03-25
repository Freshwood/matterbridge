package com.freshsoft.matterbridge.util

import com.typesafe.config.{Config, ConfigFactory}

import scala.language.postfixOps

/**
	* The global config property holder
	*/
trait MatterBridgeConfigHolder {

  val config: Config = ConfigFactory.load()
}

/**
	* The matter bridge server configuration
	*/
trait MatterBridgeServerConfig extends MatterBridgeConfigHolder {
  val host: String = config.getString("http.host")
  val port: Int = config.getInt("http.port")
}

/**
	* The trait which includes all matterbridge integration configurations
	*/
trait MatterBridgeConfig extends MatterBridgeServerConfig {
  val codingLoveCommand: String =
    config.getString("matterbridge.integrations.codinglove.command")
  val codingLoveResponseType: String =
    config.getString("matterbridge.integrations.codinglove.response_type")
  val nineGagCommand: String =
    config.getString("matterbridge.integrations.ninegag.command")
  val nineGagResponseType: String =
    config.getString("matterbridge.integrations.ninegag.response_type")
  val newsriverApiToken: String =
    config.getString("matterbridge.integrations.newsriver.api_token")
  val newsriverIncomingTokenUrl: String =
    config.getString("matterbridge.integrations.newsriver.incoming_token")
  val newsriverCommand: String =
    config.getString("matterbridge.integrations.newsriver.command")
  val newsriverResponseType: String =
    config.getString("matterbridge.integrations.newsriver.response_type")
}

trait DatabaseConfiguration extends MatterBridgeConfigHolder {
  private lazy val databaseConfig = config.getConfig("database")

  lazy val jdbcUrl: String = databaseConfig.getString("url")
  lazy val dbUser: String = databaseConfig.getString("user")
  lazy val dbPassword: String = databaseConfig.getString("password")
  lazy val dbDriver: String = databaseConfig.getString("driver")
}
