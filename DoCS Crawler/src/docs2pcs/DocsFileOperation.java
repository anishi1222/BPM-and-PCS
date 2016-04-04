package docs2pcs;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;

import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class DocsFileOperation {

    private String DOCS_TARGET_URL;
    private String PCS_TARGET_URL;
    private String TRIGGER_FOLDER_ID;
    private String PROCESS_DEFINITION_ID;
    private Authenticator auth;
    private String USERNAME;
    private String PASSWD;
    private int THREAD_CNT;
    private int DEQUE_CNT;
    private String PARENT_FOLDER_ID;
    private String TARGET_FOLDER_PREFIX;
    private ExecutorService execService;
    private ConcurrentHashMap<String, Param4MovingItem> map;
    private boolean suspended;
    private LinkedBlockingDeque<Param4MovingItem> queue;

    public DocsFileOperation() {
    }

    public boolean Initialize() {
        // Read initial parameter from JSON file
        boolean ret = true;
        try (JsonReader reader = Json.createReader(new FileReader("docs2jcs.json"))) {
            JsonObject jsonObj = reader.readObject();
            THREAD_CNT = jsonObj.getInt("ThreadCnt");
            DEQUE_CNT = jsonObj.getInt("DequeCnt");
            DOCS_TARGET_URL = jsonObj.getString("DoCSTargetUrl");
            PCS_TARGET_URL = jsonObj.getString("PCSTargetUrl");
            PROCESS_DEFINITION_ID = jsonObj.getString("ProcessDefinitionId");
            TRIGGER_FOLDER_ID = jsonObj.getString("TriggerFolderId");
            PARENT_FOLDER_ID = jsonObj.getString("parentFolderId");
        	TARGET_FOLDER_PREFIX = jsonObj.getString("TargetFolderPrefix");
            USERNAME = jsonObj.getString("UserName");
            PASSWD = jsonObj.getString("Passwd");

            auth = new Authenticator(USERNAME, PASSWD);
            queue = new LinkedBlockingDeque<>(DEQUE_CNT);

            // # of thread is equals to sum of defined thread count and # of thread for monitoring file
            execService = Executors.newFixedThreadPool(THREAD_CNT + 1);
            map = new ConcurrentHashMap<String, Param4MovingItem>();
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(DocsFileOperation.class.getName()).log(Level.SEVERE, null, ex);
            ret = false;
        }
        return ret;
    }

    public synchronized void suspend() {
        suspended = false;
    }

    public synchronized boolean isSuspended() {
        return suspended;
    }

    /**
     * monitoring file, moving file, and invoking PCS process
     */
    public void start() {

        CallPCSProcess callPcs = new CallPCSProcess();
        CheckDocsFolder checkFolder = new CheckDocsFolder();
        execService.submit(checkFolder);
        for (int i = 1; i < THREAD_CNT + 1; i++) {
            execService.submit(callPcs);
        }

        // wait for entry
        Scanner scan = new Scanner(System.in);
        String str = scan.next();
    }

    /**
     *
     */
    public void checkItem() {
        /**
         * Monitoring a specified folder
         */
        try {
            // Check items in monitored folder
            JsonObject docsJSONobj = getFolderItems(TRIGGER_FOLDER_ID, null);
            Integer count = Integer.parseInt(docsJSONobj.getString("count"));
            if (count < 1) {
                return;
            }

            JsonArray itemsArray = docsJSONobj.getJsonArray("items");
            for (int i = 0; i < itemsArray.size(); i++) {
                JsonObject jObj = itemsArray.getJsonObject(i);

                // Is type of item file, folder, or others?
                String _type = jObj.getString("type");
                if (!_type.equals("file") && !_type.equals("folder")) {
                    continue;
                }

                // each element is packed into Param4MovingItem object
                Param4MovingItem p4mi = new Param4MovingItem();
                String _itemId = jObj.getString("id");

                // If key value is stored in Map, this item is now being processed and we should not process this item.
                if (!map.containsKey(_itemId)) {
                    p4mi.setItemId(_itemId);
                    p4mi.setItemName(jObj.getString("name"));
                    p4mi.setItemType(_type);
                    p4mi.setUserId(jObj.getJsonObject("createdBy").getString("displayName"));

                    // Map is used for interlocking resource (key: itemId)
                    map.put(p4mi.getItemId(), p4mi);

                    // insert Deque (timeout is not required)
                    queue.putLast(p4mi);
                }
            }
        } catch (NumberFormatException | InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * @param _instanceId
     * @param _itemType
     * @param _itemId
     * @return
     */
    public Boolean moveItem(String _instanceId, String _itemType, String _itemId) {

        Boolean ret = false;
        try {
            /**
             * Searching for items in specified folder
             */
            String _filterName = TARGET_FOLDER_PREFIX + "_" + _instanceId;
            JsonObject docsJSONobj1 = getFolderItems(PARENT_FOLDER_ID, _filterName);
            if (docsJSONobj1 == null) {
                return ret;
            }

            // Return error when there is no child item
            Integer count = Integer.parseInt(docsJSONobj1.getString("count"));
            if (count < 1) {
                return ret;
            }

            // Check items element
            JsonArray itemsArray = docsJSONobj1.getJsonArray("items");
            String _folderId = null;

            for (int i = 0; i < itemsArray.size(); i++) {
                JsonObject jObj = itemsArray.getJsonObject(i);

                // Unless item type is folder, check next item
                if (!jObj.getString("type").equals("folder")) {
                    continue;
                }
                // If folder name is different from filterName, check next item
                if (!jObj.getString("name").equals(_filterName)) {
                } // Get folder Id and exit loop
                else {
                    _folderId = jObj.getString("id");
                    break;
                }
            }
            // If _folderid is null, return error
            if (_folderId == null) {
                return ret;
            }
            // Get Item Id of inner folder
            JsonObject docsJSONobj2 = this.getFolderItems(_folderId, "PHOTO");
            if (docsJSONobj2 == null) {
                return ret;
            }

            // If no child item is found, return error
            count = Integer.parseInt(docsJSONobj2.getString("count"));
            if (count < 1) {
                return ret;
            }

            String destId = null;
            JsonArray itemsArray2 = docsJSONobj2.getJsonArray("items");
            for (int i = 0; i < itemsArray2.size(); i++) {
                JsonObject jObj = itemsArray2.getJsonObject(i);

                // Unless item type is folder, check next item
                if (!jObj.getString("type").equals("folder")) {
                } else {
                    destId = jObj.getString("id");
                    break;
                }
            }
            // Return error in case destId is null
            if (destId == null) {
                return ret;
            }

            // Move file to the folder specified with folder id
            ret = postMoveItem(_itemId, _itemType, destId);
        } catch (ForbiddenException ex) {
            ex.printStackTrace(System.err);
        }
        return ret;
    }

    /**
     * @param _id
     * @param _filterName
     * @return
     */
    private JsonObject getFolderItems(String _id, String _filterName) {
        Client client = ClientBuilder.newClient().register(auth);
        JsonObject docsJSONobj = null;
        Response response = null;

        WebTarget myResource = client.target(DOCS_TARGET_URL).path("/api/1.1/folders/{id}/items");
        if (_filterName != null) {
            response =
                myResource.resolveTemplate("id", _id).queryParam("filterName",
                                                                 _filterName).request(MediaType.APPLICATION_JSON).get(Response.class);
        }
        else {
            response = myResource.resolveTemplate("id", _id).request(MediaType.APPLICATION_JSON).get(Response.class);
        }
        // Check HTTP Status
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            response.close();
        } else {
            // Get Object Tree to return a value as JsonObject
            try (JsonReader reader = Json.createReader(new StringReader(response.readEntity(String.class)))) {
                response.close();
                docsJSONobj = reader.readObject();
            }
        }
        return docsJSONobj;
    }

    /**
     * @param _itemId
     * @param _destinationId
     * @return
     */
    private Boolean postMoveItem(String _itemId, String _itemType, String _destinationId) {
        Client client = ClientBuilder.newClient().register(auth);
        Boolean ret = false;
        WebTarget myResource = null;

        // Is this _itemId pointed to file or folder?
        switch (_itemType) {
        case "file":
            myResource = client.target(DOCS_TARGET_URL).path("/api/1.1/files/{id}/move");
            break;
        case "folder":
            myResource = client.target(DOCS_TARGET_URL).path("/api/1.1/folders/{id}/move");
            break;
        default:
            return ret;
        }

        // create response in the format of JSON
        JsonObject putObj = Json.createObjectBuilder().add("destinationID", _destinationId).build();

        Response response;
        response =
            myResource.resolveTemplate("id", _itemId).request().buildPost(Entity.entity(putObj.toString(),
                                                                                        MediaType.APPLICATION_JSON)).invoke(Response.class);

        // Check HTTP Status
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            response.close();
            return ret;
        }
        try (JsonReader reader = Json.createReader(new StringReader(response.readEntity(String.class)))) {
            response.close();
            JsonObject docsJSONobj = reader.readObject();
            String errorCode = docsJSONobj.getString("errorCode");
            ret = errorCode.equals("0");
        }
        return ret;
    }

    class CallPCSProcess implements Runnable {

        @Override
        public void run() {

            Client client = ClientBuilder.newClient().register(auth);
            JsonObject pcsJSONobj = null;
            Response response = null;

            WebTarget myResource = client.target(PCS_TARGET_URL).path("/api/3.0/processes");

            while (!isSuspended()) {
                try {
                    Param4MovingItem p4mi = queue.pollFirst(1L, TimeUnit.SECONDS);
                    if (p4mi != null) {
                        JsonObject postObj =
                            Json.createObjectBuilder().add("processDefId", PROCESS_DEFINITION_ID)
                                                      .add("operation", "start")
                                                      .add("params", Json.createObjectBuilder()
                                                      .add("docId", p4mi.getItemId())
                                                      .add("docName", p4mi.getItemName())
                                                      .add("userId", p4mi.getUserId())
                                                    )
                                                    .build();
                        response =
                            myResource.request().buildPost(Entity.entity(postObj.toString(),
                                                                         MediaType.APPLICATION_JSON)).invoke(Response.class);

                        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                            // ResponseをParse
                            try (JsonReader reader = Json.createReader(new StringReader(response.readEntity(String.class)))) {
                                response.close();
                                pcsJSONobj = reader.readObject();
                                String instanceId;
                                if (pcsJSONobj.getString("state").equalsIgnoreCase("OPEN")) {
                                    instanceId = pcsJSONobj.getString("processId");
                                    moveItem(instanceId, p4mi.getItemType(), p4mi.getItemId());

                                    map.remove(p4mi.getItemId());
                                }
                            }
                        }
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace(System.err);
                    Logger.getLogger(DocsFileOperation.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    class CheckDocsFolder implements Runnable {
        @Override
        public void run() {
            while (!isSuspended()) {
                checkItem();
                try {
                   synchronized (this) {
                       wait(100);
                   }
                } catch (InterruptedException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
    }
}
