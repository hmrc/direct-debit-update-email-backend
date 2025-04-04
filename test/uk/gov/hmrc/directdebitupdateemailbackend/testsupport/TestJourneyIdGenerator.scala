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

import ddUpdateEmail.models.journey.JourneyId
import uk.gov.hmrc.directdebitupdateemailbackend.services.JourneyIdGenerator

import java.util.concurrent.atomic.AtomicReference

class TestJourneyIdGenerator(testJourneyIdPrefix: String) extends JourneyIdGenerator {

  private val idIterator: Iterator[JourneyId] =
    LazyList.from(0).map(i => JourneyId(s"$testJourneyIdPrefix${i.toString}")).iterator
  private val nextJourneyIdCached             = new AtomicReference[JourneyId](idIterator.next())

  def readNextJourneyId(): JourneyId = nextJourneyIdCached.get()

  override def nextJourneyId(): JourneyId =
    nextJourneyIdCached.getAndSet(idIterator.next())
}
