package com.freshsoft.matterbridge.service.database

import java.util.UUID

import data.matterbridge.NineGagDataProvider
import model.{DbEntity, NineGagEntity}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * The data service for all data base entities
  */
sealed trait DataService {
  type T >: DbEntity
}

trait NineGagDataService {
  implicit def executionContext: ExecutionContext

  def read(id: UUID): Future[Option[NineGagEntity]]

  def byName(name: String): Future[Seq[NineGagEntity]]

  def count: Future[Long]

  def add(name: String, gifUrl: String): Future[Boolean]

  def exists(gifUrl: String): Future[Boolean]
}

class NineGagService(db: NineGagDataProvider)(implicit val executionContext: ExecutionContext)
    extends NineGagDataService {

  private val log = LoggerFactory.getLogger(getClass)

  override def read(id: UUID): Future[Option[NineGagEntity]] = db.byId(id)

  override def byName(name: String): Future[Seq[NineGagEntity]] = db.byName(s"%$name%")

  override def count: Future[Long] = db.count

  override def add(name: String, gifUrl: String): Future[Boolean] = {
    this.exists(gifUrl) flatMap { isExistent =>
      if (isExistent) {
        log.info(s"The 9gag gif with the url [$gifUrl] already exists. Skipping entry...")
        Future.successful(false)
      } else {
        log.info(s"Adding 9gag gif with name [$name]")
        db.insert(name, gifUrl)
      }
    }
  }

  override def exists(gifUrl: String): Future[Boolean] = db.exists(gifUrl)
}
