package commonsware.com.charapplication1;

import com.github.bassaer.chatmessageview.models.Message;

import java.util.Date;


public class MessageHolder {
    private DataToReceive sender;
    private DataToReceive receiver;
    private String message;
    private String date;
    private boolean isOwnMessage;

    public boolean isOwnMessage() {
        return isOwnMessage;
    }

    public void setOwnMessage(boolean ownMessage) {
        isOwnMessage = ownMessage;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public DataToReceive getSender() {
        return sender;
    }

    public void setSender(DataToReceive sender) {
        this.sender = sender;
    }

    public DataToReceive getReceiver() {
        return receiver;
    }

    public void setReceiver(DataToReceive receiver) {
        this.receiver = receiver;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String format) {
        this.date = format;
    }
}
