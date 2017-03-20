package com.freshsoft.matterbridge.server

import akka.http.scaladsl.server.Route
import com.freshsoft.matterbridge.routing._
import com.freshsoft.matterbridge.service.database.{
  BotService,
  CodingLoveService,
  NineGagService,
  RssConfigService
}
import com.freshsoft.matterbridge.util.{DatabaseConfiguration, FlywayService}
import data.matterbridge.{
  BotDataProvider,
  CodingLoveDataProvider,
  NineGagDataProvider,
  RssConfigDataProvider
}

import scala.concurrent.ExecutionContext

/**
  * The service definition for the matter bridge web service
  */
trait MatterBridgeWebService extends DatabaseConfiguration {
  implicit def executionContext: ExecutionContext

  lazy val nineGagDb: NineGagDataProvider = new NineGagDataProvider(jdbcUrl, dbUser, dbPassword)

  lazy val rssConfigDb: RssConfigDataProvider =
    new RssConfigDataProvider(jdbcUrl, dbUser, dbPassword)

  lazy val botDb: BotDataProvider = new BotDataProvider(jdbcUrl, dbUser, dbPassword)

  lazy val nineGagService: NineGagService = new NineGagService(nineGagDb)

  lazy val rssConfigService: RssConfigService = new RssConfigService(rssConfigDb)

  lazy val botService: BotService = new BotService(botDb)

  lazy val slackRoute: Route = new MatterBridgeRoute().routes

  lazy val nineGagRoute: Route = new NineGagRoute(nineGagService).route

  lazy val rssConfigRoute: Route = new RssConfigRoute(rssConfigService).route

  lazy val botRoute: Route = new BotRoute(botService).route

  lazy val webContentRoute: Route = new WebContentRoute(nineGagDb).route
}

trait NineGagActorService extends DatabaseConfiguration {
  implicit def executionContext: ExecutionContext

  lazy val db: NineGagDataProvider = new NineGagDataProvider(jdbcUrl, dbUser, dbPassword)

  lazy val nineGagService: NineGagService = new NineGagService(db)
}

trait CodingLoveActorService extends DatabaseConfiguration {
  implicit def executionContext: ExecutionContext

  lazy val db: CodingLoveDataProvider = new CodingLoveDataProvider(jdbcUrl, dbUser, dbPassword)

  lazy val codingLoveService: CodingLoveService = new CodingLoveService(db)
}

trait RssConfigActorService extends DatabaseConfiguration {
  implicit def executionContext: ExecutionContext

  lazy val rssConfigDb: RssConfigDataProvider =
    new RssConfigDataProvider(jdbcUrl, dbUser, dbPassword)

  lazy val rssConfigService: RssConfigService = new RssConfigService(rssConfigDb)
}

trait BotActorService extends DatabaseConfiguration {
  implicit def executionContext: ExecutionContext

  lazy val botDb: BotDataProvider = new BotDataProvider(jdbcUrl, dbUser, dbPassword)

  lazy val botService: BotService = new BotService(botDb)
}

trait Flyway extends DatabaseConfiguration {
  lazy val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  flywayService.migrateDatabaseSchema()
}
