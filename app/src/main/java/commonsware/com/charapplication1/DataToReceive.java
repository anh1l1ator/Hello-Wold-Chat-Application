package commonsware.com.charapplication1;

/**
 * Created by shubham on 10/9/17.
 */

public class DataToReceive {

    private String email;
    private String id;
    private String locale;
    private String name;
    private String picture_url;
    private String handle;
    private int existing;
    private int handleExists;
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }


    public int getHandleExists() {
        return handleExists;
    }

    public void setHandleExists(int handleExists) {
        this.handleExists = handleExists;
    }

    public int getExisting() {
        return existing;
    }

    public void setExisting(int existing) {
        this.existing = existing;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPicture_url() {
        return picture_url;
    }

    public void setPicture_url(String picture_url) {
        this.picture_url = picture_url;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return handle;
    }


}
