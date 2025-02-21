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
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyConnector @Inject() (httpClient: HttpClientV2, servicesConfig: ServicesConfig)(using
  ExecutionContext,
  OperationalCryptoFormat
) {

  private val baseUrl: String = servicesConfig.baseUrl("direct-debit-update-email-backend")

  def findLatestJourneyBySessionId()(implicit hc: HeaderCarrier): Future[Option[Journey]] =
    for {
      _      <- Future(require(hc.sessionId.isDefined, "Missing required 'SessionId'"))
      result <- httpClient
                  .get(url"$baseUrl/direct-debit-update-email/journey/find-latest-by-session-id")
                  .execute[Option[Journey]]
    } yield result

  def updateSelectedEmail(journeyId: JourneyId, selectedEmail: Email)(implicit hc: HeaderCarrier): Future[Journey] =
    httpClient
      .post(url"$baseUrl/direct-debit-update-email/journey/${journeyId.value}/selected-email")
      .withBody(Json.toJson(selectedEmail))
      .execute[Journey]

  def updateStartEmailVerificationJourneyResult(journeyId: JourneyId, result: StartEmailVerificationJourneyResult)(
    implicit hc: HeaderCarrier
  ): Future[Journey] =
    httpClient
      .post(url"$baseUrl/direct-debit-update-email/journey/${journeyId.value}/start-verification-journey-result")
      .withBody(Json.toJson(result))
      .execute[Journey]

  def updateEmailVerificationResult(journeyId: JourneyId, result: EmailVerificationResult)(implicit
    hc: HeaderCarrier
  ): Future[Journey] =
    httpClient
      .post(url"$baseUrl/direct-debit-update-email/journey/${journeyId.value}/email-verification-result")
      .withBody(Json.toJson(result))
      .execute[Journey]

}
