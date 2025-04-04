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

package ddUpdateEmail.models.journey

import ddUpdateEmail.crypto.CryptoFormat
import ddUpdateEmail.models.{Email, EmailVerificationResult, Origin, StartEmailVerificationJourneyResult, TaxId, TaxRegime}
import io.circe.generic.semiauto.deriveCodec
import play.api.libs.json.{JsObject, JsValue, Json, OFormat, OWrites}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import ddUpdateEmail.utils.DeriveJson
import ddUpdateEmail.utils.DeriveJson.Circe.codec

import java.time.{Clock, Instant}

sealed trait Journey derives CanEqual {
  val _id: JourneyId
  val origin: Origin
  val createdOn: Instant
  val lastUpdated: Instant = Instant.now(Clock.systemUTC())
  val sjRequest: SjRequest
  val sessionId: SessionId
  val taxRegime: TaxRegime
  val taxId: Option[TaxId]
  val bouncedEmail: Email
}

object Journey {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given format(using cryptoFormat: CryptoFormat): OFormat[Journey] = {
    val defaultFormat: OFormat[Journey] = DeriveJson.Circe.format(deriveCodec[Journey])

    // we need to write some extra fields on the top of the structure so it's
    // possible to index on them and use them in queries
    val customWrites = OWrites[Journey](j =>
      defaultFormat.writes(j) ++ Json.obj(
        "sessionId"   -> j.sessionId.value,
        "createdAt"   -> MongoJavatimeFormats.instantFormat.writes(j.createdOn),
        "lastUpdated" -> MongoJavatimeFormats.instantFormat.writes(j.lastUpdated)
      )
    )
    OFormat(
      defaultFormat.preprocess { case j: JsObject =>
        j - "lastUpdated" - "createdAt" - "sessionId" - "_id"
      },
      customWrites
    )
  }

  implicit class JourneyOps(private val j: Journey) extends AnyVal {

    def json(using CryptoFormat): JsValue = Json.toJson(j)

  }

  sealed trait BeforeSelectedEmail extends Journey

  sealed trait AfterSelectedEmail extends Journey {
    val selectedEmail: Email
  }

  sealed trait BeforeEmailVerificationJourneyStarted extends Journey

  sealed trait AfterEmailVerificationJourneyStarted extends Journey {
    val startEmailVerificationJourneyResult: StartEmailVerificationJourneyResult
  }

  sealed trait BeforeEmailVerificationResult extends Journey

  sealed trait AfterEmailVerificationResult extends Journey {
    val emailVerificationResult: EmailVerificationResult
  }

  final case class Started(
    override val _id:          JourneyId,
    override val origin:       Origin,
    override val createdOn:    Instant,
    override val sjRequest:    SjRequest,
    override val sessionId:    SessionId,
    override val taxRegime:    TaxRegime,
    override val taxId:        Option[TaxId],
    override val bouncedEmail: Email
  ) extends Journey
      with BeforeSelectedEmail
      with BeforeEmailVerificationJourneyStarted
      with BeforeEmailVerificationResult

  final case class SelectedEmail(
    override val _id:           JourneyId,
    override val origin:        Origin,
    override val createdOn:     Instant,
    override val sjRequest:     SjRequest,
    override val sessionId:     SessionId,
    override val taxRegime:     TaxRegime,
    override val taxId:         Option[TaxId],
    override val bouncedEmail:  Email,
    override val selectedEmail: Email
  ) extends Journey
      with AfterSelectedEmail
      with BeforeEmailVerificationJourneyStarted
      with BeforeEmailVerificationResult

  final case class EmailVerificationJourneyStarted(
    override val _id:                                 JourneyId,
    override val origin:                              Origin,
    override val createdOn:                           Instant,
    override val sjRequest:                           SjRequest,
    override val sessionId:                           SessionId,
    override val taxRegime:                           TaxRegime,
    override val taxId:                               Option[TaxId],
    override val bouncedEmail:                        Email,
    override val selectedEmail:                       Email,
    override val startEmailVerificationJourneyResult: StartEmailVerificationJourneyResult
  ) extends Journey
      with AfterSelectedEmail
      with AfterEmailVerificationJourneyStarted
      with BeforeEmailVerificationResult

  final case class ObtainedEmailVerificationResult(
    override val _id:                                 JourneyId,
    override val origin:                              Origin,
    override val createdOn:                           Instant,
    override val sjRequest:                           SjRequest,
    override val sessionId:                           SessionId,
    override val taxRegime:                           TaxRegime,
    override val taxId:                               Option[TaxId],
    override val bouncedEmail:                        Email,
    override val selectedEmail:                       Email,
    override val startEmailVerificationJourneyResult: StartEmailVerificationJourneyResult,
    override val emailVerificationResult:             EmailVerificationResult
  ) extends Journey
      with AfterSelectedEmail
      with AfterEmailVerificationJourneyStarted
      with AfterEmailVerificationResult

}
