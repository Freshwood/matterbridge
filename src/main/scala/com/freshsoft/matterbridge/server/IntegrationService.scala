package com.freshsoft.matterbridge.server

import model.MatterBridgeEntities.NineGagGifResult
import com.freshsoft.matterbridge.util.DatabaseConfiguration
import scalikejdbc._
import scalikejdbc.async.{AsyncConnectionPool, AsyncDB, _}

import scala.concurrent.{ExecutionContext, Future}

trait Db {
  def firstNineGag: Future[Seq[NineGagGifResult]]
}

trait Database extends DatabaseConfiguration {
  implicit def executor: ExecutionContext
  def db: Db
}

/**
  * Asynchronous version of the database service, uses async PostgreSQL driver underneath.
  *
  */
class AsyncDb(jdbcUrl: String, dbUser: String, dbPassword: String)(implicit ec: ExecutionContext)
    extends Db {

  override def toString: String = s"AsyncDb($jdbcUrl, $dbUser)"

  AsyncConnectionPool.singleton(jdbcUrl, dbUser, dbPassword)

  override def firstNineGag: Future[Seq[NineGagGifResult]] = AsyncDB.withPool { implicit s =>
    sql"SELECT name, gifurl FROM ninegag" map { row =>
      NineGagGifResult(row.string(1), row.string(2))
    } toList () future ()
  }
}

/**
  * Synchronous version of the database service, uses JDBC underneath.
  *
  */
class SyncDb(jdbcUrl: String, dbUser: String, dbPassword: String, driver: String)(
    implicit ec: ExecutionContext)
    extends Db {

  override def toString: String = s"SyncDb($jdbcUrl, $dbUser)"

  Class.forName(driver)
  ConnectionPool.singleton(jdbcUrl, dbUser, dbPassword)

  override def firstNineGag: Future[Seq[NineGagGifResult]] = Future {
    DB.readOnly { implicit s =>
      sql"SELECT name, gifurl FROM ninegag" map { row =>
        NineGagGifResult(row.string(1), row.string(2))
      } toList () apply ()
    }
  }
}

trait AsyncDatabase extends Database {
  lazy val db = new AsyncDb(jdbcUrl, dbUser, dbPassword)
}

trait SyncDatabase extends Database {
  lazy val db = new SyncDb(jdbcUrl, dbUser, dbPassword, dbDriver)
}

/**
  * Created by Freshwood on 13.03.2017.
  */
class IntegrationService(implicit val executor: ExecutionContext) extends AsyncDatabase {
  def nineGag: Future[Seq[NineGagGifResult]] = {
    db.firstNineGag map println
    db.firstNineGag
  }
}
