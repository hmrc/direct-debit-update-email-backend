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
import ddUpdateEmail.models.journey.{Journey, SessionId}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.directdebitupdateemailbackend.services.JourneyService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

class JourneyController @Inject() (
  journeyService: JourneyService,
  cc:             ControllerComponents
)(using OperationalCryptoFormat, ExecutionContext)
    extends BackendController(cc) {

  val findLatestJourneyBySessionId: Action[AnyContent] = Action.async { implicit request =>
    val sessionId: SessionId =
      summon[HeaderCarrier].sessionId
        .map(x => SessionId(x.value))
        .getOrElse(throw new RuntimeException("Missing required 'SessionId'"))

    journeyService.findLatestJourney(sessionId).map {
      case Some(journey: Journey) => Ok(Json.toJson(journey))
      case None                   => NotFound(s"sessionId:${sessionId.toString}")
    }
  }

}
