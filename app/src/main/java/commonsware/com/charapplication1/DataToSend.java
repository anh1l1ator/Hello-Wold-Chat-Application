package commonsware.com.charapplication1;

/**
 * Created by shubham on 10/9/17.
 */

public class DataToSend {
    private String idToken;
    private String handle;
    private String handleToSend;

    public String getHandleToSend() {
        return handleToSend;
    }

    public void setHandleToSend(String handleToSend) {
        this.handleToSend = handleToSend;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }
};
