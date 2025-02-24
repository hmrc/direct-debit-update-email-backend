/*
 * Copyright 2025 HM Revenue & Customs
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

import ddUpdateEmail.crypto.CryptoFormat
import ddUpdateEmail.models.*
import ddUpdateEmail.models.TaxId.EmpRef
import ddUpdateEmail.models.journey.{Journey, JourneyId, SessionId, SjRequest}
import org.scalatest.freespec.AnyFreeSpecLike
import play.api.libs.json.*
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.directdebitupdateemailbackend.testsupport.RichMatchers
import uk.gov.hmrc.directdebitupdateemailbackend.testsupport.Givens.jsValueCanEqual

import java.time.Instant

class JourneySpec extends AnyFreeSpecLike, RichMatchers {

  given CryptoFormat = CryptoFormat.NoOpCryptoFormat

  val journeyStartedJson: JsValue = Json.parse("""{
                                                 | "Started": {
                                                 | "_id":"journeyId-123",
                                                 | "origin":"BTA",
                                                 | "createdOn":"2023-10-01T15:30:00Z",
                                                 | "sjRequest":{"ddiNumber":"12345","backUrl":"/","returnUrl":"/"},
                                                 | "sessionId":"session-12345",
                                                 | "taxRegime":"paye",
                                                 | "taxId":{"EmpRef":{"value":"1234567"}},
                                                 | "bouncedEmail":"email@test.com"
                                                 | },
                                                 | "sessionId":"session-12345",
                                                 | "createdAt":{"$date":{"$numberLong":"1696174200000"}}
                                                 |}""".stripMargin)

  val journeyStarted: Journey = Journey.Started(
    JourneyId("journeyId-123"),
    Origin.BTA,
    Instant.parse("2023-10-01T15:30:00Z"),
    SjRequest(DDINumber("12345"), BackUrl("/"), ReturnUrl("/")),
    SessionId("session-12345"),
    TaxRegime.Paye,
    Some(EmpRef("1234567")),
    Email(SensitiveString("email@test.com"))
  )

  "Journey JSON serialization and deserialization" - {

    "serialize Journey.Started case class to the correct JSON" in {
      val journeyJson = Json.toJson(journeyStarted).as[JsObject]

      (journeyJson - "lastUpdated") shouldBe journeyStartedJson
      (journeyJson \ "lastUpdated" \ "$date" \ "$numberLong").get shouldBe a[JsString]
    }

    "deserialize from valid Journey.Started JSON to the correct case class" in {
      journeyStartedJson.validate[Journey] match {
        case JsSuccess(result, _) => result shouldBe journeyStarted
        case e: JsError           => fail(s"Expected JsSuccess but got $e")
      }
    }

    "fail to deserialize invalid JSON" in {
      JsString("invalid").validate[Journey].isError shouldBe true
      JsObject(Seq()).validate[Journey].isError shouldBe true
    }
  }

}
