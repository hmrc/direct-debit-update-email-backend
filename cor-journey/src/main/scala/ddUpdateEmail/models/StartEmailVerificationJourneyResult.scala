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

package ddUpdateEmail.models

import cats.Eq
import play.api.libs.json.Format
import julienrf.json.derived

sealed trait StartEmailVerificationJourneyResult extends Product with Serializable

object StartEmailVerificationJourneyResult {

  implicit val eq: Eq[StartEmailVerificationJourneyResult] = Eq.fromUniversalEquals

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: Format[StartEmailVerificationJourneyResult] = derived.oformat()

  final case class Ok(redirectUrl: String) extends StartEmailVerificationJourneyResult

  case object AlreadyVerified extends StartEmailVerificationJourneyResult

  case object TooManyPasscodeAttempts extends StartEmailVerificationJourneyResult

  case object TooManyPasscodeJourneysStarted extends StartEmailVerificationJourneyResult

  case object TooManyDifferentEmailAddresses extends StartEmailVerificationJourneyResult

}
