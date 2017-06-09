package pcsoauth2;

import java.io.StringReader;

import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;


public class OAuth2Client {
    public OAuth2Client() {
        super();
    }

    static HashMap entryMap;

    public static void main(String[] args) {
        OAuth2Client oAuth2Client = new OAuth2Client();
        entryMap = populateMap();

        // Obtain Client Assertion with Client ID and Client Secret
        try {
            String accessToken = oAuth2Client.getOAuthToken();
            FundsTransferRequest ftr = new FundsTransferRequest();
            System.out.println("[HTTP Status]"+ oAuth2Client.invokeFundsTransferProcess(accessToken, ftr));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return
     */
    public static HashMap populateMap() {
        HashMap<String, String> map = new HashMap();

        // PCS
        map.put("PCS_URL", "https://<PCS Host>:443/bpm/api/4.0/processes");
        map.put("PCS_PROCESS_DEF_ID", "Process Definition ID");
        map.put("PCS_FTS_SVC_NAME", "FundsTransferProcess.service");

        // OAuth
        map.put("TOKEN_URL", "https://<IdentityDomain>.identity.<DataCenter>.oraclecloud.com/oam/oauth2/tokens");
        map.put("CLIENT_ID", "CLIENT_ID");
        map.put("SECRET", "SECRET");
        map.put("DOMAIN_NAME", "DOMAIN_NAME");
        map.put("USER_NAME", "USER_NAME");
        map.put("PASSWORD", "PASSWORD");
        return map;
    }

    public String getOAuthToken() throws Exception {
        String authString = entryMap.get("CLIENT_ID").toString() + ":" + entryMap.get("SECRET").toString();
        return getAccessToken(authString, getClientAssertion(authString));
    }

    private Map<String, String> getClientAssertion(String authString) throws Exception {

        MultivaluedHashMap<String, String> formData = new MultivaluedHashMap();
        formData.putSingle("grant_type", "client_credentials");

        Response response
             = ClientBuilder.newClient()
                            .target(entryMap.get("TOKEN_URL").toString())
                            .register(new BasicAuthenticator(entryMap.get("CLIENT_ID").toString(), entryMap.get("SECRET").toString()))
                            .request()
                            .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8")
                            .buildPost(Entity.entity(formData, 
                                                     MediaType.APPLICATION_FORM_URLENCODED_TYPE))
                            .invoke();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new Exception(response.getStatusInfo().getReasonPhrase());
        }

        Map<String, String> assertionMap = new HashMap<>();
        try (JsonReader reader = Json.createReader(new StringReader(response.readEntity(String.class)))) {
            response.close();
            JsonObject jsonObj = reader.readObject();
            assertionMap.put("assertion_token", jsonObj.get("access_token").toString());
            assertionMap.put("assertion_type", jsonObj.get("oracle_client_assertion_type").toString());
        }
        return assertionMap;
    }

    private String getAccessToken(String authString, Map clientAssertionMap) throws Exception {

        MultivaluedHashMap<String, String> formData = new MultivaluedHashMap();
        formData.putSingle("grant_type", "password");
        formData.putSingle("username", entryMap.get("USER_NAME").toString());
        formData.putSingle("password", entryMap.get("PASSWORD").toString());
        formData.putSingle("client_assertion_type", clientAssertionMap.get("assertion_type").toString());
        formData.putSingle("client_assertion", clientAssertionMap.get("assertion_token").toString());

        Response response = 
            ClientBuilder.newClient()
                         .target(entryMap.get("TOKEN_URL").toString())
                         .register(new BasicAuthenticator(entryMap.get("CLIENT_ID").toString(), entryMap.get("SECRET").toString()))
                         .request()
                         .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8")
                         .buildPost(Entity.entity(formData, MediaType.APPLICATION_FORM_URLENCODED_TYPE))
                         .invoke(Response.class);

        if (response == null || response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new Exception(response.getStatusInfo().getReasonPhrase());
        }

        String accessToken;
        try (JsonReader reader = Json.createReader(new StringReader(response.readEntity(String.class)))) {
            response.close();
            JsonObject jsonObj = reader.readObject();
            accessToken = jsonObj.getString("access_token");
        }
        return accessToken;
    }

    public String invokeFundsTransferProcess(String token, FundsTransferRequest ftr) throws Exception {

        JsonObject postObj =
            Json.createObjectBuilder()
                .add("processDefId", entryMap.get("PCS_PROCESS_DEF_ID").toString())
                .add("serviceName", entryMap.get("PCS_FTS_SVC_NAME").toString())
                .add("operation", "start")
                .add("action", "Submit")
                .add("params",
                    Json.createObjectBuilder()
                        .add("incidentId", ftr.getIncidentId())
                        .add("sourceAcctNo", ftr.getSourceAcctNo())
                        .add("destAcctNo", ftr.getDestAcctNo())
                        .add("amount", ftr.getAmount())
                        .add("transferType",
                                ftr.getTransferType()
                                .equals("tparty") ? "intra" : "inter"))
                .build();

        Response response = 
            ClientBuilder.newClient()
                         .target(entryMap.get("PCS_URL").toString())
                         .register(new OAuth2Authenticator(token))
                         .request(MediaType.APPLICATION_JSON_TYPE)
                         .buildPost(Entity.entity( postObj.toString(), 
                                                   MediaType.APPLICATION_JSON_TYPE))
                         .invoke(Response.class);

        if (response != null && response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new Exception(response.getStatusInfo().getReasonPhrase());
        }
        return String.valueOf(response.getStatus());
    }
}
