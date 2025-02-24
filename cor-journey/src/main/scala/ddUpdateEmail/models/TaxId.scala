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

import io.circe.generic.semiauto.deriveCodec
import ddUpdateEmail.utils.DeriveJson
import play.api.libs.json.OFormat

sealed trait TaxId extends Product, Serializable {

  val value: String

}

object TaxId {

  final case class EmpRef(value: String) extends TaxId

  final case class Vrn(value: String) extends TaxId

  final case class Zppt(value: String) extends TaxId

  final case class Zsdl(value: String) extends TaxId

  given OFormat[TaxId] = DeriveJson.Circe.format(deriveCodec[TaxId])

}
