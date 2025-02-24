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

package uk.gov.hmrc.directdebitupdateemailbackend.testsupport.stubs

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, urlPathEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import ddUpdateEmail.models.DDINumber
import play.api.libs.json.{JsValue, Json}

object DirectDebitBackendStub {

  private def getBouncedEmailStatusUrl(ddiNumber: DDINumber): String =
    s"/direct-debit-backend/bounced-email/status/${ddiNumber.value}"

  type HttpStatus = Int

  def stubGetBouncedEmailStatus(
    ddiNumber:      DDINumber,
    responseStatus: HttpStatus,
    responseBody:   Option[JsValue]
  ): StubMapping =
    stubFor(
      get(urlPathEqualTo(getBouncedEmailStatusUrl(ddiNumber)))
        .willReturn {
          lazy val response = aResponse().withStatus(responseStatus)
          responseBody.fold(
            response
          )(body => response.withBody(Json.prettyPrint(body)))
        }
    )

}
