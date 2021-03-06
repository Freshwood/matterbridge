package com.freshsoft.matterbridge.util

import com.typesafe.config.{Config, ConfigFactory}

import scala.language.postfixOps

/**
	* The global config property holder
	*/
trait MatterBridgeConfigHolder {

  lazy val config: Config = ConfigFactory.load("application.conf")
}

/**
	* The matter bridge server configuration
	*/
trait MatterBridgeServerConfig extends MatterBridgeConfigHolder {
  lazy val host: String = config.getString("http.host")
  lazy val port: Int = config.getInt("http.port")
}

/**
	* The trait which includes all matterbridge integration configurations
	*/
trait MatterBridgeConfig extends MatterBridgeServerConfig {
  lazy val codingLoveCommand: String =
    config.getString("matterbridge.integrations.codinglove.command")
  lazy val codingLoveResponseType: String =
    config.getString("matterbridge.integrations.codinglove.response_type")
  lazy val nineGagApiUrl: String =
    config.getString("matterbridge.integrations.ninegag.api-url")
  lazy val nineGagCommand: String =
    config.getString("matterbridge.integrations.ninegag.command")
  lazy val nineGagResponseType: String =
    config.getString("matterbridge.integrations.ninegag.response_type")
}

trait DatabaseConfiguration extends MatterBridgeConfigHolder {
  println(config.root())

  private lazy val databaseConfig = config.getConfig("database")

  lazy val jdbcUrl: String = databaseConfig.getString("url")
  lazy val dbUser: String = databaseConfig.getString("user")
  lazy val dbPassword: String = databaseConfig.getString("password")
  lazy val dbDriver: String = databaseConfig.getString("driver")
}
