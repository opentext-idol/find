# Haven Search OnDemand QA Framework
*Correct as of 4th Sep 2015, but currently quite volatile*

## Overview
Tests are written using the JUnit framework, and built using Maven.
This allows the full test suite to be run automatically by Jenkins.
The build is parameterized by
- Application type ("Hosted" or "On Premise")
-	Selenium hub URL (hub is run locally or on a VM)
-	Application URL
-	Browser (currently "firefox", "internet explorer" or "chrome", but adding
new browsers only requires setting up a suitable Selenium server)

## Maven
Maven is configured with the Failsafe plugin - this will run all test classes in the `integration-tests` package that end in `ITCase.java`.
It is possible to add custom JUnit `RunListener`s, or limit the tests run using `Category`s just by modifying the pom.xml.
However, the IntelliJ test runner does not read this information from the POM, so currently `TestWatcher`s are used instead of `RunListener`s.

*TODO: Categories will be used to mark tests only relevant for one product? Will need access to a specific subclass of ElementFactory*

## Selenium Server
A Selenium server can be configured to run locally by installing the Selenium server jar,
along with any WebDriver executables.

Before running a test locally, start up the Selenium server from command line with e.g.

`java -Dwebdriver.chrome.driver=PATH_TO_chromedriver.exe -Dwebdriver.ie.driver=PATH_TO_IEDriverServer.exe -jar PATH_TO_selenium-server-standalone.jar`

By default this starts on  http://localhost:4444/wd/hub. This should be used as `com.autonomy.hubUrl` when running a test.

## Running Tests
To run all tests from command line:
- Start the Selenium server as outlined above
- Run the following in cmd (but with your own API key)
```sh
mvn clean verify -DtestConfig.location="config.json" -Dcom.autonomy.hubUrl="http://localhost:4444/wd/hub" -Dcom.autonomy.abcHostedUrl="http://search.dev.idolondemand.com/searchoptimizer" -Dcom.autonomy.apiKey=YOUR_API_KEY -Dcom.autonomy.applicationType="Hosted" -Dcom.autonomy.browsers="chrome"
```
*TODO: config.json is only used for on-premise, should be removed at some point*

## Mock UI
The framework is designed to be shared as much as possible between Hosted and On Premise.
The mock UI is kept separate from the tests - tests depend on mock UI but UI should not have any dependency on the test framework.

There are two bases to extend from
-	`AppElement`: a wrapper around the Selenium `WebElement` interface providing some additional convenience methods.
Represents an element on the page. The underlying `WebDriver` can be accessed using `getDriver()`.
Specific elements should be constructed as `AppElement`s, but passed around as `WebElement`s.
-	`AppPage`: represents a page, subclasses must implement `waitforLoad()` - this will be called in the constructor.
It should involve some kind of `WebDriverWait` for something that only appears on that specific page.
This ensures the page is fully loaded, so that `findElement` does not fail, and to minimise the risk of `StaleElementException`s.
In practice, it is often necessary to wait *before* calling `new`, so many `AppPage`s instead have a static factory method `make`.

There are classes representing the objects in the application, e.g. `Promotion` and `User`.
There are also `Service`s in the mock UI, which are facades for performing complicated actions that may navigate through several pages, e.g. `PromotionService.setUpPromotion`. `SearchActionFactory` is currently an exception to this rule - performing a search and waiting for the results to load before applying each filter is cumbersome.

*TODO: should search filters instead be decorators around a search object? i.e. new LanguageFilterSearch("English", new Search(...))*

For elements on a page, there are several utility classes such as `FormInput` or `Checkbox` that represent common bootstrap elements and avoid creating repetitive (and therefore fragile) getters/setters for similar elements. It also means that if the behaviour of an element changes (e.g. from a checkbox to a numeric input) the test will need to be rewritten - *this is a good thing!*

## Abstraction
Since some pages are shared between hosted and on-premise, pages should be created via an Abstract Factory:
- factories `HSOElementFactory` and `OPElementFactory` extend `ElementFactory`
- they create objects such as `HSOPromotionsPage` and `OPPromotionsPage` which extend `PromotionsPage`
`ElementFactory` is mainly used for creating `AppPage`s, but the name was chosen to avoid confusion with Selenium's own `PageFactory`.
An `AppElement` on an `AppPage` should generally be constructed via a getter (`Button promotionsCategoryFilterButton()`) in the `AppPage`, rather than using `new` directly in the test.

This has the advantages that:
- tests can be written for both apps by passing around `ElementFactory` and `PromotionsPage` polymorphically
- tests can still be written for a specific app using the explicit `HSOElementFactory` to use the hosted-specific method `getIndexesPage`
- enforces separation of shared and app-specific tests
- `ElementFactory` is an abstract class, so `KeywordsPage` (identical in both) can be concrete (no need for redundant `HSOKeywordsPage`)
- minimises code duplication, e.g. any shared promotions behaviour can be put in `PromotionsPage` instead of the concrete subclasses
- allows further abstraction, e.g. `HSOLoginPage` may be refactored to `HODLoginPage` and used for testing all Haven OnDemand apps
- hiding the constructors means the tests do not have to worry about passing around `WebDriver` to create `AppElement`s
- resilient to change as refactoring when apps diverge becomes simple
Disadvantages:
- more complex structure to understand
- slightly harder to find things (e.g. is `promotionsCategoryFilterButton` in `PromotionsPage` or `HSOPromotionsPage`?)

As of 20/10/2015 the on-premise specific parts have been removed from git, the only remaining overlap is `ApplicationType`, intentionally left behind so that tests can do an in-line check of the application type for simple assertions (e.g. check if a button exists on hosted, but not on on-premise).

## Naming Conventions
There are several naming conventions used - most are not there for any specific reason, but consistent naming structure helps readability.
- Any class that is only relevant to on-premise is prefixed with `OP`, similarly `HSO` is used for hosted. Anything that applies specifically to the SAAS half (due to slight inconsistencies between halves) is prefixed with `SAAS`.
- "get" should be omitted when returning an element (`WebElement`, `Checkbox` etc.), as in `KeywordsPage.createNewKeywordsButton()`. Other getters that return a calculated value (such as a `String` or `int`) should include "get", as in `PromotionsPage.getPromotionTitles()`. This convention avoids possible confusion with return types without complicated method names (otherwise would need `getPromotionTitleStrings` to avoid confusion with `getPromotionTitleWebElements`)
- Factory methods start with `make` or `create`. Since the word "create" is used in the app itself (as in "Create new promotion"), `make` is usually preferred to avoid confusion.
- The exception to the previous rule is `ElementFactory`, which uses `get` (`elementFactory.getKeywordsPage`) for backwards compatibility (it used to cache pages for speed, but this leads to `StaleElementException`s on hosted).
- When constructing an `AppPage`, it is often necessary to wait *before* calling `new`, so many `AppPage`s instead have a static factory method `make(WebDriver)`. Typically `make` and `waitForLoad` both call the static method `Page.waitForLoad(WebDriver)`.
- Test *files* end in `ITCase.java` in order to be recognised by the plugin that runs the tests. Test *methods* start with `test` but this is purely convention.

## JUnit Test Framework
Since IntelliJ and Failsafe both rely on built-in JUnit runners, we do not have a custom test runner.
The two test runners are not particularly compatible when it comes to adding `RunListener`s.

However similar results can be obtained using `Rule`s (especially `TestWatcher`s).
There are already rules in place to save useful information when a test fails: a screenshot is taken and the HTML of the current DOM tree saved.
New Rules can be added and may benefit from the slightly hacky `TestState` singleton, which contains information such as the current test class and method name.

These rules are declared in `ABCTestBase`.
All test classes should inherit from this class (either directly, or indirectly via not-yet-written `HSOTestBase` and `OPTestBase` for app-specific tests).
`ABCTestBase` also performs other important set-up/tear-down such as creating the driver object and interpreting the command-line parameters.

Test assertions should use Hamcrest matchers wherever possible: this makes output far more readable.
If instead using a boolean assertion, the description should **describe the expected behaviour, not the reason for failing**.

The Hamcrest `assertThat` methods have been overwritten internally - make sure to static import the correct one.
This overwrite allows better handling of assertion failures.
The same class also contains `verifyThat` methods - these will allow a test to continue if the assertion fails.

## Writing Tests
A test class contains all the integration tests for a specific page, although there is no specific reason for this.
Test methods vary in length: some are very short - just checking the page loads with the correct information, others
are longer and test a full flow to mimic a real user experience.

Assertions within tests should be `verifyThat` unless the assertion is critical to the state of the test.
For example, *verify* that there are 5 promotions in the promotions list, but *assert* that the promotion you wish to click on is in the list.

Beware of `StaleElementException`! Every time you navigate to a new page, any previous `AppPage` objects will now be stale.
`AppBody` will go stale when switching between Angular/Backbone sides of the hosted app.

A test should very rarely need to call `findElement` directly, as it leads to fragile tests. Even if it is only used once, it likely belongs in the mock UI.
A mock UI page should *never* need to construct another page or access the nav bars, this likely belongs in a helper object (`Service`) instead.
