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

package uk.gov.hmrc.directdebitupdateemailbackend.services

import ddUpdateEmail.models.journey.{Journey, JourneyId, SessionId}
import ddUpdateEmail.utils.Errors
import uk.gov.hmrc.directdebitupdateemailbackend.repositories.JourneyRepo

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyService @Inject() (
  journeyRepo: JourneyRepo
)(using ExecutionContext) {

  def findLatestJourney(sessionId: SessionId): Future[Option[Journey]] =
    journeyRepo.findLatestJourney(sessionId)

  def get(journeyId: JourneyId): Future[Journey] =
    journeyRepo.findById(journeyId).map { maybeJourney =>
      maybeJourney.getOrElse(Errors.throwNotFoundException("Could not find journey"))
    }

  def upsert(journey: Journey): Future[Journey] =
    journeyRepo.upsert(journey).map(_ => journey)

}
