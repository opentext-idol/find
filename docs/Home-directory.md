# What is it?

Find needs a `home` directory in which to store its config file and logs.

# Creating a home directory

- Create a directory, somewhere in your file system.
- Make sure that the user that Find is or will be running as has read and write permissions on this directory

## Linux example

Assuming that your Find user is called `find`, and is in a group called `find`:

    $ mkdir /opt/findhome
    $ chown find:find /opt/findhome
    $ chmod u+rwx /opt/findhome

# Configuring Find to use the home directory

See the [[Java System Properties|Find-Java-System-Properties]] page for details about how to configure the `hp.find.home` system property.