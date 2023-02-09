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

import play.api.test.Helpers._
import uk.gov.hmrc.directdebitupdateemailbackend.testsupport.stubs.AuthStub
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.Future

trait CommonBehaviour { this: ItSpec =>

  def authenticatedJourneyEndpointBehaviour[A](getResult: HeaderCarrier => Future[A]): Unit = {

    "must return an 401 (UNAUTHORIZED) if no bearer token is presented" in {
      val result = getResult(HeaderCarrier())

      val exception = intercept[UpstreamErrorResponse](await(result))
      exception.statusCode shouldBe UNAUTHORIZED

      AuthStub.ensureAuthoriseNotCalled()
    }

    "must return an error if no journey can be found the given journey id" in {
      AuthStub.authorise()
      val result = getResult(hcWithAuthorization)

      val exception = intercept[UpstreamErrorResponse](await(result))
      exception.statusCode shouldBe NOT_FOUND
      exception.getMessage() should include("Could not find journey")
      AuthStub.ensureAuthoriseCalled()
    }

  }

}
