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

import cats.Eq
import cats.syntax.eq.*
import ddUpdateEmail.crypto.CryptoFormat
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

import java.util.Locale

final case class Email(value: SensitiveString) extends AnyVal

object Email {

  given Eq[Email] = {
    def toLowerCaseString(e: Email): String = e.value.decryptedValue.toLowerCase(Locale.UK)
    Eq.instance { case (e1, e2) => toLowerCaseString(e1) === toLowerCaseString(e2) }
  }

  given format(using cryptoFormat: CryptoFormat): Format[Email] = {
    given Format[SensitiveString] =
      ddUpdateEmail.crypto.sensitiveStringFormat(cryptoFormat)
    Json.valueFormat
  }

}
