# Squad 3 Billing API contract

Base URL: `http://localhost:8080`; all successful and error bodies use the envelope shown below. Protected routes require `Authorization: Bearer <accessToken>`. Pagination is zero based.

```json
{"success":true,"data":{},"error":null}
```

Errors use HTTP `400` for invalid input/rules, `401` for missing or invalid JWT, `404` for missing records, `409` for persistence conflicts, and `500` for unexpected failures:

```json
{"success":false,"data":null,"error":{"code":"BAD_REQUEST","message":"Choose another primary payment method before removing this one","details":null}}
```

## Authentication

`POST /api/auth/login` (public)

Request: `{"email":"demo@example.com","password":"demo123"}`

Response `200`: `{"success":true,"data":{"accessToken":"<jwt>","tokenType":"Bearer","expiresInSeconds":3600},"error":null}`. Invalid credentials return `401`.

## Plans and subscription

| Method and path | Request | Successful response |
| --- | --- | --- |
| `GET /api/plans?page=0&size=20&sort=price,asc` | — | `200`, `data` is `{items:[Plan],page,size,totalElements,totalPages,sort}` |
| `GET /api/plans/{id}` | — | `200`, `data: Plan` |
| `PUT /api/plans/{id}/activate` | — | `200`, `data: Plan` |
| `PUT /api/plans/{id}/deactivate` | — | `200`, `data: Plan` |
| `GET /api/subscription?customerId=1` | — | `200`, `data: Subscription` |
| `PUT /api/subscription/{id}/plan` | `{"planId":2}` | `200`, `data: Subscription` |

`Plan`: `{id,name,price,userLimit,storageLimitGb,reportsPerMonth,activeDashboardLimit,dataProcessingLimitGb,support,active}`. `Subscription`: `{id,customerId,planId,planName,billingCycle,currentPeriodStart,currentPeriodEnd,amountDue,lastPaymentAt}`. IDs and dates are JSON numbers and ISO dates; `billingCycle` is `MONTHLY` or `ANNUAL`.

## Billing screen data

| Method and path | Request | Successful response |
| --- | --- | --- |
| `GET /api/usage?customerId=1` | — | `200`, `data: {customerId,planName,metrics:[{metric,used,limit,percentageUsed}]}` |
| `GET /api/invoices?customerId=1&page=0&size=20&status=Due&sort=periodStart,desc` | — | `200`, `data: {items:[{id,periodStart,periodEnd,amount,status,paidOn}],page,size,totalElements,totalPages,sort}` |
| `GET /api/invoices/summary?customerId=1` | — | Billing summary: `200`, `data: {totalPaid,totalInvoices,outstandingAmount}` |
| `GET /api/invoices/monthly-totals?customerId=1` | — | `200`, `data: [{month:"2026-10",total:9.99}]` (six calendar months) |
| `GET /api/payment-methods?customerId=1` | — | `200`, `data: [{id,customerId,brand,lastFour,expiryMonth,expiryYear,primary}]` |
| `POST /api/payment-methods?customerId=1` | `{brand:"Visa",lastFour:"4242",expiryMonth:12,expiryYear:2028,makePrimary:false}` | `201`, `data: PaymentMethod` |
| `PUT /api/payment-methods/{id}/primary?customerId=1` | — | `200`, `data: PaymentMethod` |
| `DELETE /api/payment-methods/{id}?customerId=1` | — | `200`, `data: null` |

Invoice status accepts `Paid` or `Due`; page is zero-based, size is 1–100. Sort fields are `periodStart`, `periodEnd`, `amount`, `status`, `paidOn`, with `asc` or `desc`. Payment creation accepts only brand, last four digits, and expiry (plus `makePrimary`); full card number and CVV are not accepted. The first method becomes primary automatically. Removing a primary method returns `400` with the server rule message; select another primary first.

## Local integration

The API permits browser requests from `http://localhost:5173` for `/api/**`, including preflight requests. Swagger UI is at `/swagger-ui.html`; OpenAPI JSON is at `/v3/api-docs`.
