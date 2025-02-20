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
import ddUpdateEmail.models.StartEmailVerificationJourneyResult
import play.api.test.Helpers._
import uk.gov.hmrc.directdebitupdateemailbackend.testsupport.stubs.AuthStub
import uk.gov.hmrc.directdebitupdateemailbackend.testsupport.{ItSpec, TestData}
import uk.gov.hmrc.http.UpstreamErrorResponse

class UpdateStartVerificationJourneyResultControllerSpec extends ItSpec {

  lazy val journeyConnector = app.injector.instanceOf[JourneyConnector]

  "POST /journey/:journeyId/start-verification-journey-result" - {

    behave like authenticatedJourneyEndpointBehaviour(
      journeyConnector.updateStartEmailVerificationJourneyResult(
        TestData.journeyId,
        StartEmailVerificationJourneyResult.TooManyPasscodeJourneysStarted
      )(_)
    )

    "must return a 400 (BAD_REQUEST) if an email has not been selected yet" in {
      AuthStub.authorise()

      insertJourneyForTest(TestData.Journeys.afterStarted())

      val error = intercept[UpstreamErrorResponse](
        await(
          journeyConnector.updateStartEmailVerificationJourneyResult(
            TestData.journeyId,
            StartEmailVerificationJourneyResult.AlreadyVerified
          )
        )
      )
      error.statusCode shouldBe BAD_REQUEST

      AuthStub.ensureAuthoriseCalled()
    }

    List(
      StartEmailVerificationJourneyResult.Ok("/redirect"),
      StartEmailVerificationJourneyResult.AlreadyVerified,
      StartEmailVerificationJourneyResult.TooManyPasscodeJourneysStarted,
      StartEmailVerificationJourneyResult.TooManyPasscodeAttempts,
      StartEmailVerificationJourneyResult.TooManyDifferentEmailAddresses
    ).foreach { startResult =>
      s"must update the status if an email has just been selected [startResult = ${startResult.getClass.getSimpleName}]" in {
        AuthStub.authorise()

        insertJourneyForTest(TestData.Journeys.afterSelectedEmail())

        val result =
          journeyConnector.updateStartEmailVerificationJourneyResult(TestData.journeyId, startResult)
        await(result) shouldBe TestData.Journeys.afterEmailVerificationJourneyStarted(startResult)

        AuthStub.ensureAuthoriseCalled()
      }
    }

    "must update the status if a start verification result was already in the journey" in {
      AuthStub.authorise()

      insertJourneyForTest(TestData.Journeys.afterEmailVerificationJourneyStarted())

      val result =
        journeyConnector.updateStartEmailVerificationJourneyResult(
          TestData.journeyId,
          StartEmailVerificationJourneyResult.TooManyPasscodeJourneysStarted
        )
      await(result) shouldBe TestData.Journeys.afterEmailVerificationJourneyStarted(
        StartEmailVerificationJourneyResult.TooManyPasscodeJourneysStarted
      )

      AuthStub.ensureAuthoriseCalled()
    }

  }

}
