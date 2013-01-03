Towns mod loader
================

The mod loader uses code injection techniques to modify the functionality of the Towns game.

Building
--------

You will need to install the xaos.jar from game's lib folder into the maven repository.
For the current (v8a) build on OS X, do change the path to the jar though:

	mvn install:install-file -Dfile=/Users/muzzy/Library/Application\ Support/Steam/SteamApps/common/towns/lib/xaos.jar -DgroupId=com.townsgame -DartifactId=xaos -Dversion=0.8.1 -Dpackaging=jar

