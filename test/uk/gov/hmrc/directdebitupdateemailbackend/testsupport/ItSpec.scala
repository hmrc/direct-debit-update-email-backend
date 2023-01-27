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

package uk.gov.hmrc.directdebitupdateemailbackend.testsupport

import akka.util.Timeout
import com.google.inject.{AbstractModule, Provides}
import ddUpdateEmail.crypto.CryptoFormat.OperationalCryptoFormat
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.test.Helpers._
import play.api.test.{DefaultTestServerFactory, RunningServer}
import play.api.{Application, Mode}
import play.core.server.ServerConfig
import uk.gov.hmrc.crypto.{AesCrypto, Decrypter, Encrypter, PlainText}
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.directdebitupdateemailbackend.repositories.JourneyRepo
import uk.gov.hmrc.directdebitupdateemailbackend.services.JourneyIdGenerator
import uk.gov.hmrc.http.HeaderCarrier

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDateTime, ZoneId, ZonedDateTime}
import javax.inject.Singleton
import scala.annotation.nowarn
import scala.concurrent.duration._
import scala.util.Random

trait ItSpec
  extends AnyFreeSpecLike
  with RichMatchers
  with GuiceOneServerPerSuite
  with WireMockSupport { self =>

  override def beforeEach(): Unit = {
    super.beforeEach()

    await(app.injector.instanceOf[JourneyRepo].collection.drop().toFuture())(Timeout(10.seconds))
    ()
  }

  implicit val testCrypto: Encrypter with Decrypter = new AesCrypto {
    override protected val encryptionKey: String = "P5xsJ9Nt+quxGZzB4DeLfw=="
  }

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout  = scaled(Span(20, Seconds)),
    interval = scaled(Span(300, Millis))
  )

  lazy val frozenZonedDateTime: ZonedDateTime = {
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    //the frozen time has to be in future otherwise the journeys will disappear from mongodb because of expiry index
    LocalDateTime.parse("2057-11-02T16:28:55.185", formatter).atZone(ZoneId.of("Europe/London"))
  }

  val clock: Clock = Clock.fixed(frozenZonedDateTime.toInstant, ZoneId.of("UTC"))

  lazy val overridingsModule: AbstractModule = new AbstractModule {
    override def configure(): Unit = ()

    @Provides
    @Singleton
    @nowarn // silence "method never used" warning
    def operationalCryptoFormat: OperationalCryptoFormat = OperationalCryptoFormat(testCrypto)

    @Provides
    @Singleton
    @nowarn // silence "method never used" warning
    def clock: Clock = self.clock

    /**
     * This one is randomised every time new test application is spawned. Thanks to that there will be no
     * collisions in database when 2 tests insert journey.
     */
    @Provides
    @Singleton
    @nowarn // silence "method never used" warning
    def journeyIdGenerator(testJourneyIdGenerator: TestJourneyIdGenerator): JourneyIdGenerator = testJourneyIdGenerator

    @Provides
    @Singleton
    @nowarn // silence "method never used" warning
    def testJourneyIdGenerator(): TestJourneyIdGenerator = {
      val randomPart: String = Random.alphanumeric.take(5).mkString
      val journeyIdPrefix: String = s"TestJourneyId-$randomPart-"
      new TestJourneyIdGenerator(journeyIdPrefix)
    }

  }

  def journeyIdGenerator: TestJourneyIdGenerator = app.injector.instanceOf[TestJourneyIdGenerator]

  implicit def hc: HeaderCarrier = HeaderCarrier()

  val testServerPort: Int = 19001
  val baseUrl: String = s"http://localhost:${testServerPort.toString}"
  val databaseName: String = "direct-debit-update-email-backend-it"

  def conf: Map[String, Any] = Map(
    "mongodb.uri" -> s"mongodb://localhost:27017/$databaseName",
    "microservice.services.direct-debit-update-email-backend.protocol" -> "http",
    "microservice.services.direct-debit-update-email-backend.host" -> "localhost",
    "microservice.services.direct-debit-update-email-backend.port" -> testServerPort,
    "microservice.services.direct-debit-backend.port" -> WireMockSupport.port,
    "microservice.services.internal-auth.port" -> WireMockSupport.port,
    "auditing.consumer.baseUri.port" -> WireMockSupport.port,
    "auditing.enabled" -> false,
    "auditing.traceRequests" -> false
  )

  //in tests use `app`
  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(conf)
    .overrides(GuiceableModule.fromGuiceModules(Seq(overridingsModule)))
    .build()

  object TestServerFactory extends DefaultTestServerFactory {
    override protected def serverConfig(app: Application): ServerConfig = {
      val sc = ServerConfig(port    = Some(testServerPort), sslPort = Some(0), mode = Mode.Test, rootDir = app.path)
      sc.copy(configuration = sc.configuration.withFallback(overrideServerConfiguration(app)))
    }
  }

  override implicit protected lazy val runningServer: RunningServer =
    TestServerFactory.start(app)

  def encryptString(s: String, encrypter: Encrypter): String =
    encrypter.encrypt(
      PlainText("\"" + SensitiveString(s).decryptedValue + "\"")
    ).value

}

