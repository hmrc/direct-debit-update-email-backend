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

import com.google.inject.{Inject, Singleton}
import ddUpdateEmail.models.Origin
import ddUpdateEmail.models.journey.{SjRequest, SjResponse}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, Request, Result}
import uk.gov.hmrc.directdebitupdateemailbackend.models.StartResult
import uk.gov.hmrc.directdebitupdateemailbackend.services.StartService
import uk.gov.hmrc.internalauth.client.{BackendAuthComponents, IAAction, Predicate, Resource, ResourceLocation, ResourceType}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SjController @Inject() (
    startService: StartService,
    auth:         BackendAuthComponents,
    cc:           ControllerComponents
)(implicit ex: ExecutionContext) extends BackendController(cc) {

  private def internalAuthPermission(origin: String): Predicate.Permission = Predicate.Permission(
    resource = Resource(
      resourceType     = ResourceType("direct-debit-update-email-backend"),
      resourceLocation = ResourceLocation(s"direct-debit-update-email/$origin/start")
    ),
    action   = IAAction("READ")
  )

  private val btaPermission: Predicate.Permission = internalAuthPermission("bta")
  private val payePermission: Predicate.Permission = internalAuthPermission("epaye")

  val startBta: Action[JsValue] = auth.authorizedAction(btaPermission).compose(Action(parse.json)).async { implicit request =>
    doStart(Origin.BTA, request.body)
  }

  val startEpaye: Action[JsValue] = auth.authorizedAction(payePermission).compose(Action(parse.json)).async { implicit request =>
    doStart(Origin.EpayeService, request.body)
  }

  private def doStart(origin: Origin, requestJson: JsValue)(implicit request: Request[_]): Future[Result] =
    requestJson.validate[SjRequest] match {
      case JsSuccess(sjRequest, _) =>
        startService.start(origin, sjRequest).map {
          case StartResult.NoSessionId         => BadRequest(noSessionIdErrorBody)
          case StartResult.NoDirectDebitFound  => NotFound(noDirectDebitFoundErrorBody)
          case StartResult.TaxRegimeNotAllowed => Forbidden(taxRegimeNotAllowedErrorBody)
          case StartResult.IsNotBounced        => Conflict(emailNotBouncedErrorBody)
          case StartResult.Started(nextUrl)    => Created(Json.toJson(SjResponse.Success(nextUrl)))
        }
      case JsError(_) =>
        BadRequest(jsonCannotBeParsedErrorBody)
    }

  private val noSessionIdErrorBody = Json.toJson(SjResponse.Error(BAD_REQUEST, "session ID not found"))
  private val noDirectDebitFoundErrorBody = Json.toJson(SjResponse.Error(NOT_FOUND, "direct debit not found"))
  private val taxRegimeNotAllowedErrorBody = Json.toJson(SjResponse.Error(FORBIDDEN, "tax regime not allowed"))
  private val emailNotBouncedErrorBody = Json.toJson(SjResponse.Error(CONFLICT, "email not bounced"))
  private val jsonCannotBeParsedErrorBody = Json.toJson(SjResponse.Error(BAD_REQUEST, "request body cannot be parsed"))

}
