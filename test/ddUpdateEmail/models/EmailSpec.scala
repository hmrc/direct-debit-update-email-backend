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

package ddUpdateEmail.models

import cats.syntax.eq._

import org.scalatest.freespec.AnyFreeSpecLike
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.directdebitupdateemailbackend.testsupport.RichMatchers

class EmailSpec extends AnyFreeSpecLike with RichMatchers {

  "Email" - {

    "must have an Eq instance which" - {

      "equates email addresses that the same" in {
        Email(SensitiveString("email")) eqv Email(SensitiveString("email")) shouldBe true
        Email(SensitiveString("email")) eqv Email(SensitiveString("email2")) shouldBe false
      }

      "is case-insensitive" in {
        Email(SensitiveString("email")) eqv Email(SensitiveString("eMaIl")) shouldBe true
      }

    }

  }

}
