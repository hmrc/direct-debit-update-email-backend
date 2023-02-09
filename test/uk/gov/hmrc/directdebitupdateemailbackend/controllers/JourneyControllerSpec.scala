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
import ddUpdateEmail.models.{BackUrl, DDINumber, Email, Origin, ReturnUrl, TaxRegime}
import ddUpdateEmail.models.journey.{Journey, SessionId, SjRequest}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{SessionId => HttpSessionId}
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.directdebitupdateemailbackend.repositories.JourneyRepo
import uk.gov.hmrc.directdebitupdateemailbackend.testsupport.{ItSpec, TestData}
import uk.gov.hmrc.http.HeaderCarrier

class JourneyControllerSpec extends ItSpec {

  lazy val connector = app.injector.instanceOf[JourneyConnector]

  lazy val journeyRepo = app.injector.instanceOf[JourneyRepo]

  s"GET ${routes.JourneyController.findLatestJourneyBySessionId.url}" - {

    "must return an error if no session id can be found" in {
      val result = connector.findLatestJourneyBySessionId()(HeaderCarrier())

      an[Exception] shouldBe thrownBy(await(result))
    }

    "must return None if a journey cannot be found for a session id" in {
      val result = connector.findLatestJourneyBySessionId()(HeaderCarrier(sessionId = Some(HttpSessionId(TestData.sessionId.value))))

      await(result) shouldBe None
    }

    "must return the latest journey for a session id if one can be found" in {
      val journey1 = Journey.Started(
        journeyIdGenerator.nextJourneyId(),
        Origin.BTA,
        frozenZonedDateTime.toInstant,
        SjRequest(DDINumber("12345"), BackUrl("/"), ReturnUrl("/")),
        TestData.sessionId,
        TaxRegime.Paye,
        Email(SensitiveString("email@test.com"))
      )

      // same session id, created later
      val journey2 = journey1.copy(
        _id       = journeyIdGenerator.nextJourneyId(),
        createdOn = journey1.createdOn.plusSeconds(1L)
      )

      // different session id
      val journey3 = journey1.copy(
        _id       = journeyIdGenerator.nextJourneyId(),
        sessionId = SessionId(journey1.sessionId.value + "0000")
      )

      await(journeyRepo.upsert(journey1)) shouldBe ()
      await(journeyRepo.upsert(journey2)) shouldBe ()
      await(journeyRepo.upsert(journey3)) shouldBe ()

      val result = connector.findLatestJourneyBySessionId()(HeaderCarrier(sessionId = Some(HttpSessionId(TestData.sessionId.value))))
      await(result) shouldBe Some(journey2)
    }

  }

}
