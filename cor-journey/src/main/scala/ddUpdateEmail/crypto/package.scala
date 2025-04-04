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

package ddUpdateEmail

import play.api.libs.json._
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.crypto.json.JsonEncryption

package object crypto {

  val noOpSensitiveStringFormat: Format[SensitiveString] = Format(
    Reads {
      case JsString(s) => JsSuccess(SensitiveString(s))
      case other       => JsError(s"Expected JsString but got ${other.getClass.getSimpleName}")
    },
    Writes(s => JsString(s.decryptedValue))
  )

  def sensitiveStringFormat(cryptoFormat: CryptoFormat): Format[SensitiveString] = cryptoFormat match {
    case CryptoFormat.OperationalCryptoFormat(crypto) =>
      JsonEncryption.sensitiveEncrypterDecrypter(SensitiveString.apply)(using summon[Format[String]], crypto)

    case CryptoFormat.NoOpCryptoFormat =>
      noOpSensitiveStringFormat
  }

}
