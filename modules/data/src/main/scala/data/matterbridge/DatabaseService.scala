package data.matterbridge

import java.util.UUID

import model.{DbEntity, NineGagEntity}
import scalikejdbc._
import scalikejdbc.async.{AsyncConnectionPool, AsyncDB, _}

import scala.concurrent.{ExecutionContext, Future}

/**
  * The data base service definition
  * A simple data access layer
  */
trait BaseDataService {
  type T = DbEntity

  val tableName: String

  def read(id: UUID): Future[Option[T]]

  /*def create(entity: T): Future[T]

  def delete(entity: T): Future[Option[Boolean]]

  def update(entity: T): Future[T]

  def count: Future[Long]*/
}

trait NineGagDataService extends BaseDataService {
  override val tableName: String = "ninegag"
  //def byGifName(gifName: String): T
}

/**
  * Asynchronous version of the database service, uses async PostgresSQL driver underneath.
  */
class NineGagDataProvider(jdbcUrl: String, databaseUser: String, databaseSecret: String)(
    implicit executionContext: ExecutionContext)
    extends NineGagDataService {

  AsyncConnectionPool.singleton(jdbcUrl, databaseUser, databaseSecret)

  override def read(id: UUID): Future[Option[NineGagEntity]] = AsyncDB.withPool { implicit s =>
    sql"SELECT * FROM $tableName WHERE id = $id" map { row =>
      NineGagEntity(UUID.fromString(row.string(1)),
                    row.string(2),
                    row.string(3),
                    row.jodaDateTimeOpt(4),
                    row.jodaDateTimeOpt(5),
                    row.jodaDateTimeOpt(6))
    } single () future ()
  }
}
