package data.matterbridge

import model.DbEntity

import scala.concurrent.ExecutionContext

/**
  * The data base related model and service definition
  */
trait DatabaseConfiguration {
  val jdbcUrl: String
  val databaseUser: String
  val databaseSecret: String
  val databaseDriver: String
}

trait DbProvider extends DatabaseConfiguration {
  implicit def executor: ExecutionContext

  def db[A <: DbEntity]: BaseDataService[A]
}

trait NineGagDatabase extends DbProvider {
  lazy val db = new NineGagDataProvider(jdbcUrl, databaseUser, databaseSecret)
}
