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

package uk.gov.hmrc.directdebitupdateemailbackend.models

import ddUpdateEmail.models.NextUrl

sealed trait StartResult

object StartResult {

  case object NoSessionId extends StartResult

  case object NoDirectDebitFound extends StartResult

  case object TaxRegimeNotAllowed extends StartResult

  case object IsNotBounced extends StartResult

  final case class Started(nextUrl: NextUrl) extends StartResult

}
