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

  def read(id: UUID): Future[Option[T]]

  def byName(name: String): Future[Seq[T]]

  /*def create(entity: T): Future[T]

  def delete(entity: T): Future[Option[Boolean]]

  def update(entity: T): Future[T]

  def count: Future[Long]*/
}

/**
  * Asynchronous version of the database service, uses async PostgresSQL driver underneath.
  */
class NineGagDataProvider(jdbcUrl: String, databaseUser: String, databaseSecret: String)(
    implicit executionContext: ExecutionContext)
    extends BaseDataService {

  AsyncConnectionPool.singleton(jdbcUrl, databaseUser, databaseSecret)

  override def read(id: UUID): Future[Option[NineGagEntity]] = AsyncDB.withPool { implicit s =>
    sql"SELECT * FROM ninegag WHERE id = $id" map { row =>
      NineGagEntity(UUID.fromString(row.string(1)),
                    row.string(2),
                    row.string(3),
                    row.jodaDateTimeOpt(4),
                    row.jodaDateTimeOpt(5))
    } single () future ()
  }

  override def byName(searchName: String): Future[Seq[NineGagEntity]] = AsyncDB.withPool {
    implicit s =>
      sql"SELECT * FROM ninegag WHERE name LIKE {search}"
        .bindByName('search -> searchName) map { row =>
        NineGagEntity(UUID.fromString(row.string(1)),
                      row.string(2),
                      row.string(3),
                      row.jodaDateTimeOpt(4),
                      row.jodaDateTimeOpt(5))
      } list () future ()
  }
}
