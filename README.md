Rentchecker
===========

Copyright © 2014, Robert Dimsdale. Licensed under [Apache v2 License].

About
-----
Check rent owed at myequityapartments.com

Requirements
------------

Gradle >= 1.10

Android SDK >= 7

Jdk 1.7

Usage
-----

To compile (from the top-level directory): gradle clean build

To run the cmd application (after compiling): java -jar cmd/build/libs/rentchecker-cmd-0.0.1.jar

The cmd application takes -u USERNAME and -p PASSWORD. If either of these are omitted, it will prompt for them.

The android application currently requires the username and password hard-coded. Replace the values in MainActivity.java and rebuild.

TODO
----

Replace hard-coded username/password in Android app with data in preferences, and prompt if data is missing (e.g. first-time run).

 [Apache v2 License]: https://github.com/robdimsdale/rentchecker/raw/master/LICENSE
