# Investment

## User states

| Status         | Description                                            |
|----------------|--------------------------------------------------------|
| NOT_REGISTERED | For new user (no info found in database)               |
| NOT_AUTHORISED | For user **NOT** authorised to see fund information    |
| REGISTERED     | For registered user authorised to see fund information |

## Investor types

* PROFESSIONAL
* ELECTIVE_PROFESSIONAL
* NON_PROFESSIONAL

## Invalid request

```
(Investor type is (PROFESSIONAL or NON_PROFESSIONAL)) and 
  (mandatory field == null or phone number is invalid))
```

### Mandatory fields (for PROFESSIONAL or NON_PROFESSIONAL investor)

* name
* phone number
* country
* fund name

Note: Investor type is mandatory for all cases.

## Business scenarios

| Given state | When event                       | Then Output     | New state      |
|-------------|----------------------------------|-----------------|----------------|
| any         | Invalid request                  | 400 Bad Request | unchanged      |
| any         | Country in denied list           | 403 Forbidden   | NOT_AUTHORISED |
| any         | Incorrect fund name              | 403 Forbidden   | NOT_AUTHORISED |
| any         | NON_PROFESSIONAL                 | 403 Forbidden   | NOT_AUTHORISED |
| any         | PROFESSIONAL or NON_PROFESSIONAL | 200 OK          | REGISTERED     |
