package com.freshsoft.matterbridge.server

import akka.http.scaladsl.server.Route
import com.freshsoft.matterbridge.routing.WebContentRoute
import com.freshsoft.matterbridge.util.{DatabaseConfiguration, FlywayService}
import data.matterbridge.NineGagDataProvider

import scala.concurrent.ExecutionContext

/**
  * The service definition for the matter bridge web service
  */
trait MatterBridgeWebService extends DatabaseConfiguration {
  implicit def executionContext: ExecutionContext

  lazy val db: NineGagDataProvider = new NineGagDataProvider(jdbcUrl, dbUser, dbPassword)

  val routes: Route = new WebContentRoute(db).route
}

trait Flyway extends DatabaseConfiguration {
  lazy val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  flywayService.migrateDatabaseSchema()
}
