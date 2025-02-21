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

import ddUpdateEmail.crypto.CryptoFormat
import ddUpdateEmail.models.TaxId.{EmpRef, Vrn, Zppt, Zsdl}
import ddUpdateEmail.models.TaxRegime
import org.scalatest.freespec.AnyFreeSpecLike
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.directdebitupdateemailbackend.testsupport.{RichMatchers, TestData}

class GetBounceStatusResponseSpec extends AnyFreeSpecLike, RichMatchers {

  given CryptoFormat = CryptoFormat.NoOpCryptoFormat

  "GetBounceStatusResponse " - {

    List(
      ("paye", TaxRegime.Paye, "empref", EmpRef("1234567")),
      ("vatc", TaxRegime.VatC, "vrn", Vrn("123456")),
      ("ppt", TaxRegime.Ppt, "zppt", Zppt("12345")),
      ("zsdl", TaxRegime.Zsdl, "zsdl", Zsdl("1234"))
    ).foreach { case (taxRegimeString, expectedTaxRegime, taxIdType, expectedTaxId) =>
      s"must be able to read for taxRegime=$taxRegimeString and taxIdType=$taxIdType" in {
        val json = Json.parse(
          s"""{
               |  "isBounced": true,
               |  "email": "${TestData.bouncedEmail.value.decryptedValue}",
               |  "taxRegime": "$taxRegimeString",
               |  "taxId": {
               |    "type": "$taxIdType",
               |    "value": "${expectedTaxId.value}"
               |  }
               |}""".stripMargin
        )
        json.validate[GetBounceStatusResponse] shouldEqual JsSuccess(
          GetBounceStatusResponse(
            isBounced = true,
            TestData.bouncedEmail,
            expectedTaxRegime,
            Some(expectedTaxId)
          )
        )
      }

    }

    "must be able to read when there is no taxId in the response" in {
      val json = Json.parse(
        s"""{
           |  "isBounced": false,
           |  "email": "${TestData.bouncedEmail.value.decryptedValue}",
           |  "taxRegime": "paye"
           |}""".stripMargin
      )
      json.validate[GetBounceStatusResponse] shouldEqual JsSuccess(
        GetBounceStatusResponse(
          isBounced = false,
          TestData.bouncedEmail,
          TaxRegime.Paye,
          None
        )
      )
    }

  }

}
