# Audit Instance Image

Generate audit trail image file through Oracle BPM 12c (12.1.3) APIs.

## Required jar files ##
Following jar files are required when running this sample application.
+   `<Oracle_Home>/oracle_common/modules/oracle.jrf_12.1.3`  
    jrf.jar  
+   `<Oracle_Home>/soa/soa/modules/oracle.bpm.client_11.1.1`  
    oracle.bpm.client.jar  
    oracle.bpm.bpm-services.client.jar  
    oracle.bpm.bpm-services.interface.jar  
+   `<Oracle_Home>/soa/soa/modules/oracle.bpm.runtime_11.1.1`  
    oracle.bpm.core.jar  
+   `<Oracle_Home>/soa/soa/modules/oracle.bpm.workspace_11.1.1`  
    oracle.bpm.ui.jar  
+   `<Oracle_Home>/soa/soa/modules/oracle.soa.workflow_11.1.1`  
    bpm-services.jar  
+   `<Oracle_Home>/wlserver/server/lib`  
    wlthint3client.jar  

## How to run sample application ##
### Edit connection.properties ###
+   `hostname`, `port`  
    Host name and port number for connecting BPM server.

+   `username`, `password`
    User credentials for connecting BPM server.

### Run! ###
java -cp $CLASSPATH test.AuditProcImage `Instance ID` `File Path` `Image type`

+   `Instance ID` :  
    Process Instance ID, which is used to specify process instance.

+   `File Path` :  
    Full path of audit trail image file (if a file exists, overwrite and replace the file.)

+   `Image Type` :  
    set 'Process', or 'Audit'  


## Comments ##
This test application works on Java 7.  
Whether this should work on Java 8 has not been confirmed yet.

License
----------
Copyright &copy; 2015 anishi1222
Distributed under the [MIT License][mit].  

[MIT]: http://www.opensource.org/licenses/mit-license.php
