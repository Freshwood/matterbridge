package data.matterbridge

import java.util.UUID

import model.{CodingLoveEntity, DbEntity, NineGagEntity, RssEntity}
import org.joda.time.DateTime
import scalikejdbc._
import scalikejdbc.async.{AsyncConnectionPool, AsyncDB, _}

import scala.concurrent.{ExecutionContext, Future}

/**
  * The data base service definition
  * A simple data access layer
  */
trait BaseDataService[S <: DbEntity] {

  val table: String

  def byId(id: UUID): Future[Option[S]]

  def count: Future[Long]

  def byName(name: String): Future[Seq[S]]
}

/**
  * Asynchronous version of the database service, uses async PostgresSQL driver underneath.
  */
sealed abstract class AbstractDataService[S <: DbEntity](
    implicit executionContext: ExecutionContext)
    extends BaseDataService[S] {

  val resultSetToEntity: WrappedResultSet => S

  val resultSetToCount: WrappedResultSet => Long = row => row.long(1)

  override def byId(id: UUID): Future[Option[S]] = AsyncDB.withPool { implicit s =>
    val query = s"SELECT * FROM $table WHERE id = $id"
    SQL(query) map resultSetToEntity single () future ()
  }

  override def count: Future[Long] = AsyncDB.withPool { implicit s =>
    val query = s"SELECT count(*) as count FROM $table"
    SQL(query) map resultSetToCount single () future () map
      (_.getOrElse(0))
  }
}

sealed trait NineGagDataService extends AbstractDataService[NineGagEntity] {

  override val table: String = "ninegag"

  def insert(name: String, gifUrl: String): Future[Boolean]

  def exists(gifUrl: String): Future[Boolean]
}

sealed trait CodingLoveDataService extends AbstractDataService[CodingLoveEntity] {

  override val table: String = "codinglove"

  def insert(name: String, gifUrl: String): Future[Boolean]

  def exists(gifUrl: String): Future[Boolean]
}

sealed trait RssConfigDataService extends AbstractDataService[RssEntity] {

  override val table: String = "rss"

  def insert(name: String, rssUrl: String, incomingToken: String): Future[Boolean]

  def exists(rssUrl: String): Future[Boolean]

  def all: Future[Seq[RssEntity]]

  def update(id: UUID): Future[Boolean]
}

class NineGagDataProvider(jdbcUrl: String, databaseUser: String, databaseSecret: String)(
    implicit executionContext: ExecutionContext)
    extends NineGagDataService {

  AsyncConnectionPool.singleton(jdbcUrl, databaseUser, databaseSecret)

  override val resultSetToEntity: WrappedResultSet => NineGagEntity = row => {
    NineGagEntity(UUID.fromString(row.string(1)),
                  row.string(2),
                  row.string(3),
                  row.jodaDateTimeOpt(4),
                  row.jodaDateTimeOpt(5))
  }

  override def byName(searchName: String): Future[Seq[NineGagEntity]] = AsyncDB.withPool {
    implicit s =>
      val query = s"SELECT * FROM $table WHERE name LIKE '%$searchName%'"
      SQL(query) map resultSetToEntity list () future ()
  }

  override def insert(name: String, gifUrl: String): Future[Boolean] = AsyncDB.withPool {
    implicit s =>
      val query = s"INSERT INTO $table(id, name, gifurl, created_at) VALUES(?, ?, ?, ?);"
      val now = DateTime.now()
      val update = SQL[NineGagEntity](
        query
      ) bind (UUID.randomUUID(), name, gifUrl, now) update () future ()
      update map (_ == 1)
  }

  override def exists(gifUrl: String): Future[Boolean] = AsyncDB.withPool { implicit s =>
    val query = s"SELECT count(*) as count FROM $table WHERE gifurl = '$gifUrl'"
    SQL(query) map resultSetToCount single () future () map { result =>
      val test = result.getOrElse(0)
      if (test == 0) false else true
    }
  }
}

class CodingLoveDataProvider(jdbcUrl: String, databaseUser: String, databaseSecret: String)(
    implicit executionContext: ExecutionContext)
    extends CodingLoveDataService {

  AsyncConnectionPool.singleton(jdbcUrl, databaseUser, databaseSecret)

  override val resultSetToEntity: WrappedResultSet => CodingLoveEntity = row => {
    CodingLoveEntity(UUID.fromString(row.string(1)),
                     row.string(2),
                     row.string(3),
                     row.jodaDateTimeOpt(4),
                     row.jodaDateTimeOpt(5))
  }

  override def byName(searchName: String): Future[Seq[CodingLoveEntity]] = AsyncDB.withPool {
    implicit s =>
      val query = s"SELECT * FROM $table WHERE name LIKE '%$searchName%'"
      SQL(query) map resultSetToEntity list () future ()
  }

  override def insert(name: String, gifUrl: String): Future[Boolean] = AsyncDB.withPool {
    implicit s =>
      val query = s"INSERT INTO $table(id, name, gifurl, created_at) VALUES(?, ?, ?, ?);"
      val now = DateTime.now()
      val update = SQL[CodingLoveEntity](
        query
      ) bind (UUID.randomUUID(), name, gifUrl, now) update () future ()
      update map (_ == 1)
  }

  override def exists(gifUrl: String): Future[Boolean] = AsyncDB.withPool { implicit s =>
    val query = s"SELECT count(*) as count FROM $table WHERE gifurl = '$gifUrl'"
    SQL(query) map resultSetToCount single () future () map { result =>
      val test = result.getOrElse(0)
      if (test == 0) false else true
    }
  }
}

class RssConfigDataProvider(jdbcUrl: String, databaseUser: String, databaseSecret: String)(
    implicit executionContext: ExecutionContext)
    extends RssConfigDataService {

  AsyncConnectionPool.singleton(jdbcUrl, databaseUser, databaseSecret)

  override val resultSetToEntity: WrappedResultSet => RssEntity = row => {
    RssEntity(UUID.fromString(row.string(1)),
              row.string(2),
              row.string(3),
              row.string(4),
              row.jodaDateTimeOpt(5),
              row.jodaDateTimeOpt(6),
              row.jodaDateTimeOpt(7))
  }

  override def byName(searchName: String): Future[Seq[RssEntity]] = AsyncDB.withPool {
    implicit s =>
      val query = s"SELECT * FROM $table WHERE name LIKE '%$searchName%'"
      SQL(query) map resultSetToEntity list () future ()
  }

  override def insert(name: String, rssUrl: String, incomingToken: String): Future[Boolean] =
    AsyncDB.withPool { implicit s =>
      val query =
        s"INSERT INTO $table(id, name, rss_url, incoming_token, created_at) VALUES(?, ?, ?, ?, ?);"
      val now = DateTime.now()
      val update = SQL[RssEntity](
        query
      ) bind (UUID.randomUUID(), name, rssUrl, incomingToken, now) update () future ()
      update map (_ == 1)
    }

  override def exists(rssName: String): Future[Boolean] = AsyncDB.withPool { implicit s =>
    val query = s"SELECT count(*) as count FROM $table WHERE name = '$rssName'"
    SQL(query) map resultSetToCount single () future () map { result =>
      val test = result.getOrElse(0)
      if (test == 0) false else true
    }
  }

  override def all: Future[Seq[RssEntity]] = AsyncDB.withPool { implicit s =>
    val query = s"SELECT * FROM $table"
    SQL(query) map resultSetToEntity list () future ()
  }

  override def update(id: UUID): Future[Boolean] =
    AsyncDB.withPool { implicit s =>
      val now = DateTime.now()
      val query = s"Update $table SET updated_at = '$now' WHERE id = '$id'"
      SQL(query) update () future () map (_ == 1)
    }
}
