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

import com.google.inject.Inject
import ddUpdateEmail.crypto.CryptoFormat.OperationalCryptoFormat
import ddUpdateEmail.models.Email
import ddUpdateEmail.models.journey.{Journey, JourneyId}
import io.scalaland.chimney.dsl.into
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.directdebitupdateemailbackend.actions.Actions
import uk.gov.hmrc.directdebitupdateemailbackend.services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class UpdateSelectedEmailController @Inject() (
  actions:        Actions,
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using ExecutionContext, OperationalCryptoFormat)
    extends BackendController(cc) {

  def updateSelectedEmail(journeyId: JourneyId): Action[Email] =
    actions.authenticatedAction.async(parse.json[Email]) { implicit request =>
      for {
        journey    <- journeyService.get(journeyId)
        newJourney <- journey match {
                        case j: Journey.BeforeSelectedEmail => updateJourneyWithNewValue(j, request.body)
                        case j: Journey.AfterSelectedEmail  => updateJourneyWithExistingValue(j, request.body)
                      }
      } yield Ok(newJourney.json)
    }

  private def updateJourneyWithNewValue(journey: Journey.BeforeSelectedEmail, selectedEmail: Email): Future[Journey] = {
    @SuppressWarnings(Array("org.wartremover.warts.SeqApply"))
    val newJourney: Journey = journey match {
      case j: Journey.Started =>
        j.into[Journey.SelectedEmail]
          .withFieldConst(_.selectedEmail, selectedEmail)
          .transform
    }

    journeyService.upsert(newJourney)
  }

  private def updateJourneyWithExistingValue(
    journey:       Journey.AfterSelectedEmail,
    selectedEmail: Email
  ): Future[Journey] = {
    // don't check to see if email is same to allow for passcodes to be requested again for same email
    @SuppressWarnings(Array("org.wartremover.warts.SeqApply"))
    val newJourney = journey match {
      case j: Journey.SelectedEmail                   =>
        j.copy(selectedEmail = selectedEmail)
      case j: Journey.EmailVerificationJourneyStarted =>
        j.into[Journey.SelectedEmail]
          .withFieldConst(_.selectedEmail, selectedEmail)
          .transform
      case j: Journey.ObtainedEmailVerificationResult =>
        j.into[Journey.SelectedEmail]
          .withFieldConst(_.selectedEmail, selectedEmail)
          .transform
    }

    journeyService.upsert(newJourney)
  }

}
