# Register for exchange of information frontend

This service provides a UI that allows users to register so that they can report under mandatory disclosure rules.

### Overview:

Users are able to register as an `Organisation` or `Individual`. The service follows two main flows consisting of a `with ID` or `without ID` journey. 

An `Organisation with ID` will use their `Unique Taxpayer Reference & name`  to perform a business match.
An `Individual with ID` will use their `NINO, name & date of birth` to perform a business match.

If a business match is successful we check for a subscription. If a subscription exists we try to create an enrolment, if an enrolment already exists users will be presented with a kick out page notifying them that they're already registered. If a subscription does not exist we collect contact details to then create a subscription and then an enrolment. 

Both `Organisation without ID & Individual without ID` will need to provide additional details before being asked to provide contact details to create subscription and enrolment. 

This service interacts with [Tax Enrolments](https://github.com/hmrc/tax-enrolments), [Email Service](https://github.com/hmrc/email) & [Register for exchange of information](https://github.com/hmrc/register-for-exchange-of-information).

### API Calls:
| PATH | Supported Methods | Description |
|------|-------------------|-------------|
|```/enrolment-store-proxy/enrolment-store/enrolments/HMRC-MDR-ORG~MDRID~{subscriptionID}/groups``` | GET | Retrieves enrolment status - if 204 returned we take subscription info and enrol user |
|```/tax-enrolments/service/HMRC-MDR-ORG/enrolment``` | PUT | Creates tax enrolment |
|```/register-for-exchange-of-information/subscription/read-subscription``` | POST | Reads subscription and returns subscriptionID |
|```/register-for-exchange-of-information/subscription/create-subscription``` | POST | Creates subscription and returns subscriptionID |
|```/register-for-exchange-of-information/registration/organisation/utr``` | POST | Sends registration for Organisation with ID |
|```/register-for-exchange-of-information/registration/individual/nino``` | POST | Sends registration for Individual with ID |
|```/register-for-exchange-of-information/registration/organisation/noId``` | POST | Sends registration for Organisation without ID |
|```/register-for-exchange-of-information/registration/individual/noId``` | POST | Sends registration for Individual without ID |
|```/hmrc/email``` | POST | Sends email to user on successful registration |

***API Specs:***
- [Register without ID](https://confluence.tools.tax.service.gov.uk/display/DAC6/MDR+Specs?preview=/388662598/434373860/AEOI-DCT70a-1.10-EISAPISpecification-MDRCustomerRegistrationWithoutIdentifiertoETMP.pdf)
- [Register with ID](https://confluence.tools.tax.service.gov.uk/display/DAC6/MDR+Specs?preview=/388662598/434373864/AEOI-DCT70b-1.10-EISAPISpecification-MDRCustomerRegistrationWithIdentifiertoETMP.pdf)
- [Create subscription](https://confluence.tools.tax.service.gov.uk/display/DAC6/MDR+Specs?preview=/388662598/434373868/AEOI-DCT70c-1.10-EISAPISpecification-MDRCustomerSubscriptionCreate.pdf)

## Run Locally

This service runs on port 10015 and is named REGISTER_FOR_EXCHANGE_OF_INFORMATION_FRONTEND in service manager.

Run the following command to start services locally:

    sm --start MDR_ALL -r

#### *Auth login details*: 

      enrolmentKey = "N/A"  
      identifier = "N/A"  
      identifier value = "N/A"
      redirect url = "/register-for-mdr"

#### *Acceptance test repo*:  
[register-for-exchange-of-information-ui-tests](https://github.com/hmrc/register-for-exchange-of-information-ui-tests)



## Requirements

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), and requires a Java 8 [JRE] to run.

[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

