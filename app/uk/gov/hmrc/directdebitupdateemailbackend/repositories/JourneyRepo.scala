/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.directdebitupdateemailbackend.repositories

import ddUpdateEmail.crypto.CryptoFormat.OperationalCryptoFormat
import ddUpdateEmail.models.journey.{Journey, JourneyId, SessionId}
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Indexes}
import uk.gov.hmrc.directdebitupdateemailbackend.config.AppConfig
import uk.gov.hmrc.directdebitupdateemailbackend.repositories.JourneyRepoUtil.*
import uk.gov.hmrc.directdebitupdateemailbackend.repositories.JourneyRepoUtil.journeyId
import uk.gov.hmrc.directdebitupdateemailbackend.repositories.JourneyRepoUtil.journeyIdExtractor
import uk.gov.hmrc.directdebitupdateemailbackend.repositories.Repo.{Id, IdExtractor}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

@Singleton
final class JourneyRepo @Inject() (
  mongoComponent: MongoComponent,
  config:         AppConfig
)(using ExecutionContext, OperationalCryptoFormat)
    extends Repo[JourneyId, Journey](
      collectionName = "journey",
      mongoComponent = mongoComponent,
      indexes = JourneyRepoUtil.indexes(config.journeyRepoTtl),
      extraCodecs = Codecs.playFormatSumCodecs(Journey.format),
      replaceIndexes = true
    ) {

  /** Find the latest journey for given sessionId.
    */
  def findLatestJourney(sessionId: SessionId): Future[Option[Journey]] =
    collection
      .find(filter = Filters.eq("sessionId", sessionId.value))
      .sort(BsonDocument("createdAt" -> -1))
      .headOption()

}

object JourneyRepoUtil {

  given journeyId: Id[JourneyId] = new Id[JourneyId] {
    override def value(i: JourneyId): String = i.value
  }

  given journeyIdExtractor: IdExtractor[Journey, JourneyId] = new IdExtractor[Journey, JourneyId] {
    override def id(j: Journey): JourneyId = j._id
  }

  def indexes(cacheTtl: FiniteDuration): Seq[IndexModel] = Seq(
    IndexModel(
      keys = Indexes.ascending("lastUpdated"),
      indexOptions = IndexOptions().expireAfter(cacheTtl.toSeconds, TimeUnit.SECONDS).name("lastUpdatedIdx")
    )
  )

}
