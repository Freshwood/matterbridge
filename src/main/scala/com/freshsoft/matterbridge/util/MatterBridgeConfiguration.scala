package com.freshsoft.matterbridge.util

import com.freshsoft.matterbridge.entity.MatterBridgeEntities.RssFeedConfigEntry
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConversions._
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
  val matterBridgeCommand: String = config.getString("matterbridge.command")
  val matterBridgeResponseType: String = config.getString("matterbridge.response_type")
  val codingLoveCommand: String =
    config.getString("matterbridge.integrations.codinglove.command")
  val codingLoveResponseType: String =
    config.getString("matterbridge.integrations.codinglove.response_type")
  val nineGagCommand: String =
    config.getString("matterbridge.integrations.ninegag.command")
  val nineGagResponseType: String =
    config.getString("matterbridge.integrations.ninegag.response_type")
  val nineGagMaximumGifStore: Int =
    config.getInt("matterbridge.integrations.ninegag.max_gif_store")
  val newsriverApiToken: String =
    config.getString("matterbridge.integrations.newsriver.api_token")
  val newsriverIncomingTokenUrl: String =
    config.getString("matterbridge.integrations.newsriver.incoming_token")
  val newsriverCommand: String =
    config.getString("matterbridge.integrations.newsriver.command")
  val newsriverResponseType: String =
    config.getString("matterbridge.integrations.newsriver.response_type")
  val rssFeedList: List[RssFeedConfigEntry] =
    config.getConfigList("matterbridge.integrations.rss") map { p =>
      RssFeedConfigEntry(p.getString("url"), p.getString("incoming_token"), p.getString("name"))
    } toList

  val botMap: Map[String, String] =
    config.getConfigList("matterbridge.integrations.bot") flatMap { entry =>
      Map(entry.getString("key") -> entry.getString("value"))
    } toMap
}

trait DatabaseConfiguration extends MatterBridgeConfigHolder {
  private lazy val databaseConfig = config.getConfig("database")

  lazy val jdbcUrl: String = databaseConfig.getString("url")
  lazy val dbUser: String = databaseConfig.getString("user")
  lazy val dbPassword: String = databaseConfig.getString("password")
  lazy val dbDriver: String = databaseConfig.getString("driver")
}
