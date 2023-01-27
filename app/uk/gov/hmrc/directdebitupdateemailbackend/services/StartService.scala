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

import com.google.inject.{Inject, Singleton}
import ddUpdateEmail.models.{NextUrl, Origin}
import ddUpdateEmail.models.journey.{Journey, SessionId, SjRequest, Stage}
import uk.gov.hmrc.directdebitupdateemailbackend.config.AppConfig
import uk.gov.hmrc.directdebitupdateemailbackend.models.{GetBounceStatusResponse, StartResult}
import uk.gov.hmrc.directdebitupdateemailbackend.repositories.JourneyRepo
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, Instant}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StartService @Inject() (
    appConfig:                 AppConfig,
    directDebitBackendService: DirectDebitBackendService,
    journeyIdGenerator:        JourneyIdGenerator,
    clock:                     Clock,
    journeyRepo:               JourneyRepo
)(implicit ec: ExecutionContext) {

  private implicit def toFuture(r: StartResult): Future[StartResult] = Future.successful(r)

  private val nextUrl = NextUrl(s"${appConfig.startNextUrlBase}/direct-debit-verify-email/check-or-change-email-address")

  def start(origin: Origin, sjRequest: SjRequest)(implicit hc: HeaderCarrier): Future[StartResult] =
    hc.sessionId match {
      case None =>
        StartResult.NoSessionId

      case Some(sessionId) =>
        directDebitBackendService.getStatus(sjRequest.ddiNumber).flatMap{
          case None =>
            StartResult.NoDirectDebitFound

          case Some(status) =>
            if (!appConfig.allowedTaxRegimes.contains(status.taxRegime))
              StartResult.TaxRegimeNotAllowed
            else if (!status.isBounced)
              StartResult.IsNotBounced
            else
              initiateSession(origin, sjRequest, SessionId(sessionId.value), status)
                .map(_ => StartResult.Started(nextUrl))

        }
    }

  private def initiateSession(
      origin:    Origin,
      sjRequest: SjRequest,
      sessionId: SessionId,
      status:    GetBounceStatusResponse
  ): Future[Unit] = {
    val journey = Journey.Started(
      journeyIdGenerator.nextJourneyId(),
      origin,
      createdOn = Instant.now(clock),
      sjRequest, sessionId, status.taxRegime, status.email,
      Stage.AfterStarted.Started
    )

    journeyRepo.upsert(journey)
  }

}

