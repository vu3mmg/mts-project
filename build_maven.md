#how to build mts with maven (after version 5.5)
# Introduction #

This wiki page explains how to build MTS in maven. This is only applicable for MTS 5.5 and more.

# Maven installation #

We won't explain maven installation. You might use your IDE's bundled maven or a command-line tool. You will _need_ internet connectivity.
Please configure maven according to your proxy if you have one.

# How to build #

You should use the following goals only:
  * clean _cleans the target directory_
  * package _will generate the **target/mts directory** (you can run mts from there)_
  * install _will generate **target/mts-VERSION-STANDARD.jar** (iz pack installer)_

# Tips #

To be able to run MTS from **target/mts directory** you should customise the files :
  * **src/main/bin/java\_home** _(ex: C:\Program Files\Java\jre6)_
  * **src/main/bin/java\_memory** _(ex: 1024)_

To be able to debug, profile MTS, or tweak the JVM, you have to connect remotely, you should customise the file (read java\_arguments.txt) :
  * **src/main/bin/java\_arguments** _(add the relevant arguments)_

Customising those files will have effect when running mts in target/mts directoty; however it will have no effect on the installer. Only the **.release** files are packaged in the installer.