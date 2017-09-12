package commonsware.com.charapplication1;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.bassaer.chatmessageview.models.Message;
import com.github.bassaer.chatmessageview.models.User;
import com.github.bassaer.chatmessageview.views.ChatView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "chatActivity";
    private DateFormat dateFormat=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CANADA);
    private final Type listOfMessageHolderObject = new TypeToken<List<MessageHolder>>(){}.getType();
    private String sharedPrefFile;
    private SharedPreferences sharedPref;

    private BroadcastReceiver broadcastReceiver;
    private ChatView mChatView;
    private Gson gson = new Gson();

    private DataToReceive receiver;
    private DataToReceive sender;
    private User receivingUser;
    private User sendingUser;
    private ArrayList<MessageHolder> messageList= new ArrayList<>();

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver),
                new IntentFilter(backgroundServiceForMessages.SERVICE_RESULT));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    private void updateUiForElement(MessageHolder messageHolder) {
        Message message;
        if(messageHolder.getSender()==receiver) {
            message = new Message.Builder()
                    .setUser(receivingUser)
                    .setRightMessage(true)
                    .setMessageText(messageHolder.getMessage())
                    .hideIcon(true)
                    .build();
        } else {
            message = new Message.Builder()
                    .setUser(sendingUser)
                    .setRightMessage(false)
                    .setMessageText(messageHolder.getMessage())
                    .hideIcon(true)
                    .build();
        }
        mChatView.send(message);
    }

    private void updateUi() {
        int i;
        for(i=0;i<messageList.size();++i) {
            updateUiForElement(messageList.get(i));
        }
    }

    private void handleIntentExtras(Bundle extras) {
        final String receiverHandle = extras.getString(getString(R.string.receivingHandle));
        final String senderHandle = extras.getString(getString(R.string.sendingHandle));
        receiver = gson.fromJson(receiverHandle, DataToReceive.class);
        sender = gson.fromJson(senderHandle, DataToReceive.class);
    }

    private void initialiseUsers() {
        int myId = 0;
        Bitmap myIcon = BitmapFactory.decodeResource(getResources(), R.drawable.face_2);
        int yourId = 1;
        Bitmap yourIcon = BitmapFactory.decodeResource(getResources(), R.drawable.face_1);
        receivingUser = new User(myId, receiver.getName(), myIcon);
        sendingUser   = new User(yourId, sender.getName(), yourIcon);
    }

    private void initialiseChatView() {
        mChatView = (ChatView) findViewById(R.id.chat_view);
        mChatView.setOnClickSendButtonListener(this);
        mChatView.setBackgroundColor(getResources().getColor(R.color.cream));
        mChatView.setLeftBubbleColor(getResources().getColor(R.color.purple));
    }

    private void initialiseUi() {
        final String defaultValue = "[]";
        final String arrayList = sharedPref.getString(sharedPrefFile, defaultValue);
        messageList = gson.fromJson(arrayList, listOfMessageHolderObject);
        updateUi();
    }

    private void initialiseSharedPref() {
        Context context = getApplicationContext();
        sharedPrefFile = getString(R.string.preference_file_key)  + TAG + receiver.getHandle() + sender.getHandle();
        sharedPref = context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE);
    }

    private void registerBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra(backgroundServiceForMessages.SERVICE_MESSAGE);
                Log.d(TAG, message);
                MessageHolder messageHolder = gson.fromJson(message, MessageHolder.class);
                if(messageHolder.getSender().getHandle().equals(sender.getHandle())) {
                    updateUiForElement(messageHolder);
                    messageList.add(messageHolder);
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        handleIntentExtras(intent.getExtras());
        initialiseUsers();
        initialiseChatView();
        initialiseSharedPref();
        initialiseUi();
        registerBroadcastReceiver();
        setTitle(sender.getHandle());
    }

    private void sendMessage(Message message) {
        Date date = new Date();
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setMessage(message.getMessageText());
        messageHolder.setReceiver(sender);
        messageHolder.setSender(receiver);
        messageHolder.setDate(dateFormat.format(date));
        messageList.add(messageHolder);
        final String json = gson.toJson(messageHolder);
        new HttpPostRequest().execute(getString(R.string.apiUrlMessage),json,"POST");
    }

    @Override
    public void onClick(View view) {
        Log.i(TAG, "Successfully called OnClick of Chat Activity!");
        Message message = new Message.Builder()
                .setUser(receivingUser)
                .setRightMessage(true)
                .setMessageText(mChatView.getInputText())
                .hideIcon(true)
                .build();
        mChatView.send(message);
        mChatView.setInputText("");
        sendMessage(message);
        Log.i(TAG, "Json creation was successful!");
    }

    private void writeToFile() {
        final String toWrite = gson.toJson(messageList, listOfMessageHolderObject);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(sharedPrefFile, toWrite);
        editor.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        writeToFile();
    }

    private class HttpPostRequest extends postRequestBaseClass {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i(TAG, result);
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }
    }

}
