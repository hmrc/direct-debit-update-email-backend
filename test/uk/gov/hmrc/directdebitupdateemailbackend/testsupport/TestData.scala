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

import ddUpdateEmail.models.{BackUrl, DDINumber, Email, EmailVerificationResult, Origin, ReturnUrl, StartEmailVerificationJourneyResult, TaxId, TaxRegime}
import ddUpdateEmail.models.journey.{Journey, JourneyId, SessionId, SjRequest}
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.http.Authorization

import java.time.{Instant, LocalDateTime, ZoneOffset}

object TestData {

  val bearerToken: Authorization = Authorization("Bearer 1234567841323231213")

  val journeyId: JourneyId = JourneyId("b6217497-ab5b-4e93-855a-afc9f9e933b6")

  val sessionId: SessionId = SessionId("session-12345")

  val ddiNumber: DDINumber = DDINumber("0123456789")

  val bouncedEmail: Email = Email(SensitiveString("bounced@email.com"))

  val internalAuthToken: String = "123567345789"

  val sjRequest: SjRequest = SjRequest(
    ddiNumber,
    BackUrl("/back"),
    ReturnUrl("/return")
  )

  val taxId: TaxId = TaxId.EmpRef("12345")

  val selectedEmail: Email = Email(SensitiveString("selected@email.com"))

  val createdOn: Instant = LocalDateTime.parse("2057-11-02T16:28:55.185").toInstant(ZoneOffset.UTC)

  val emailVerificationRedirectUrl = "/redirect"

  object Journeys {

    def afterStarted(origin: Origin = Origin.BTA, taxRegime: TaxRegime = TaxRegime.Paye) =
      Journey.Started(
        journeyId,
        origin,
        createdOn,
        sjRequest,
        sessionId,
        taxRegime,
        Some(taxId),
        bouncedEmail
      )

    def afterSelectedEmail(
      selectedEmail: Email = selectedEmail,
      origin:        Origin = Origin.BTA,
      taxRegime:     TaxRegime = TaxRegime.Paye
    ) =
      Journey.SelectedEmail(
        journeyId,
        origin,
        createdOn,
        sjRequest,
        sessionId,
        taxRegime,
        Some(taxId),
        bouncedEmail,
        selectedEmail
      )

    def afterEmailVerificationJourneyStarted(
      startVerificationResult: StartEmailVerificationJourneyResult =
        StartEmailVerificationJourneyResult.Ok(emailVerificationRedirectUrl),
      selectedEmail:           Email = selectedEmail,
      origin:                  Origin = Origin.BTA,
      taxRegime:               TaxRegime = TaxRegime.Paye
    ) =
      Journey.EmailVerificationJourneyStarted(
        journeyId,
        origin,
        createdOn,
        sjRequest,
        sessionId,
        taxRegime,
        Some(taxId),
        bouncedEmail,
        selectedEmail,
        startVerificationResult
      )

    def afterEmailVerificationResult(
      result:        EmailVerificationResult = EmailVerificationResult.Verified,
      selectedEmail: Email = selectedEmail,
      origin:        Origin = Origin.BTA,
      taxRegime:     TaxRegime = TaxRegime.Paye
    ) =
      Journey.ObtainedEmailVerificationResult(
        journeyId,
        origin,
        createdOn,
        sjRequest,
        sessionId,
        taxRegime,
        Some(taxId),
        bouncedEmail,
        selectedEmail,
        StartEmailVerificationJourneyResult.Ok(emailVerificationRedirectUrl),
        result
      )

  }

}
