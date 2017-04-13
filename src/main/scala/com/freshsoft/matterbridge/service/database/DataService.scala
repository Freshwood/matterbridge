package com.freshsoft.matterbridge.service.database

import java.util.UUID

import data.matterbridge._
import model._
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

/**
  * The data service for all data base entities
  */
sealed trait DataService[S <: DbEntity] {

  def byId(id: UUID): Future[Option[S]]

  def delete(id: UUID): Future[Boolean]

  def restore(id: UUID): Future[Boolean]

  def byName(name: String): Future[Seq[S]]

  def count: Future[Long]
}

trait NineGagDataService extends DataService[NineGagEntity] {
  implicit def executionContext: ExecutionContext

  def add(name: String, gifUrl: String, categoryId: UUID): Future[Boolean]

  def exists(gifUrl: String): Future[Boolean]

  def last: Future[Seq[NineGagEntity]]
}

trait CodingLoveDataService extends DataService[CodingLoveEntity] {
  implicit def executionContext: ExecutionContext

  def add(name: String, gifUrl: String): Future[Boolean]

  def exists(gifUrl: String): Future[Boolean]

  def last: Future[Seq[CodingLoveEntity]]
}

trait RssConfigDataService extends DataService[RssEntity] {
  implicit def executionContext: ExecutionContext

  def add(name: String, rssUrl: String, incomingToken: String): Future[Boolean]

  def exists(gifUrl: String): Future[Boolean]

  def all: Future[Seq[RssEntity]]

  def update(id: UUID): Future[Boolean]
}

trait BotDataService extends DataService[BotEntity] {
  implicit def executionContext: ExecutionContext

  def add(name: String): Future[Boolean]

  def exists(botName: String): Future[Boolean]

  def all: Future[Seq[BotEntity]]

  def update(id: UUID, name: String): Future[Boolean]

  def updateResource(id: UUID, value: String): Future[Boolean]

  def addResource(botId: UUID, value: String): Future[Boolean]

  def allResources(botId: UUID): Future[Seq[BotEntityResource]]

  def randomBotMessage(botId: UUID): Future[Option[BotEntityResource]]

  def deleteResource(id: UUID): Future[Boolean]

  def allDeleted: Future[Seq[BotEntity]]
}

trait CategoryDataService extends DataService[CategoryEntity] {
  implicit def executionContext: ExecutionContext

  def add(categoryName: String): Future[Boolean]

  def exists(categoryName: String): Future[Boolean]

  def all: Future[Seq[CategoryEntity]]
}

trait WebDataService {
  implicit def executionContext: ExecutionContext

  def overallCount: Future[EntityStatistic]
}

sealed abstract class AbstractDataService[A <: DbEntity, S <: BaseDataService[A]](db: S)
    extends DataService[A] {

  protected val log: Logger

  override def byId(id: UUID): Future[Option[A]] = db.byId(id)

  override def count: Future[Long] = db.count

  override def byName(name: String): Future[Seq[A]] = db.byName(name)

  override def delete(id: UUID): Future[Boolean] = db.delete(id)

  override def restore(id: UUID) = db.restore(id)
}

class NineGagService(db: NineGagDataProvider, categoryDb: CategoryDataProvider)(
    implicit val executionContext: ExecutionContext)
    extends AbstractDataService[NineGagEntity, NineGagDataProvider](db)
    with NineGagDataService {

  override val log: Logger = LoggerFactory.getLogger(getClass)

  override def add(name: String, gifUrl: String, categoryId: UUID): Future[Boolean] = {
    this.exists(gifUrl) flatMap { isExistent =>
      if (isExistent) {
        log.info(s"The 9gag gif with the url [$gifUrl] already exists. Skipping entry...")
        Future.successful(false)
      } else {
        log.info(s"Adding 9gag gif with name [$name]")
        db.insert(name, gifUrl, categoryId)
      }
    }
  }

  override def exists(gifUrl: String): Future[Boolean] = db.exists(gifUrl)

  override def last: Future[Seq[NineGagEntity]] = db.last
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

  override def last: Future[Seq[CodingLoveEntity]] = db.last
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

  override def exists(rssName: String): Future[Boolean] = db.exists(rssName)

  override def all: Future[Seq[RssEntity]] = db.all

  override def update(id: UUID): Future[Boolean] = db.update(id)
}

class BotService(db: BotDataProvider)(implicit val executionContext: ExecutionContext)
    extends AbstractDataService[BotEntity, BotDataProvider](db)
    with BotDataService {

  override val log: Logger = LoggerFactory.getLogger(getClass)

  override def add(name: String): Future[Boolean] = db.insert(name)

  override def exists(botName: String): Future[Boolean] = db.exists(botName)

  override def all: Future[Seq[BotEntity]] = db.all

  override def update(id: UUID, name: String): Future[Boolean] = db.update(id, name)

  override def updateResource(id: UUID, value: String): Future[Boolean] =
    db.updateResource(id, value)

  override def addResource(botId: UUID, value: String): Future[Boolean] =
    db.insertResource(botId, value)

  override def allResources(botId: UUID): Future[Seq[BotEntityResource]] = db.allResources(botId)

  override def randomBotMessage(botId: UUID): Future[Option[BotEntityResource]] =
    db.allResources(botId) map { resources =>
      if (resources.nonEmpty) {
        Option(resources.toVector(Random.nextInt(resources.length)))
      } else {
        None
      }
    }

  override def deleteResource(id: UUID) = db.deleteResource(id)

  override def allDeleted = db.allDeleted
}

class CategoryService(db: CategoryDataProvider)(implicit val executionContext: ExecutionContext)
    extends AbstractDataService[CategoryEntity, CategoryDataProvider](db)
    with CategoryDataService {
  override protected val log: Logger = LoggerFactory.getLogger(getClass)

  override def add(categoryName: String): Future[Boolean] = db.insert(categoryName)

  override def exists(categoryName: String): Future[Boolean] = db.exists(categoryName)

  override def all: Future[Seq[CategoryEntity]] = db.all
}

class WebService(
    nineGagDataProvider: NineGagDataProvider,
    codingLoveDataProvider: CodingLoveDataProvider,
    rssConfigDataProvider: RssConfigDataProvider,
    botDataProvider: BotDataProvider,
    categoryDataProvider: CategoryDataProvider)(implicit val executionContext: ExecutionContext)
    extends WebDataService {

  override def overallCount: Future[EntityStatistic] =
    for {
      nineGagCount <- nineGagDataProvider.count
      codingLoveCount <- codingLoveDataProvider.count
      rssCount <- rssConfigDataProvider.count
      botCount <- botDataProvider.count
      categoryCount <- categoryDataProvider.count
    } yield EntityStatistic(nineGagCount, codingLoveCount, rssCount, botCount, categoryCount)

}
