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

package uk.gov.hmrc.directdebitupdateemailbackend.testsupport

import ddUpdateEmail.models.{BackUrl, DDINumber, Email, ReturnUrl}
import ddUpdateEmail.models.journey.{SessionId, SjRequest}
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

object TestData {

  val sessionId: SessionId = SessionId("session-12345")

  val ddiNumber: DDINumber = DDINumber("0123456789")

  val bouncedEmail: Email = Email(SensitiveString("bounced@email.com"))

  val internalAuthToken: String = "123567345789"

  val sjRequest: SjRequest = SjRequest(
    ddiNumber,
    BackUrl("/back"),
    ReturnUrl("/return")
  )

}
