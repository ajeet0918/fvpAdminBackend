# Local payment testing

This profile replaces Cashfree with a local payment simulator. It updates the same order
payment fields used by the production flow, but it never calls Cashfree.

## Start the backend

From this repository:

~~~bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
~~~

The local profile enables app.payment.local-simulator.enabled=true and disables Cashfree.
The normal local database is still required.

## Start the website

In fvpwebsite, keep the local API value in .env:

~~~dotenv
VITE_API_BASE_URL=http://localhost:8081/api
~~~

Then run:

~~~bash
npm run dev
~~~

Sign in as a customer, add a delivery address, add a product to the cart, and open checkout.

## Online payment test

1. Select **Pay online now** and place the order.
2. The checkout page shows **Local test mode** instead of opening Cashfree.
3. Choose **Simulate success** or **Simulate failure**.
4. Open **My Account** and confirm the order payment status.

The simulator calls POST /api/customer/me/orders/{orderId}/local-payment and records a
LOCAL-TEST-* payment reference in the order history.

## COD test

1. Select **Cash on delivery** and place the order.
2. In the admin frontend, move the order to **DELIVERED**.
3. Record the collected cash from the admin payment controls.
4. Refresh the customer portal and confirm the payment is **PAID**.
