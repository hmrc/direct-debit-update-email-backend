
# direct-debit-update-email-backend

This service is built using Scala (2.13.10) and the Play framework (2.8). It uses linting tools such as WartRemover and
Sclariform.
It also uses the sbt updates plugin - to ensure that compilation fails if dependencies are not up-to-date.
This microservice acts as a state machine, utilising mongodb.
As users progress through the [frontend](https://www.github.com/hmrc/direct-debit-update-email-frontend) journey various
rest endpoints are called to update the state of the users journey.
The service integrates with [direct-debit-backend](https://www.github.com/hmrc/direct-debit-backend) to obtain the 'bounced' status an email associated with a DDINumber.
This project makes use of sbt modules - which act as libraries for our models and anything else we may want to reuse in
the frontend. We have coined the term `cor` for these modules - `collection of routines`

---

## Contents

* [Dictionary](https://www.github.com/hmrc/direct-debit-update-email-backend#dictionary)
* [Current supported tax regimes](https://www.github.com/hmrc/direct-debit-update-email-backend#current-supported-tax-regimes)
* [Running the service locally](https://github.com/hmrc/direct-debit-update-email-backend#running-the-service-locally)
* [Running tests](https://github.com/hmrc/direct-debit-update-email-backend#running-tests)
* [Developing locally](https://github.com/hmrc/direct-debit-update-email-backend#developing-locally)

---

### Dictionary

Acronyms used in a codebase. To speed up writing and reading.

| Phrase    | Description                                                   |
|-----------|---------------------------------------------------------------|
| Sj        | Start Journey                                                 |
| DDINumber | A number used to identify a direct debit                      |
| Bta       | Business Tax Account - client app                             |
| Epaye     | Employers' Pay as you earn - tax type                         |
| PAYE      | Pay as you earn - tax type - often interchangeable with Epaye |
| Bounced   | Can an email be successfully sent to an email                 | 

---

### Current supported tax regimes

The current list of supported tax regimes is:
* paye

Current up-to-date list can be found in [application.conf](https://github.com/hmrc/direct-debit-update-email-backend/blob/main/conf/application.conf) under the key `allowed-tax-regimes`

---

### Running the service locally

You can run the service locally using sbt: `sbt run`

If running locally, the service runs on port `10802`

---

### Running tests

You can run the unit/integration tests locally using sbt: sbt test

To run a specific spec, run `sbt 'testOnly *<SpecName>'`, e.g. `sbt 'testOnly *JourneyControllerSpec'`

---

### Developing locally

If you want to make changes to one of the cors and test those changes locally before raising a pull request, you can
publish your changes locally.

To do this, simply run

```
sbt publishLocal
``` 

to create a snapshot version of backend locally (e.g. `0.3.0-SNAPSHOT`) then use that as the cor version in frontend.

---

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").