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
import ddUpdateEmail.utils.EnumFormat
import enumeratum.{EnumEntry, PlayEnum}
import play.api.libs.json.Format

import scala.collection.immutable

sealed trait EmailVerificationResult extends EnumEntry, Product, Serializable derives CanEqual

object EmailVerificationResult extends PlayEnum[EmailVerificationResult] {

  given Eq[EmailVerificationResult] = Eq.fromUniversalEquals

  given Format[EmailVerificationResult] = EnumFormat(EmailVerificationResult)

  case object Verified extends EmailVerificationResult

  case object Locked extends EmailVerificationResult

  override val values: immutable.IndexedSeq[EmailVerificationResult] = findValues

}
