openmrs-module-hipmodule
==========================

HIP module for exposing HIP specific features from OpenMRS.

Description
-----------
This module is supporting the HIP service with all necessary endpoints exposed that are required by the HIP service.

Building from Source
--------------------
You will need to have Java 1.6+ and Maven 2.x+ installed. 
You need to install the OpenMrs module SDK from https://github.com/openmrs/openmrs-sdk (see Readme for installation instructions)

Use the command `mvn clean install` to build the module, the .omod file will be in the omod/target folder.

Installation
------------
1. Build the module to produce the .omod file.
2. Use the OpenMRS Administration > Manage Modules screen to upload and install the .omod file.

If uploads are not allowed from the web (changable via a runtime property), you can drop the omod
into the ~/.OpenMRS/modules folder.  (Where ~/.OpenMRS is assumed to be the Application 
Data Directory that the running openmrs is currently using.)  After putting the file in there 
simply restart OpenMRS/tomcat and the module will be loaded and started.
