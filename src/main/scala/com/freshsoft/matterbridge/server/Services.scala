package com.freshsoft.matterbridge.server

import akka.http.scaladsl.server.Route
import com.freshsoft.matterbridge.routing.{MatterBridgeRoute, NineGagRoute, WebContentRoute}
import com.freshsoft.matterbridge.service.database.{CodingLoveService, NineGagService}
import com.freshsoft.matterbridge.util.{DatabaseConfiguration, FlywayService}
import data.matterbridge.{CodingLoveDataProvider, NineGagDataProvider}

import scala.concurrent.ExecutionContext

/**
  * The service definition for the matter bridge web service
  */
trait MatterBridgeWebService extends DatabaseConfiguration {
  implicit def executionContext: ExecutionContext

  lazy val db: NineGagDataProvider = new NineGagDataProvider(jdbcUrl, dbUser, dbPassword)

  lazy val nineGagService: NineGagService = new NineGagService(db)

  lazy val slackRoute: Route = new MatterBridgeRoute().routes

  lazy val nineGagRoute: Route = new NineGagRoute(nineGagService).route

  lazy val webContentRoute: Route = new WebContentRoute(db).route
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

trait Flyway extends DatabaseConfiguration {
  lazy val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  flywayService.migrateDatabaseSchema()
}
