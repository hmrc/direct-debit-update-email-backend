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

import ddUpdateEmail.utils.EnumFormat
import enumeratum.EnumEntry.Lowercase
import enumeratum.{EnumEntry, PlayEnum}
import play.api.libs.json.Format

import scala.collection.immutable

sealed trait TaxRegime extends EnumEntry with Lowercase with Product with Serializable

object TaxRegime extends PlayEnum[TaxRegime] {

  implicit val format: Format[TaxRegime] = EnumFormat(TaxRegime)

  /** Soft Drinks Industry Levy (Sdil)
    */
  case object Zsdl extends TaxRegime

  case object VatC extends TaxRegime

  case object Cds extends TaxRegime

  /** Plastics Packaging Tax (Ppt)
    */
  case object Ppt extends TaxRegime

  /** Pay As You Earn (Paye)
    */
  case object Paye extends TaxRegime

  override val values: immutable.IndexedSeq[TaxRegime] = findValues

}
