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

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, stubFor, urlPathEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping

object InternalAuthStub {

  private val authoriseUrl: String = "/internal-auth/auth"

  def authorise(): StubMapping =
    stubFor(
      post(urlPathEqualTo(authoriseUrl))
        .willReturn(aResponse().withStatus(200).withBody("""{ "retrievals": [] }"""))
    )

  def ensureAuthoriseCalledForInternalAuth(internalAuthResourceLocation: String) =
    verify(
      exactly(1),
      postRequestedFor(urlPathEqualTo(authoriseUrl))
        .withRequestBody(
          equalToJson(
            s"""{
               |  "predicate":  {
               |    "resource": {
               |      "resourceType": "direct-debit-update-email-backend",
               |      "resourceLocation": "$internalAuthResourceLocation"
               |    },
               |    "action": "WRITE"
               |  },
               |  "retrieve": [ ]
               |}
               |""".stripMargin
          )
        )
    )

}
