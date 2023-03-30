# Stripe

## Create `Checkout Session`

Ref: https://stripe.com/docs/api/checkout/sessions/create

### Observation

* Stripe does not prevent same customer to subscribe multiple times to same recurring-payment product.
* Possible to apply discount code, for e.g. for users having emails with certain domains, such as internal employees.

### Parameters

Important parameters

* `customer` (Optional)  
  * If stripe customer exists, we pass its ID.
  * If kept blank, Stripe creates a new customer.  
  * Email address of existing user will be prefilled and is non-editable by end-user, similar to `customer_email`.
  * `default payment method` will be prefilled, and is editable by end-user.
* `customer_email` (Optional)  
  * We set this value so that it becomes none editable for end user. 


Note: Only one of `customer` or `customer_email` can be passed. Passing both values is not allowed.

Input from UI

* `success_url` (Required)      
* `cancel_url` (Optional)

Fixed values

* `mode` = `subscription` 
* `locale` = `auto` 
* `allow_promotion_codes` = true
* `subscription_data.trial_period_days` = 30

Giving free access to internal employees 
* `discounts.coupon` = Stripe Coupon ID loaded from config.
* `payment_method_collection` = `if_required` when applying 100% discount coupon

Not used

* `client_reference_id` (Optional)      
* `currency` (Optional)

Not allowed

* `customer_creation` (Optional) - Not allowed in `subscription` mode.
* `submit_type` (Optional) - Allowed only for `payment` mode.


## Create `Customer Portal Session`

Ref: https://stripe.com/docs/api/customer_portal/sessions/create

### Parameters

* `customer` - Stripe Customer ID
* `return_url`