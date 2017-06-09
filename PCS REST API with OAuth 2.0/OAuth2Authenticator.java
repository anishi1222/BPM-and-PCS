package pcsoauth2;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;

public class OAuth2Authenticator implements ClientRequestFilter {
    private final String accessToken;

    public OAuth2Authenticator(String accessToken) {
        this.accessToken = accessToken;
    }

    public OAuth2Authenticator() {
        this.accessToken = null;
    }

    public void filter(ClientRequestContext requestContext) throws IOException {
        MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        final String basicAuthentication = "Bearer " + this.accessToken;
        headers.add("Authorization", basicAuthentication);
    }
}
