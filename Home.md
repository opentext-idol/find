# What is Find?

Find is an end-user search interface for [HPE IDOL](http://www8.hp.com/uk/en/software-solutions/information-data-analytics-idol/index.html) and [Haven OnDemand](https://www.havenondemand.com)

# Guide

## Compiling and Running Find

### Basics

- [[Building Find]] - how to get the code and compile a copy of Find
- [[Running a Development Copy of Find]] - how to test your changes
- [[Running a Production Copy of Find]] - how to deploy your changed version of Find

### Advanced
- [[Heroku Deployment Guide]] - how to run Find on Heroku, if that's something you want to do
- [[Understanding the Code Structure]] - info about what's going on under the surface
- [[Vagrant]] - how to set up the Find backend virtual machine with Vagrant
- [[JetBrains IntelliJ IDEA]] - setting up the IntelliJ IDE to run Find
- [[Documents Duplication]] - How to prevent documents with the same references being obscured.

## Configuring Find for IDOL

- [[Find Java System Properties]] - changing some runtime settings
- [[Configuring Find for IDOL]] - guide to the Find for IDOL config file
- [[Find User Roles]] - letting people log into Find

## Configuring Find for HavenOnDemand

- [[Find Java System Properties]] - changing some runtime settings
- [[Configuring Find for Haven OnDemand]] - guide to the Find for Haven OnDemand config file

# Modifying Find

- [[Changing the logo]]
- [[Custom Document Templates]]
- [[Translating Find]]

# What license does it use?

The MIT license - see [the license file](https://github.com/hpe-idol/find/blob/master/LICENSE) for details

# Branches and Tags

We use [Git Flow](http://nvie.com/posts/a-successful-git-branching-model/) for our branching model.

## Branch Structure
- `master` is the latest "known good" version of the code
- `develop` is the bleeding edge version of the code, which will contain unreleased and potentially unstable features
- `feature/*` contains unfinished features that are not yet ready for release
- `release/*` contains temporary release branches.  At the end of every "sprint" (sprints are two-week long periods of work), we take a release branch from `develop`, e.g. `release/sprint77`.  We test the branch and fix any high-priority bugs that are discovered.  Two weeks later (at the end of the following sprint) we finish the release branch and merge the known-good code into the `master` branch.
- `onprem-release/*` branches track released version of Find for IDOL customers.
- Any other branches can be safely ignored at this stage.  Treat them as feature branches.

## Tag Structure
We have two types of tags - fixed tags that track a specific version (e.g. `v1.0.3`), and floating tags that track a commit that is deployed somewhere (e.g. `hsod-preview`).

# Contributing
If you want to contribute to Find, please open a pull request and send us some comments about your change.  No promises that we'll accept it, but we will reply at least.  We would strongly recommend opening a ticket first to discuss what you want to change, as it might be something that we're already working on! :smile: 

# Support
If you are an HPE IDOL Express or HPE IDOL Premium customer, support for Find is available to you via Customer Support, but only if you are running a version of Find which is distributed on the Big Data Download Center.  **No support is provided for the open source version of Find.**