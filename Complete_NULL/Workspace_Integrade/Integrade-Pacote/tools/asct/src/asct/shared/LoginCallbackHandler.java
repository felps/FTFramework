/**
 * 
 */
package asct.shared;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * @author jrbraga
 *
 */
public class LoginCallbackHandler implements CallbackHandler {
	private String userName;
	private String password;
	
	public LoginCallbackHandler(String userName, String password) {
		this.userName=userName;
		this.password=password;
	}
	/* (non-Javadoc)
	 * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
	 */
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		 for(int i=0;i<callbacks.length;i++) {
	            Callback callBack = callbacks[i];
	            // Handles username callback.
	            if (callBack instanceof NameCallback) {
	                NameCallback nameCallback = (NameCallback)callBack;
	                nameCallback.setName(this.userName);

	             // Handles password callback.
	            } else if (callBack instanceof PasswordCallback) {
	              PasswordCallback passwordCallback = (PasswordCallback)callBack;
	              passwordCallback.setPassword(this.password.toCharArray());

	          } else {
	              throw new UnsupportedCallbackException(callBack, "Call back not supported");
	          }//else

		
		 }
	}
}
