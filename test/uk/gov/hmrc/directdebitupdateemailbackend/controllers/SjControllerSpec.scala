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

import org.apache.pekko.stream.Materializer
import ddUpdateEmail.models.TaxId.EmpRef
import ddUpdateEmail.models.{Origin, TaxRegime}
import ddUpdateEmail.models.journey.Journey
import play.api.libs.json.{JsNull, JsNumber, JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.directdebitupdateemailbackend.repositories.JourneyRepo
import uk.gov.hmrc.directdebitupdateemailbackend.testsupport.FakeRequestUtils.FakeRequestOps
import uk.gov.hmrc.directdebitupdateemailbackend.testsupport.stubs.{DirectDebitBackendStub, InternalAuthStub}
import uk.gov.hmrc.directdebitupdateemailbackend.testsupport.{ItSpec, TestData}
import uk.gov.hmrc.directdebitupdateemailbackend.testsupport.Givens.jsValueCanEqual
import uk.gov.hmrc.http.UpstreamErrorResponse

class SjControllerSpec extends ItSpec {

  lazy val controller = app.injector.instanceOf[SjController]

  lazy implicit val mat: Materializer = app.injector.instanceOf[Materializer]

  lazy val journeyRepo: JourneyRepo = app.injector.instanceOf[JourneyRepo]

  "SjController" - {

    List(
      (routes.SjController.startBta, Origin.BTA, controller.startBta, "direct-debit-update-email/bta/start"),
      (
        routes.SjController.startEpaye,
        Origin.EpayeService,
        controller.startEpaye,
        "direct-debit-update-email/epaye/start"
      )
    ).foreach { case (call, origin, doStart, internalAuthResourceLocation) =>
      s"when handling requests to start at ${call.url} for origin ${origin.toString} must" - {

        val sjRequestJson: JsValue = Json.parse(
          s"""{  "ddiNumber": "${TestData.ddiNumber.value}",  "backUrl": "/back",  "returnUrl": "/return" }"""
        )

        val validFakeRequest =
          FakeRequest()
            .withJsonBodyAndJsonContentType(sjRequestJson)
            .withAuthorization(TestData.internalAuthToken)
            .withXSessionId(TestData.sessionId)

        "return a 400 (BAD_REQUEST) if there is no x-session-id header" in {
          InternalAuthStub.authorise()

          val request =
            FakeRequest().withJsonBodyAndJsonContentType(sjRequestJson).withAuthorization(TestData.internalAuthToken)

          val result = doStart(request)
          status(result) shouldBe BAD_REQUEST
          contentAsJson(result) shouldBe Json.parse(
            """{
                  |  "statusCode": 400,
                  |  "message": "session ID not found"
                  |}""".stripMargin
          )

          InternalAuthStub.ensureAuthoriseCalledForInternalAuth(internalAuthResourceLocation)
        }

        "return a 400 (BAD_REQUEST) if the JSON body cannot be parsed" in {
          InternalAuthStub.authorise()

          val request =
            FakeRequest().withJsonBodyAndJsonContentType(JsNumber(1)).withAuthorization(TestData.internalAuthToken)

          val result = doStart(request)
          status(result) shouldBe BAD_REQUEST
          contentAsJson(result) shouldBe Json.parse(
            """{
                |  "statusCode": 400,
                |  "message": "request body cannot be parsed"
                |}""".stripMargin
          )

          InternalAuthStub.ensureAuthoriseCalledForInternalAuth(internalAuthResourceLocation)
        }

        "return a 404 (NOT_FOUND) if a direct debit cannot be found" in {
          InternalAuthStub.authorise()
          DirectDebitBackendStub.stubGetBouncedEmailStatus(
            TestData.ddiNumber,
            NOT_FOUND,
            Some(
              Json.parse(
                """{
                    |  "code": "NOT_FOUND",
                    |  "reason": "The DDINumber could not be found"
                    |}""".stripMargin
              )
            )
          )

          val result = doStart(validFakeRequest)
          status(result) shouldBe NOT_FOUND
          contentAsJson(result) shouldBe Json.parse(
            """{
                  |  "statusCode": 404,
                  |  "message": "direct debit not found"
                  |}""".stripMargin
          )

          InternalAuthStub.ensureAuthoriseCalledForInternalAuth(internalAuthResourceLocation)
        }

        "return a 403 (FORBIDDEN) if the direct debit is for a tax regime which is not allowed" in {
          InternalAuthStub.authorise()
          DirectDebitBackendStub.stubGetBouncedEmailStatus(
            TestData.ddiNumber,
            OK,
            Some(
              Json.parse(
                s"""{
                    |  "isBounced": true,
                    |  "email": "${TestData.bouncedEmail.value.decryptedValue}",
                    |  "taxRegime": "ppt"
                    |}""".stripMargin
              )
            )
          )

          val result = doStart(validFakeRequest)
          status(result) shouldBe FORBIDDEN
          contentAsJson(result) shouldBe Json.parse(
            """{
                  |  "statusCode": 403,
                  |  "message": "tax regime not allowed"
                  |}""".stripMargin
          )

          InternalAuthStub.ensureAuthoriseCalledForInternalAuth(internalAuthResourceLocation)
        }

        "return a 409 (CONFLICT) if the email is not bounced" in {
          InternalAuthStub.authorise()
          DirectDebitBackendStub.stubGetBouncedEmailStatus(
            TestData.ddiNumber,
            OK,
            Some(
              Json.parse(
                s"""{
                     |  "isBounced": false,
                     |  "email": "${TestData.bouncedEmail.value.decryptedValue}",
                     |  "taxRegime": "paye"
                     |}""".stripMargin
              )
            )
          )

          val result = doStart(validFakeRequest)
          status(result) shouldBe CONFLICT

          contentAsJson(result) shouldBe Json.parse(
            """{
                  |  "statusCode": 409,
                  |  "message": "email not bounced"
                  |}""".stripMargin
          )
          InternalAuthStub.ensureAuthoriseCalledForInternalAuth(internalAuthResourceLocation)
        }

        "throw an exception if direct-debit-backend returns a 404 (NOT_FOUND) response without a body saying " +
          "the DD cannot be found" in {
            InternalAuthStub.authorise()
            DirectDebitBackendStub.stubGetBouncedEmailStatus(
              TestData.ddiNumber,
              NOT_FOUND,
              Some(JsNull)
            )

            an[UpstreamErrorResponse] shouldBe thrownBy(await(doStart(validFakeRequest)))
            InternalAuthStub.ensureAuthoriseCalledForInternalAuth(internalAuthResourceLocation)
          }

        "return a 201 (CREATED) if session has been created" in {
          val empRef = EmpRef("12345")
          InternalAuthStub.authorise()
          DirectDebitBackendStub.stubGetBouncedEmailStatus(
            TestData.ddiNumber,
            OK,
            Some(
              Json.parse(
                s"""{
                     |  "isBounced": true,
                     |  "email": "${TestData.bouncedEmail.value.decryptedValue}",
                     |  "taxRegime": "paye",
                     |  "taxId": {
                     |    "type": "empref",
                     |    "value": "${empRef.value}"
                     |  }
                     |}""".stripMargin
              )
            )
          )

          val journeyId = journeyIdGenerator.readNextJourneyId()
          val result    = doStart(validFakeRequest)

          status(result) shouldBe CREATED
          val body = contentAsJson(result)
          body shouldBe Json.parse(
            """{ "nextUrl": "http://localhost:10801/direct-debit-verify-email/check-or-change-email-address" }"""
          )

          await(journeyRepo.findLatestJourney(TestData.sessionId)) shouldBe Some(
            Journey.Started(
              journeyId,
              origin,
              frozenZonedDateTime.toInstant,
              TestData.sjRequest,
              TestData.sessionId,
              TaxRegime.Paye,
              Some(empRef),
              TestData.bouncedEmail
            )
          )

          InternalAuthStub.ensureAuthoriseCalledForInternalAuth(internalAuthResourceLocation)
        }

      }

    }

  }

}
