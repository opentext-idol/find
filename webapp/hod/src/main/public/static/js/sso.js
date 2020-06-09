/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

/*
 * This is modified copy of the file in the HOD SSO JS which supports redirecting to an optional SAML endpoint and makes
 * an authenticate combined PATCH request to determine if the user is logged in. Dev Console will be updated to support
 * redirecting to SAML, then this can go away.
 *
 * TODO: Generalise and merge this into hod-sso-js (FIND-512)
 */

(function() {
    const CONFIG = JSON.parse(document.getElementById('config-json').textContent);
    const XHR_DONE_STATE = XMLHttpRequest.DONE === undefined ? 4 : XMLHttpRequest.DONE;
    const NO_USER_TOKEN_CODE = 12102;
    const TRUE_STRING = 'true';
    const APPLICATION_ROOT = location.pathname
        .substring(0, location.pathname.lastIndexOf(CONFIG.ssoEntryPage));

    // Parse the given query string into an object of string keys and string values
    function parseQueryString(search) {
        return search === ''
            ? {}
            : search
               // Remove the leading question mark
                   .substring(1)
                   .split('&')
                   .map(function(pairString) {
                       return pairString.split('=').map(decodeURIComponent);
                   })
                   .reduce(function(output, pair) {
                       const key = pair[0];

                       if(!output[key]) {
                           output[key] = pair[1];
                       }

                       return output;
                   }, {});
    }

    // Build a query string from an object of string keys and string values
    function buildQueryString(parameters) {
        return '?' + Object.keys(parameters)
                .map(function(key) {
                    return [key, parameters[key]].map(encodeURIComponent).join('=');
                })
                .join('&');
    }

    // Returns a document fragment containing <input> elements with names and values extracted from the data argument
    function buildFormInputs(data) {
        return Object.keys(data)
            .map(function(key) {
                const input = document.createElement('input');
                input.setAttribute('name', key);
                input.setAttribute('value', data[key]);
                input.setAttribute('type', 'hidden');
                return input;
            })
            .reduce(function(fragment, input) {
                fragment.appendChild(input);
                return fragment;
            }, document.createDocumentFragment());
    }

    function addReadyStateListener(xhr, callback) {
        xhr.addEventListener('readystatechange', function() {
            if(xhr.readyState === XHR_DONE_STATE) {
                let response;

                try {
                    response = JSON.parse(xhr.response);
                } catch(e) {
                    response = null;
                }

                if(xhr.status === 200) {
                    callback(null, response);
                } else {
                    callback({
                        status: xhr.status,
                        response: response
                    });
                }
            }
        });
    }

    // Make the given request to HOD and parse the response as JSON
    function makeSignedRequest(request, callback) {
        const xhr = new XMLHttpRequest();
        xhr.withCredentials = true;
        xhr.open(request.verb, request.url);
        xhr.setRequestHeader('token', request.token);

        addReadyStateListener(xhr, callback);

        if(request.body) {
            xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
            xhr.send(request.body);
        } else {
            xhr.send();
        }
    }

    // Load the authentication error page
    function loadErrorPage() {
        location.assign(APPLICATION_ROOT + CONFIG.errorPage);
    }

    // POST the signature of an authenticate combined PATCH request to Dev Console
    function postToDevConsole() {
        // Set the redirect URL to the application authenticate path since Dev Console will post a combined SSO token back to us
        const redirectUrl = location.protocol + '//' + location.host + APPLICATION_ROOT + CONFIG.authenticatePath;

        // Ask the application to sign our redirect URL
        const xhr = new XMLHttpRequest();
        xhr.open('GET', APPLICATION_ROOT + CONFIG.ssoPatchTokenApi + buildQueryString({'redirect-url': redirectUrl}));

        addReadyStateListener(xhr, function(error, response) {
            if(error) {
                loadErrorPage();
            } else {
                const form = document.createElement('form');
                form.setAttribute('action', CONFIG.ssoPagePostUrl);
                form.setAttribute('method', 'post');

                form.appendChild(buildFormInputs({
                    app_token: response.token,
                    redirect_url: redirectUrl
                }));

                document.body.appendChild(form);
                form.submit();
            }
        });

        xhr.send();
    }

    window.addEventListener('load', function() {
        const form = document.getElementById('authenticate-form');
        form.setAttribute('action', APPLICATION_ROOT + CONFIG.authenticatePath);

        makeSignedRequest(CONFIG.patchRequest, function(error, response) {
            if(error) {
                if(error.response && error.response.error === NO_USER_TOKEN_CODE) {
                    if(parseQueryString(location.search).authenticated === TRUE_STRING) {
                        // Cookie-less SSO redirect
                        postToDevConsole();
                    } else {
                        // Cookied SSO redirect
                        location.assign(
                            CONFIG.ssoPageGetUrl +
                            buildQueryString({
                                redirect_url: location.protocol + '//' + location.host + location.pathname
                            }));
                    }
                } else {
                    loadErrorPage();
                }
            } else {
                form.appendChild(buildFormInputs(response.token));
                form.submit();
            }
        });
    });
})();
