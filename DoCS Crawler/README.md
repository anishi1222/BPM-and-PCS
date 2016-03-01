# DoCS Folder Crawler

Crawl DoCS Folder for moving items to PCS instance specific folder.

## How To ##

## How to run sample application ##
### Edit docs2pcs.json ###
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
This test application works on Java 8.

License
----------
Copyright &copy; 2015 anishi1222
Distributed under the [MIT License][mit].  

[MIT]: http://www.opensource.org/licenses/mit-license.php
