/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
(function () {
    var XHR_DONE_STATE = 4;
    var HTTP_AUTHENTICATION_ERROR = 401;
    var DEFAULT_HOD_DOMAIN = 'havenondemand.com';

    /**
     * The error code returned by HOD when there is no SSO cookie.
     * @type {number}
     */
    var NO_USER_TOKEN_CODE = 12102;

    /**
     * Build a URL encoded query string from a map of key to value.
     * @param {Object.<string, string>} parameters
     * @return {string}
     */
    function buildQueryString(parameters) {
        return Object.keys(parameters).map(function (name) {
            return encodeURIComponent(name) + '=' + encodeURIComponent(parameters[name]);
        }).join('&');
    }

    /**
     * Add a ready state change listener to an XMLHttpRequest. When the request is done, the callback is called. Assumes
     * a JSON response.
     * @param {XMLHttpRequest} xhr
     * @param {Function} callback Called with an error response and status if there is one or null and the parsed response
     */
    function addReadyStateChangeListener(xhr, callback) {
        xhr.addEventListener('readystatechange', function () {
            if (xhr.readyState === XHR_DONE_STATE) {
                var response;

                try {
                    response = JSON.parse(xhr.response);
                } catch (e) {
                    response = null;
                }

                if (xhr.status === 200) {
                    callback(null, response);
                } else {
                    callback(xhr.status, response);
                }
            }
        });
    }

    /**
     * Make a signed request to HOD, calling the callback when it is done.
     * @param {SignedRequest} request
     * @param {Function} callback
     */
    function makeSignedRequest(request, callback) {
        var xhr = new XMLHttpRequest();
        xhr.withCredentials = true;
        xhr.open(request.verb, request.url);
        xhr.setRequestHeader('token', request.token);

        if (request.body) {
            xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        }

        addReadyStateChangeListener(xhr, callback);

        if (request.body) {
            xhr.send(request.body);
        } else {
            xhr.send();
        }
    }

    /**
     * Get a request to HOD signed by the application backend.
     * @param url
     * @param callback Called with an error if there was one or null and a {@link SignedRequest}.
     */
    function getSignedRequest(url, callback) {
        var xhr = new XMLHttpRequest();
        xhr.open('GET', url);
        addReadyStateChangeListener(xhr, callback);
        xhr.send();
    }

    /**
     * @typedef {Object} AuthenticateCombinedOutput
     * @property {string} domain The application domain
     * @property {string} application The application name
     * @property {string} username
     * @property {Object} combinedToken
     */
    /**
     * Represents a request to make to HOD from the browser. Should be generated on the server using the application
     * unbound token.
     * @typedef {Object} SignedRequest
     * @property {string} url Full URL for the request, including protocol, host, path and query string
     * @property {string} verb HTTP method
     * @property {string} token HMAC signing of the request
     * @property {string} body Request body if there is one, null if not
     */
    /**
     * @typedef {Object} AuthenticateOptions
     * @property {string} applicationRoot Root path of application
     * @property {string} [hodDomain] HOD Domain, defaults to havenondemand.com
     * @property {string} [ssoPage] URL of HOD SSO page, defaults to https://dev.havenondemand.com/sso.html. In case that provided, overrides the hodDomain value for the SSO page redirection.
     * @property {SignedRequest} [listApplicationRequest] A signed request to get a list of applications
     * @property {string} [combinedRequestApi=/api/combined-request] The URI to obtain the signed authentication request from
     * @property {string} [listApplicationRequestApi=/api/list-application-request] The URI to obtain the signed list application request from
     */
    /**
     * Attempt to authenticate the user using SSO. If the user is not signed in, redirects the browser to the SSO page.
     * If the authentication fails, the callback is called with an error. If not, it is called with null and a combined
     * token.
     * @param {Function} callback Called with an error if there was one, or with null and a {@link AuthenticateCombinedOutput}.
     * @param {AuthenticateOptions} options Configuration options
     */
    function authenticate(callback, options) {
        options = options || {};
        var applicationRoot = options.applicationRoot;
        var hodDomain = options.hodDomain || DEFAULT_HOD_DOMAIN;
        var ssoPage = options.ssoPage || 'https://dev.' + hodDomain + '/sso.html';
        var combinedRequestApi = options.combinedRequestApi || '/api/combined-request';
        var listApplicationRequestApi = options.listApplicationRequestApi || '/api/list-application-request';

        function handleHodErrorResponse(error, response, callback) {
            if (response && response.error === NO_USER_TOKEN_CODE) {
                // The user SSO token is (probably) invalid so redirect to SSO
                window.location = ssoPage + '?' + buildQueryString({redirect_url: window.location});
            } else {
                callback(error);
            }
        }

        function authenticate(listApplicationRequest) {
            // Get a list of applications and users which match the authentication
            makeSignedRequest(listApplicationRequest, function (error, response) {
                if (error) {
                    handleHodErrorResponse(error, response, callback);
                } else if (!response.length || !response[0].users.length) { // for a case when there is no user authorized for this application
                    handleHodErrorResponse(HTTP_AUTHENTICATION_ERROR, null, callback);
                } else {
                    // TODO: Allow user to choose application and user store names and domains
                    var application = {
                        description: response[0].description,
                        domain: response[0].domain,
                        domainDescription: response[0].domainDescription,
                        name: response[0].name
                    };

                    var userStore = {
                        domain: response[0].users[0].domain,
                        name: response[0].users[0].userStore,
                        domainDescription: response[0].users[0].domainDescription
                    };

                    var accounts = response[0].users[0].accounts;

                    var combinedTokenParameters = {
                        domain: application.domain,
                        application: application.name,
                        'user-store-domain': userStore.domain,
                        'user-store-name': userStore.name
                    };

                    getSignedRequest(applicationRoot + combinedRequestApi + '?' + buildQueryString(combinedTokenParameters), function (error, combinedRequest) {
                        if (error) {
                            callback(error);
                        } else {
                            // Obtain a combined token
                            makeSignedRequest(combinedRequest, function (error, response) {
                                if (error) {
                                    handleHodErrorResponse(error, response, callback);
                                } else {
                                    var combinedToken = response.token;

                                    callback(null, {
                                        accounts: accounts,
                                        application: application,
                                        userStore: userStore,
                                        combinedToken: combinedToken
                                    });
                                }
                            });
                        }
                    });
                }
            });
        }

        if (options.listApplicationRequest) {
            authenticate(options.listApplicationRequest);
        } else {
            getSignedRequest(applicationRoot + listApplicationRequestApi, function (error, listApplicationRequest) {
                if (error) {
                    callback(error);
                } else {
                    authenticate(listApplicationRequest);
                }
            });
        }
    }

    /**
     * @typedef {Object} LogoutOptions
     * @property {String} combinedToken The combined token string to use to log out
     * @property {String} [hodDomain=havenondemand.com] Domain for Haven OnDemand endpoint
     * @property {String} [hodEndpoint=https://api.havenondemand.com] Haven OnDemand endpoint, overrides the hodDomain
     * if supplied
     * @property {String} [logoutUrl=https://api.havenondemand.com/2/authenticate/combined] URL for the logout request,
     * overrides the hodEndpoint if supplied
     */
    /**
     * Attempt to log the user out using SSO, calling the callback when the process completes.
     * @param {Function} callback Called with an error if there was one or with null and the response object
     * @param {LogoutOptions} options
     */
    function logout(callback, options) {
        var hodDomain = options.hodDomain || DEFAULT_HOD_DOMAIN;
        var hodEndpoint = options.hodEndpoint || 'https://api.' + hodDomain;
        var logoutUrl = options.logoutUrl || hodEndpoint + '/2/authenticate/combined';

        var xhr = new XMLHttpRequest();
        xhr.withCredentials = true;
        xhr.open('DELETE', logoutUrl);
        xhr.setRequestHeader('token', options.combinedToken);
        addReadyStateChangeListener(xhr, callback);
        xhr.send();
    }

    window.havenOnDemandSso = window.havenOnDemandSso || {};
    window.havenOnDemandSso.authenticate = authenticate;
    window.havenOnDemandSso.logout = logout;
})();
