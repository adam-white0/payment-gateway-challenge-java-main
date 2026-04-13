## Overview
This project is a payment gateway API, implemented using Java Spring Boot, that allows merchants to
process payments, as well as retrieve previous payments.

Incoming payment requests are validated, and then sent to an acquiring bank(simulated in this
project) to be authorised. Following this, they are stored for future lookup, and the appropriate
response is returned.

## Architecture
The payment gateway has been implemented following a layered design:

```
Controller
    ↕
 Service  ↔  Bank client 
    ↕
Repository
```

Each layer has its own responsibility, described as followed:
- **Controller** - exposes endpoints client applications can interact with and handles the incoming
HTTP requests. It performs validation on the input, and formats to appropriate response.
- **Service** - the business logic presides here. It's responsibilities include calling the bank
client for payment authorisation and interacting with the repository layer to manage payments.
  - - **Bank Client** - communicates with an external service, in this case being the acquiring bank
      simulator.
- **Repository** - storing and retrieving previous payments.

### Endpoints
> POST /process-payment

Successful Response:
```
{
    "id": "ddfe997c-3fa3-4cbb-86c4-8cbf515b407f",
    "status": "Authorized",
    "cardNumberLastFour": "***************6789",
    "expiryMonth": 5,
    "expiryYear": 2026,
    "currency": "GBP",
    "amount": 12345
}
```
---
> GET /payment/{id}

Successful Response:
```
{
    "id": "ddfe997c-3fa3-4cbb-86c4-8cbf515b407f",
    "status": "Authorized",
    "cardNumberLastFour": "***************6789",
    "expiryMonth": 5,
    "expiryYear": 2026,
    "currency": "GBP",
    "amount": 12345
}
```

## Assumptions

- `{Expiry month}` / `{Expiry Year}` **must be in the future** for a valid payment.
- There are to be three currencies supported.
- The amount is expected to be in the minor currency unit e.g. £1 would be 100 (100 x 1p)
- POST process-payment endpoint will respond with 200 status when a payment is successfully sent to
the acquiring bank,
meaning the actual payment status (`Authorized`/`Declined`) does not change the HTTP status.
- `'authorization_code'`, included in the response from the acquiring bank client (when a payment is
authorised),
is not used nor returned, however is still stored in the repository.

## Key design decisions
### Validation
- Jakarta Validation was used to validate requests made to `POST /process-payment`. The validation
rules are not complex, and do not require business logic. A benefit of using Jakarta validation is,
a bad request is immediately stopped and throws an exception, as well as containing each field that
has not passed validation.
- Two custom Jakarta validation rules were added:
  - **Expiry Date validation** - Due to a rule having to check against two fields, a custom rule was
  needed. A YearMonth is created from both expiry values, and checked against the current YearMonth
  to ensure a future date has been provided.
  - **Currency validation** - This rule was created to check the `currency` field against supported
  currencies. The supported currencies are stored in an enum, and contains three currencies (USD, 
  GBP, EUR) as stated in the requirements. Storing the currencies in an enum provides an simple way
  to add or modify supported currencies. If they are not one of the three listed currencies,
  validation fails.
  - Although these are custom rules, they are still processed the same way as other Jakarta
  validation rules, meaning the same exception is thrown for other validation errors.

### RestClient over Feign
Feign is a great
option in a microservice environment where there could potentially be calls to many downstream
services. As there is only one call is being made to an external service, `RestClient` was used.
`RestClient` was also favoured as it does not require any additional dependencies and is also
provides easy configuration and control over requests and error handling. 

### Error handling
A `GlobalExceptionHandler` has been used to manage all error responses. In the exception handler,
exceptions can be mapped to specific error responses. Exceptions that have been mapped are:
- `PaymentNotFoundException` - Returns 404, occurs when a previous payment cannot be found with the
provided ID.
- `MethodArgumentNotValidException` - Returns 400, occurs when validation fails when a process
payment request is received. Error message includes invalid fields, which a message indicating
the field constraints.
- `PaymentAuthorisationUnavailableException` - Returns 503, occurs when the acquiring bank is 
unavailable.
- `PaymentAuthorisationInvalidException` - Returns 400, occurs when a request to the acquiring bank 
has failed due to missing fields.

### Testing
- Unit tests have been used for the `PaymentGatewayService` and `BankSimulatorClient` classes, as
well as the custom validators (`CurrencyValidator` and `ExpiryYearMonth`). These have been used as 
it is beneficial to test the logic of these classes individually.
- An Integration test has been created for the `PaymentGatewayController`. Due to there being no
logic in either of the functions, a unit test did not seem necessary. Instead, an integration test
can start the application and test that certain requests will return the expected responses.
  - In some cases, integration test can be created for service layers. When a service layer
  interacts with another service, or a real database is being used, an integration test could be
  useful to test the transactional behaviour. As this is not the case in `PaymentGatewayService`,
  an integration test was not created.

## Run the Application

To start the payment gateway application, run:
`./gradlew bootRun`

The application requires the acquiring bank simulator to be running. To start the bank simulator,
run:
`docker compose up`