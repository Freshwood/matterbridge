package com.freshsoft.matterbridge.service.database

import java.util.UUID

import data.matterbridge.{
  BaseDataService,
  CodingLoveDataProvider,
  NineGagDataProvider,
  RssConfigDataProvider
}
import model.{CodingLoveEntity, DbEntity, NineGagEntity, RssEntity}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

/**
  * The data service for all data base entities
  */
sealed trait DataService[S <: DbEntity] {

  def byId(id: UUID): Future[Option[S]]

  def byName(name: String): Future[Seq[S]]

  def count: Future[Long]
}

trait NineGagDataService extends DataService[NineGagEntity] {
  implicit def executionContext: ExecutionContext

  def add(name: String, gifUrl: String): Future[Boolean]

  def exists(gifUrl: String): Future[Boolean]
}

trait CodingLoveDataService extends DataService[CodingLoveEntity] {
  implicit def executionContext: ExecutionContext

  def add(name: String, gifUrl: String): Future[Boolean]

  def exists(gifUrl: String): Future[Boolean]
}

trait RssConfigDataService extends DataService[RssEntity] {
  implicit def executionContext: ExecutionContext

  def add(name: String, rssUrl: String, incomingToken: String): Future[Boolean]

  def exists(gifUrl: String): Future[Boolean]

  def all: Future[Seq[RssEntity]]
}

sealed abstract class AbstractDataService[A <: DbEntity, S <: BaseDataService[A]](db: S)
    extends DataService[A] {

  protected val log: Logger

  override def byId(id: UUID): Future[Option[A]] = db.byId(id)

  override def count: Future[Long] = db.count

  override def byName(name: String): Future[Seq[A]] = db.byName(name)
}

class NineGagService(db: NineGagDataProvider)(implicit val executionContext: ExecutionContext)
    extends AbstractDataService[NineGagEntity, NineGagDataProvider](db)
    with NineGagDataService {

  override val log: Logger = LoggerFactory.getLogger(getClass)

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

class CodingLoveService(db: CodingLoveDataProvider)(
    implicit val executionContext: ExecutionContext)
    extends AbstractDataService[CodingLoveEntity, CodingLoveDataProvider](db)
    with CodingLoveDataService {

  override val log: Logger = LoggerFactory.getLogger(getClass)

  override def add(name: String, gifUrl: String): Future[Boolean] = {
    this.exists(gifUrl) flatMap { isExistent =>
      if (isExistent) {
        log.info(s"The coding love gif with the url [$gifUrl] already exists. Skipping entry...")
        Future.successful(false)
      } else {
        log.info(s"Adding coding love gif with name [$name]")
        db.insert(name, gifUrl)
      }
    }
  }

  override def exists(gifUrl: String): Future[Boolean] = db.exists(gifUrl)
}

class RssConfigService(db: RssConfigDataProvider)(implicit val executionContext: ExecutionContext)
    extends AbstractDataService[RssEntity, RssConfigDataProvider](db)
    with RssConfigDataService {

  override val log: Logger = LoggerFactory.getLogger(getClass)

  override def add(name: String, rssUrl: String, incomingToken: String): Future[Boolean] = {
    this.exists(rssUrl) flatMap { isExistent =>
      if (isExistent) {
        log.info(
          s"The rss config with the name [$name] and url [$rssUrl] already exists. Skipping entry...")
        Future.successful(false)
      } else {
        log.info(s"Adding rss config with name [$name]")
        db.insert(name, rssUrl, incomingToken)
      }
    }
  }

  override def exists(gifUrl: String): Future[Boolean] = db.exists(gifUrl)

  override def all: Future[Seq[RssEntity]] = db.all
}
