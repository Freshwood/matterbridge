package com.freshsoft.matterbridge.service.database

import java.util.UUID

import data.matterbridge.NineGagDataProvider
import model.{DbEntity, NineGagEntity}

import scala.concurrent.{ExecutionContext, Future}

/**
  * The data service for all data base entities
  */
trait DataService {
  type T = DbEntity
}

trait NineGagDataService {
  implicit def executionContext: ExecutionContext

  def read(id: UUID): Future[Option[NineGagEntity]]

  def byName(name: String): Future[Seq[NineGagEntity]]
}

class NineGagService(db: NineGagDataProvider)(implicit val executionContext: ExecutionContext)
    extends NineGagDataService {

  override def read(id: UUID): Future[Option[NineGagEntity]] = db.read(id)

  override def byName(name: String): Future[Seq[NineGagEntity]] = db.byName(s"%$name%")
}
