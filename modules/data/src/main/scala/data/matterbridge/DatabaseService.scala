package data.matterbridge

import java.util.UUID

import model.{DbEntity, NineGagEntity}
import org.joda.time.DateTime
import scalikejdbc._
import scalikejdbc.async.{AsyncConnectionPool, AsyncDB, _}

import scala.concurrent.{ExecutionContext, Future}

/**
  * The data base service definition
  * A simple data access layer
  */
sealed trait BaseDataService {
  type T >: DbEntity

  def read(id: UUID): Future[Option[T]]

  def byName(name: String): Future[Seq[T]]

  def count: Future[Long]
}

sealed trait NineGagDataService extends BaseDataService {

  def insert(name: String, gifUrl: String): Future[Boolean]

  def exists(gifUrl: String): Future[Boolean]
}

/**
  * Asynchronous version of the database service, uses async PostgresSQL driver underneath.
  */
class NineGagDataProvider(jdbcUrl: String, databaseUser: String, databaseSecret: String)(
    implicit executionContext: ExecutionContext)
    extends NineGagDataService {

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

  override def insert(name: String, gifUrl: String): Future[Boolean] = AsyncDB.localTx {
    implicit s =>
      val now = DateTime.now()
      val update = SQL[NineGagEntity](
        "INSERT INTO ninegag(id, name, gifurl, created_at) VALUES(?, ?, ?, ?);"
      ) bind (UUID.randomUUID(), name, gifUrl, now) update () future ()
      update map (_ == 1)
  }

  override def count: Future[Long] = AsyncDB.withPool { implicit s =>
    sql"SELECT count(*) as count FROM ninegag" map { row =>
      row.long(1)
    } single () future () map (_.getOrElse(0))
  }

  override def exists(gifUrl: String): Future[Boolean] = AsyncDB.withPool { implicit s =>
    sql"SELECT count(*) as count FROM ninegag WHERE gifurl = $gifUrl" map { row =>
      row.long(1)
    } single () future () map { result =>
      val test = result.getOrElse(0)
      if (test == 0) false else true
    }
  }
}
