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

package ddUpdateEmail.connectors

import com.google.inject.{Inject, Singleton}
import ddUpdateEmail.crypto.CryptoFormat.OperationalCryptoFormat
import ddUpdateEmail.models.journey.{Journey, JourneyId}
import ddUpdateEmail.models.{Email, EmailVerificationResult, StartEmailVerificationJourneyResult}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyConnector @Inject() (httpClient: HttpClient, servicesConfig: ServicesConfig)(implicit ec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) {

  private val baseUrl: String = servicesConfig.baseUrl("direct-debit-update-email-backend")

  def findLatestJourneyBySessionId()(implicit hc: HeaderCarrier): Future[Option[Journey]] = {
    for {
      _ <- Future(require(hc.sessionId.isDefined, "Missing required 'SessionId'"))
      result <- httpClient.GET[Option[Journey]](s"$baseUrl/direct-debit-update-email/journey/find-latest-by-session-id")
    } yield result
  }

  def updateSelectedEmail(journeyId: JourneyId, selectedEmail: Email)(implicit hc: HeaderCarrier): Future[Journey] =
    httpClient.POST[Email, Journey](
      s"$baseUrl/direct-debit-update-email/journey/${journeyId.value}/selected-email",
      selectedEmail
    )

  def updateStartEmailVerificationJourneyResult(journeyId: JourneyId, result: StartEmailVerificationJourneyResult)(implicit hc: HeaderCarrier): Future[Journey] =
    httpClient.POST[StartEmailVerificationJourneyResult, Journey](
      s"$baseUrl/direct-debit-update-email/journey/${journeyId.value}/start-verification-journey-result",
      result
    )

  def updateEmailVerificationResult(journeyId: JourneyId, result: EmailVerificationResult)(implicit hc: HeaderCarrier): Future[Journey] =
    httpClient.POST[EmailVerificationResult, Journey](
      s"$baseUrl/direct-debit-update-email/journey/${journeyId.value}/email-verification-result",
      result
    )

}
