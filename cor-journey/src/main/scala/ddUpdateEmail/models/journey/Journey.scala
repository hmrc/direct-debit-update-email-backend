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
import ddUpdateEmail.models.{Email, Origin, TaxRegime}
import julienrf.json.derived
import play.api.libs.json.{JsValue, Json, OFormat, OWrites}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Clock, Instant}

sealed trait Journey {
  val _id: JourneyId
  val origin: Origin
  val createdOn: Instant
  val lastUpdated: Instant = Instant.now(Clock.systemUTC())
  val sjRequest: SjRequest
  val sessionId: SessionId
  val taxRegime: TaxRegime
  val bouncedEmail: Email
  val stage: Stage
}

object Journey {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit def format(implicit cryptoFormat: CryptoFormat): OFormat[Journey] = {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    val defaultFormat: OFormat[Journey] = derived.oformat[Journey]()

    //we need to write some extra fields on the top of the structure so it's
    //possible to index on them and use them in queries
    val customWrites = OWrites[Journey](j =>
      defaultFormat.writes(j) ++ Json.obj(
        "sessionId" -> j.sessionId.value,
        "createdAt" -> MongoJavatimeFormats.instantFormat.writes(j.createdOn),
        "lastUpdated" -> MongoJavatimeFormats.instantFormat.writes(j.lastUpdated)
      ))
    OFormat(
      defaultFormat,
      customWrites
    )
  }

  implicit class JourneyOps(private val j: Journey) extends AnyVal {

    def json(implicit cryptoFormat: CryptoFormat): JsValue = Json.toJson(j)

  }

  sealed trait BeforeSelectedEmail extends Journey

  sealed trait AfterSelectedEmail extends Journey {
    val selectedEmail: Email
  }

  final case class Started(
      override val _id:          JourneyId,
      override val origin:       Origin,
      override val createdOn:    Instant,
      override val sjRequest:    SjRequest,
      override val sessionId:    SessionId,
      override val taxRegime:    TaxRegime,
      override val bouncedEmail: Email,
      override val stage:        Stage.AfterStarted
  ) extends Journey
    with BeforeSelectedEmail

  final case class SelectedEmail(
      override val _id:           JourneyId,
      override val origin:        Origin,
      override val createdOn:     Instant,
      override val sjRequest:     SjRequest,
      override val sessionId:     SessionId,
      override val taxRegime:     TaxRegime,
      override val bouncedEmail:  Email,
      override val stage:         Stage.AfterSelectedEmail,
      override val selectedEmail: Email
  ) extends Journey
    with AfterSelectedEmail

}

