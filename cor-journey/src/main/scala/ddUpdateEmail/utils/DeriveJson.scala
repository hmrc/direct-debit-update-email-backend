/*
 * Copyright 2025 HM Revenue & Customs
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

package ddUpdateEmail.utils

import cats.syntax.either.*
import io.circe.Decoder.Result
import io.circe.{Codec, Decoder, DecodingFailure, Encoder, HCursor, Json as CirceJson, JsonObject}
import play.api.libs.json.*

object DeriveJson {

  object Circe {

    given jsValueCanEqual: CanEqual[JsValue, JsValue] =
      CanEqual.derived

    @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
    private def toPlay(json: CirceJson): JsValue = json.fold(
      JsNull,
      JsBoolean(_),
      jsonNumber => JsNumber(jsonNumber.toBigDecimal.getOrElse(sys.error("Could not convert to big decimal"))),
      JsString(_),
      vector => JsArray(vector.map(toPlay)),
      toPlayObject
    )

    private def toPlayObject(jsonObject: JsonObject): JsObject =
      JsObject(
        jsonObject.toMap.map { case (k, v) => k -> toPlay(v) }
      )

    @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
    private def toCirce(j: JsValue): CirceJson = j match {
      case JsNull               => CirceJson.Null
      case JsBoolean(b)         => CirceJson.fromBoolean(b)
      case JsTrue               => CirceJson.True
      case JsFalse              => CirceJson.False
      case JsNumber(value)      => CirceJson.fromBigDecimal(value)
      case JsString(value)      => CirceJson.fromString(value)
      case JsArray(value)       => CirceJson.fromValues(value.map(toCirce))
      case JsObject(underlying) =>
        CirceJson.fromJsonObject(
          JsonObject(underlying.map { case (k, v) => k -> toCirce(v) }.toSeq: _*)
        )
    }

    def format[A](codec: Codec.AsObject[A]): OFormat[A] =
      new OFormat {
        override def writes(o: A): JsObject =
          toPlayObject(codec.encodeObject(o))

        override def reads(json: JsValue): JsResult[A] =
          codec
            .decodeJson(toCirce(json))
            .fold(
              error => JsError(error.toString),
              JsSuccess(_)
            )
      }

    private def encoder[A](format: Format[A]): Encoder[A] = new Encoder[A] {

      override def apply(a: A): CirceJson =
        toCirce(format.writes(a))
    }

    private def decoder[A](format: Format[A]): Decoder[A] = new Decoder[A] {

      override def apply(c: HCursor): Result[A] =
        format
          .reads(toPlay(c.value))
          .asEither
          .leftMap(e => DecodingFailure(s"Could not decode: ${e.toString}", List.empty))
    }

    given codec[A](using format: Format[A]): Codec[A] = Codec.from(decoder(format), encoder(format))

  }
}
