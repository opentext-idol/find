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
mvn clean verify -DtestConfig.location="/config.json" -Dcom.autonomy.hubUrl="http://localhost:4444/wd/hub" -Dcom.autonomy.abcHostedUrl="http://search.dev.idolondemand.com/searchoptimizer" -Dcom.autonomy.apiKey=YOUR_API_KEY -Dcom.autonomy.applicationType="Hosted" -Dcom.autonomy.browsers="chrome"
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

*TODO: Currently AppPage is an interface, but since every concrete Page IS-A AppElement, surely AppPage IS-A AppElement?
Technically a page HAS-A AppElement, but tests currently use many methods (findElement, getDriver, getText) directly on an AppPage.
Methods would have to be copied over, so IS-A is more convenient?*

## Abstraction
Since some pages are shared between hosted and on-premise, pages should be created via an Abstract Factory:
- factories `HSOElementFactory` and `OPElementFactory` extend `ElementFactory`
- they create objects such as `HSOPromotionsPage` and `OPPromotionsPage` which extend `PromotionsPage`
`ElementFactory` is mainly used for creating `AppPage`s, but the name was chosen to avoid confusion with Selenium's own `PageFactory`.
An `AppElement` on an `AppPage` should generally be constructed via a getter (`Button promotionsCategoryFilterButton()`) in the `AppPage`, rather than using `new` directly in the test.

This has the advantages that:
- tests can be written for both apps by passing around `ElementFactory` and `PromotionsPage` polymorphically
- tests can still be written for a specific app using the explicit `HSOElementFactory` to use the hosted-specific method `getIndexesPage` (when it exists)
- enforces separation of shared and app-specific tests
- `ElementFactory` is an abstract class, so `KeywordsPage` (identical in both) can be concrete (no need for redundant `HSOKeywordsPage`)
- minimises code duplication, e.g. any shared promotions behaviour can be put in `PromotionsPage` instead of the concrete subclasses
- allows further abstraction, e.g. `HSOLoginPage` may be refactored to `HODLoginPage` and used for testing all Haven OnDemand apps
- hiding the constructors means the tests do not have to worry about passing around `WebDriver` to create `AppElement`s
- resilient to change as refactoring when apps diverge becomes simple
Disadvantages:
- more complex structure to understand
- slightly harder to find things (e.g. is `promotionsCategoryFilterButton` in `PromotionsPage` or `HSOPromotionsPage`?)

*TODO: Currently the specific ElementFactory is constructed via a static method*

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
