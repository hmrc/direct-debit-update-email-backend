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
import ddUpdateEmail.models.{Email, TaxId, TaxRegime}
import play.api.libs.json.{JsError, JsSuccess, Json, Reads}

final case class GetBounceStatusResponse(
    isBounced: Boolean,
    email:     Email,
    taxRegime: TaxRegime,
    taxId:     Option[TaxId]
)

object GetBounceStatusResponse {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit def reads(implicit cryptoFormat: CryptoFormat): Reads[GetBounceStatusResponse] = {
    implicit val taxIdReads: Reads[TaxId] =
      Reads { jsValue =>
        for {
          value <- (jsValue \ "value").validate[String]
          typeString <- (jsValue \ "type").validate[String]
          taxId <- typeString match {
            case "vrn"    => JsSuccess(Vrn(value))
            case "empref" => JsSuccess(EmpRef(value))
            case "zppt"   => JsSuccess(Zppt(value))
            case "zsdl"   => JsSuccess(Zsdl(value))
            case other    => JsError(s"Unrecognised type type '$other'")
          }
        } yield taxId
      }
    Json.reads
  }

}
