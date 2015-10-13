# Manipulate Application Role

Maintain Application Role through Oracle BPM 12c (12.1.3) APIs.

## Required jar files ##
Following jar files are required when running this sample application.
+ `<ORACLE_HOME>/wlserver/server/lib/wlthint3client.jar`
+ `<ORACLE_HOME>/soa/soa/modules/oracle.soa.fabric_11.1.1/bpm-infra.jar`
+ `<ORACLE_HOME>/soa/soa/modules/oracle.soa.workflow_11.1.1/bpm-services.jar`
+ `<ORACLE_HOME>/soa/soa/modules/oracle.bpm.client_11.1.1/oracle.bpm.bpm-services.client.jar`
+ `<ORACLE_HOME>/soa/soa/modules/oracle.bpm.client_11.1.1/oracle.bpm.bpm-services.interface.jar`
+ `<ORACLE_HOME>/soa/soa/modules/oracle.bpm.runtime_11.1.1/oracle.bpm.casemgmt.interface.jar`
+ `<ORACLE_HOME>/oracle_common/modules/com.oracle.webservices.fmw.jrf-ws-api_12.1.3.jar`
+ `<ORACLE_HOME>/oracle_common/modules/com.oracle.webservices.fmw.wsclient-impl_12.1.3.jar`
+ `<ORACLE_HOME>/oracle_common/modules/clients/com.oracle.webservices.fmw.client_12.1.3.jar`

## How to run sample application ##
### Edit connection.properties ###
+   `hostname`, `port`  
    Host name and port number for connecting BPM server.

+   `username`, `password`
    User credentials for connecting BPM server.

### Run! ###
java -cp $CLASSPATH maintenance.sample.AppRoleUtil `operation for application role` `Application Role Name` `operation for member` `Member Name`

+   `operation for application role` :  
    -create | -delete | -operole | -lstrole

+   `Application Role Name` :  
    Application Role to be created, removed, or manipulated.

+   `operation for member` :  
    -add | -remove | -list  

+   `Member Name` :  
    Member to be added or removed to/from application role  


## Comments ##
This test application works on Java 7.  
Whether this should work on Java 8 has not been confirmed yet.

License
----------
Copyright &copy; 2015 anishi1222
Distributed under the [MIT License][mit].  

[MIT]: http://www.opensource.org/licenses/mit-license.php
