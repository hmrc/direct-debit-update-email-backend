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
import enumeratum.{Enum, EnumEntry}
import play.api.libs.json.Format

import scala.collection.immutable

sealed trait Origin extends EnumEntry, Product, Serializable derives CanEqual

object Origin extends Enum[Origin] {

  given Format[Origin] = EnumFormat(Origin)

  case object BTA extends Origin

  case object EpayeService extends Origin

  override def values: immutable.IndexedSeq[Origin] = findValues

}
