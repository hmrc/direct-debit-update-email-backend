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

package uk.gov.hmrc.directdebitupdateemailbackend.controllers

import cats.syntax.eq._
import com.google.inject.Inject
import ddUpdateEmail.crypto.CryptoFormat.OperationalCryptoFormat
import ddUpdateEmail.models.StartEmailVerificationJourneyResult
import ddUpdateEmail.models.journey.{Journey, JourneyId}
import ddUpdateEmail.utils.Errors
import io.scalaland.chimney.dsl.TransformationOps
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.directdebitupdateemailbackend.actions.Actions
import uk.gov.hmrc.directdebitupdateemailbackend.services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class UpdateStartVerificationJourneyResultController @Inject() (
    actions:        Actions,
    journeyService: JourneyService,
    cc:             ControllerComponents
)(implicit ec: ExecutionContext, cryptoFormat: OperationalCryptoFormat) extends BackendController(cc) {

  def updateStartVerificationJourneyResult(journeyId: JourneyId): Action[StartEmailVerificationJourneyResult] =
    actions.authenticatedAction.async(parse.json[StartEmailVerificationJourneyResult]){ implicit request =>
      for {
        journey <- journeyService.get(journeyId)
        newJourney <- journey match {
          case _: Journey.BeforeSelectedEmail =>
            Errors.throwBadRequestException("Cannot update start verification journey result when email has not been selected")

          case j: Journey.SelectedEmail =>
            updateJourneyWithNewValue(j, request.body)

          case j: Journey.AfterEmailVerificationJourneyStarted =>
            updateJourney(j, request.body)
        }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(journey: Journey.SelectedEmail, result: StartEmailVerificationJourneyResult): Future[Journey] = {
    val newJourney: Journey = journey.into[Journey.EmailVerificationJourneyStarted]
      .withFieldConst(_.startEmailVerificationJourneyResult, result)
      .transform

    journeyService.upsert(newJourney)
  }

  private def updateJourney(
      journey: Journey.AfterEmailVerificationJourneyStarted,
      result:  StartEmailVerificationJourneyResult
  ): Future[Journey] = {
    if (journey.startEmailVerificationJourneyResult === result) {
      Future.successful(journey)
    } else {
      val newJourney = journey match {
        case j: Journey.EmailVerificationJourneyStarted =>
          j.copy(startEmailVerificationJourneyResult = result)

        case j: Journey.ObtainedEmailVerificationResult =>
          j.into[Journey.EmailVerificationJourneyStarted]
            .withFieldConst(_.startEmailVerificationJourneyResult, result)
            .transform
      }
      journeyService.upsert(newJourney)
    }

  }

}

