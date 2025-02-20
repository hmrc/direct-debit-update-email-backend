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

package uk.gov.hmrc.directdebitupdateemailbackend.controllers

import ddUpdateEmail.connectors.JourneyConnector
import ddUpdateEmail.models.EmailVerificationResult
import play.api.test.Helpers._
import uk.gov.hmrc.directdebitupdateemailbackend.testsupport.stubs.AuthStub
import uk.gov.hmrc.directdebitupdateemailbackend.testsupport.{ItSpec, TestData}
import uk.gov.hmrc.http.UpstreamErrorResponse

class UpdateEmailVerificationResultControllerSpec extends ItSpec {

  lazy val journeyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/email-verification-result" - {

    behave like authenticatedJourneyEndpointBehaviour(
      journeyConnector.updateEmailVerificationResult(TestData.journeyId, EmailVerificationResult.Verified)(_)
    )

    "must return a 400 (BAD_REQUEST) if an email verification journey has not been started yet" in {
      AuthStub.authorise()

      insertJourneyForTest(TestData.Journeys.afterSelectedEmail())

      val error = intercept[UpstreamErrorResponse](
        await(journeyConnector.updateEmailVerificationResult(TestData.journeyId, EmailVerificationResult.Verified))
      )
      error.statusCode shouldBe BAD_REQUEST

      AuthStub.ensureAuthoriseCalled()
    }

    EmailVerificationResult.values.foreach { verificationResult =>
      s"must update the status if an email verification journey has just been started [verificationResult = ${verificationResult.entryName}]" in {
        AuthStub.authorise()

        insertJourneyForTest(TestData.Journeys.afterEmailVerificationJourneyStarted())

        val result =
          journeyConnector.updateEmailVerificationResult(TestData.journeyId, verificationResult)
        await(result) shouldBe TestData.Journeys.afterEmailVerificationResult(verificationResult)

        AuthStub.ensureAuthoriseCalled()
      }
    }

    "must update the status if an email verification result was already in the journey" in {
      AuthStub.authorise()

      insertJourneyForTest(TestData.Journeys.afterEmailVerificationResult())

      val result =
        journeyConnector.updateEmailVerificationResult(TestData.journeyId, EmailVerificationResult.Locked)
      await(result) shouldBe TestData.Journeys.afterEmailVerificationResult(EmailVerificationResult.Locked)

      AuthStub.ensureAuthoriseCalled()
    }

  }

}
