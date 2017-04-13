# Getting Started

- First, follow the [[Building Find]] guide
- Then, follow either the [[Running a Development Copy of Find]] or [[Running a Production Copy of Find]] guide, depending on what you're trying to do.

You should now have a file called `config.json` in your Find home directory.

By default, it's going to look a lot like this:
```
{
  "login" : {
    "name" : "CommunityAuthentication",
    "defaultLogin" : {
      "username" : "admin",
      "password" : "s0m3r4nd0mp455w0rd"
    },
    "community" : {
      "protocol" : "HTTP",
      "host" : "find-backend",
      "port" : 9030
    },
    "method" : "autonomy"
  },
  "content" : {
    "protocol" : "HTTP",
    "host" : "find-idol",
    "port" : 9000
  },
  "queryManipulation" : {
    "server" : {
      "protocol" : "HTTP",
      "host" : "find-idol",
      "port" : 16000
    },
    "expandQuery" : true,
    "blacklist" : "ISO_BLACKLIST",
    "enabled" : true
  },
  "view" : {
    "referenceField" : "DREREFERENCE",
    "host" : "find-idol",
    "protocol" : "HTTP",
    "port" : 9080
  }
}
```

Goodness me, that looks complicated.  Where do we even start..?

# You need to understand JSON

JSON is quite a nice format, but it doesn't allow comments.  Spend a bit of time reading [json.org](http://json.org/) if you've not used JSON before.

In this guide, I will use path notation to reference bits of the config file.  For example, if we had a JSON config that looked like this:
```
{
  "someobject": {
    "somesetting": "SOMESTRING"
  }
}
```

then I will say that the value of `someobject.somesetting` is currently `"SOMESTRING"`.

# What's in the Config file, anyway?

The config file for Find for IDOL contains four sections:

- `login` - authentication settings
- `content` - details of the IDOL Content server
- `queryManipulation` - details of the IDOL QMS server
- `view` - details of the IDOL View server

# Configuration
## The `login` section

This section is used for configuring the connection to the IDOL Community server used for authentication.

Ignore `login.name`, and `login.defaultLogin` for the moment.  These are reserved for future use.

Configure the following settings:
- `login.community.protocol` - either `HTTP` or `HTTPS`, depending on how your IDOL Community server is configured.
- `login.community.host` - the hostname of your IDOL Community server, e.g. `community.idol.corp.example.com`
- `login.community.port` - the port that your IDOL Community server is running on, e.g. `9030`
- `login.method` - the security repository to read the user from ([see the IDOL documentation](http://my.vertica.com/docs/IDOL/Servers/IDOLServer/10.11/Help/index.html#Actions/User/ActionParameters/Repository.htm)), e.g. `autonomy` for built-in users.

## The `content` section

This section is used for configuring the connection to the IDOL Content server used for querying.

Configure the following settings:
- `content.protocol` - either `HTTP` or `HTTPS`, depending on how your IDOL Content server is configured.
- `content.host` - the hostname of your IDOL Content server, e.g. `content.idol.corp.example.com`
- `content.port` - the port that your IDOL Content server is running on, e.g. `9000`

## The `queryManipulation` section

This section is used for configuring the connection to the IDOL Query Manipulation Service server.  This is optional and can be disabled.

For the time being, ignore the `queryManipulation.expandQuery` and `queryManipulation.blacklist` settings.

Configure the following settings:
- `queryManipulation.server.protocol` - either `HTTP` or `HTTPS`, depending on how your IDOL QMS server is configured.
- `queryManipulation.server.host` - the hostname of your IDOL QMS server, e.g. `qms.idol.corp.example.com`
- `queryManipulation.server.port` - the port that your IDOL QMS server is running on, e.g. `16000`
- `queryManipulation.enabled` - set this to `true` to use QMS, or `false` to disable QMS entirely.  If this is disabled, queries will be sent directly to the Content server configured in the `content` section of the config.  No other QMS settings need to be set if this is set to `false`.

## The `view` section

This section is used for configuring the connection to the IDOL View server used for near-native document viewing.

Configure the following settings:
- `view.protocol` - either `HTTP` or `HTTPS`, depending on how your IDOL View server is configured.
- `view.host` - the hostname of your IDOL View server, e.g. `view.idol.corp.example.com`
- `view.port` - the port that your IDOL View server is running on, e.g. `9080`
- `view.referenceField` - the field in the IDOL document to use as a reference when sending `action=view` to Viewserver, e.g. `URL`, `DREREFERENCE`, or `FILE_PATH`.

# Restart Find

Having changed your config file, you **absolutely must** restart the Find web application for the changes to take effect.