Hare and Hounds Web App
=================

This repository contains a hare and hounds game web application.  It is a complete web stack from frontend to server to database. The backend is written in Kotlin.

Author
-----
Ryan Demo

601.421 Object Oriented Software Engineering

Johns Hopkins University

Fall 2017

Setup
-----

It depends only on Java 8 and Maven to build and run.

For details on installing Java 8 and an IDE that includes Maven, see the [OOSE Tools] page.


Usage
-----

The code should be directly importable as an existing Maven project into Eclipse or IntelliJ, and should directly build and run from within the IDE.

If you are using IntelliJ, clone the git repository on your computer and then use the IntelliJ Import function to import that clone.  Select the pom.xml file as what you want to import (to be clear you are importing it as a Maven project), and you can use the defaults for all other import parameters.  Once it is imported you can invoke any of the Maven phases.  You can right-click on the Bootstrap class to start the server.  Or, set up a run configuration to run <tt>Bootstrap.main()</tt>  -- from the Run menu select Edit Configurations, add (+) a new Application configuration, and set the main class to be Bootstrap.  The green triangle will then launch the server.

If you instead want to compile and run from the command line, you will need the Maven command line installed and invoke:

```console
mvn package
java -jar target/hare-and-hounds-1.0-SNAPSHOT.jar
```

Now simply point your browser to http://localhost:8080 to use the application.

[OOSE Tools]:http://pl.cs.jhu.edu/oose/resources/tools.shtml
[OOSE Web Java Lecture]: http://pl.cs.jhu.edu/oose/lectures/webjava.shtml
