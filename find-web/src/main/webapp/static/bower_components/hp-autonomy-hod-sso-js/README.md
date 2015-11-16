# HP Haven OnDemand SSO Javascript
Javascript library for working with HP Haven OnDemand SSO

## Usage
The library can be installed using bower:

    bower install hp-autonomy-hod-sso-js

The library contains the following files:

* src/js/authenticate-combined.js exposes a property named havenOnDemandSso on the window, which has methods for authenticating
and logging out of HOD SSO
* src/js/sso.js posts the token returned from havenOnDemandSso.authenticate to your server by generating and submitting a
form
* src/js/logout-sso.js reads a combined token from a request parameter and uses the havenOnDemand.logout method to log
the user out of HOD SSO

You may prefer to use your own equivalent of sso.js and logout-sso.js depending on your application.

# Is it any good?
Yes

## License
Copyright 2015 Hewlett-Packard Development Company, L.P.

Licensed under the MIT License (the "License"); you may not use this project except in compliance with the License.
