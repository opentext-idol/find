## What is it?

HPE Find includes a [Vagrant](http://vagrantup.com) file, which will provision an Ubuntu 12.04 VM running a [Redis](http://redis.io/) server for storing user sessions.

The Vagrantfile requires several plugins, which will be installed automatically if they are not installed already.

## Details

- The VM has the IP address 192.168.242.242
- One of the Vagrant plugins will set up a local DNS entry in your hosts file with the hostname `hp-find-backend`.
- The Redis runs on port 6379.

## Do I need this?

Only if you're testing a distributed version of Find.  If you're using `INMEMORY` session storage (see: [[Find Java System Properties]]) then you won't need the Redis.  If you're using `REDIS` session storage, you will want to use this.

*Do not* use the Vagrant VM for production instances of Find.  This is a development tool and should only be used in your development environment.