package docs2pcs;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;

import javax.xml.bind.DatatypeConverter;

public class Authenticator implements ClientRequestFilter {
    private final String _user;
        private final String _password;

        public Authenticator(String user, String password) {
            this._user = user;
            this._password = password;
        }

    /**
     *
     * @param requestContext
     * @throws IOException
     */
    @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            MultivaluedMap<String, Object> headers = requestContext.getHeaders();
            final String basicAuthentication = getBasicAuthentication();
            headers.add("Authorization", basicAuthentication);

        }

        private String getBasicAuthentication() {
            String token = this._user + ":" + this._password;
            try {
                return "BASIC " + DatatypeConverter.printBase64Binary(token.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                throw new IllegalStateException("Cannot encode with UTF-8", ex);
            }
        }
}
