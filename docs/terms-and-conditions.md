# Terms & Conditions (T&C)

## Assumptions
* The text for the T&C will be updated rarely.
* A user may use multiple clients to access k33-platform services.

## Objectives

* Version-control the T&C text.
  * Notify user to accept the updated T&C.
* Minimize load on backend
  * Maintain T&C text in Content Management System (CMS).
  * Avoid polling backend to check updated T&C since that is not done frequently.
  * Clients can keep a local cached copy of the last accepted version of T&C by the user.
* Ability to block services if user has not accepted the updated T&C within reasonable grace period.

## Improvements
* Email notification to the users on new version of T&C.
* Push Notification to the App and Web clients to notify update of T&C.

## Use cases

### T&C shown for the first time

1. Client will load the latest T&C, along with its version from metadata, from CMS.
2. Client will locally cache the version of accepted T&C.
3. Client will notify backend server with:
   1. T&C ID
   2. T&C Version
   3. Accept/Reject (boolean)
   4. CMS (Contentful) keys:
      1. Space ID
      2. Entry ID
      3. Field ID

### T&C previously accepted by the user

1. Client will check the latest version of T&C on CMS.
2. Client will try to load the local cache version of accepted T&C.
   1. If the latest version from CMS is higher than the version from the cache, it will load it from backend server updating the cache.
   2. If there is no cached version, it will load it from backend server updating the cache.
3. If the latest version from CMS is higher than the version from backend server, it will prompt user to accept new T&C.

Note: If the latest version of T&C from CMS is same as one in the cache, it will **NOT** poll the backend for latest accepted version.
