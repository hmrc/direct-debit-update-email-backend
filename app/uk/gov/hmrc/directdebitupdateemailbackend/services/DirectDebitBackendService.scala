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

package uk.gov.hmrc.directdebitupdateemailbackend.services

import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import ddUpdateEmail.crypto.CryptoFormat
import ddUpdateEmail.crypto.CryptoFormat.NoOpCryptoFormat
import ddUpdateEmail.models.DDINumber
import ddUpdateEmail.utils.Errors
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.directdebitupdateemailbackend.connectors.DirectDebitBackendConnector
import uk.gov.hmrc.directdebitupdateemailbackend.models.GetBounceStatusResponse
import uk.gov.hmrc.directdebitupdateemailbackend.services.DirectDebitBackendService.GetStatusError
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DirectDebitBackendService @Inject() (connector: DirectDebitBackendConnector)(implicit ec: ExecutionContext) {

  implicit val cryptoFormat: CryptoFormat = NoOpCryptoFormat

  def getStatus(ddiNumber: DDINumber)(implicit hc: HeaderCarrier): Future[Option[GetBounceStatusResponse]] =
    connector.getStatus(ddiNumber).map { httpResponse =>
      if (httpResponse.status === OK)
        httpResponse.json
          .validate[GetBounceStatusResponse]
          .fold(
            _ => Errors.throwServerErrorException("Got OK status but could not parse body"),
            Some(_)
          )
      else if (httpResponse.status === NOT_FOUND)
        httpResponse.json
          .validate[GetStatusError]
          .fold(
            _ =>
              Errors.throwServerErrorException(
                s"Got unexpected response http status code: ${httpResponse.status.toString}"
              ),
            error =>
              if (error.code === getStatusNotFoundCode) None
              else
                Errors
                  .throwServerErrorException(s"Received NOT_FOUND with response body with unknown code: ${error.code}")
          )
      else
        throw Errors.throwServerErrorException(
          s"Got unexpected response http status code: ${httpResponse.status.toString}"
        )
    }

  private val getStatusNotFoundCode: String = "NOT_FOUND"

}

object DirectDebitBackendService {

  private final case class GetStatusError(code: String, reason: String)

  private object GetStatusError {
    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    implicit val reads: Reads[GetStatusError] = Json.reads
  }

}
