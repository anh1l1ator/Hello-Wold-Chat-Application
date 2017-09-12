package commonsware.com.charapplication1;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class backgroundServiceForMessages extends IntentService {
    private LocalBroadcastManager localBroadcastManager;

    public static final String SERVICE_RESULT = "com.service.result";
    public static final String SERVICE_MESSAGE = "com.service.message";

    private final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();
    private final Type listOfMessageHolderObject = new TypeToken<List<MessageHolder>>(){}.getType();

    private final String TAG = "backgroundService";
    public backgroundServiceForMessages() {
        super("backgroundServiceForMessages");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            final String handle = intent.getStringExtra(getString(R.string.keyForbgservice));
            final DataToSend dataToSend = new DataToSend();
            final Gson gson = new Gson();
            dataToSend.setHandle(handle);
            final String json = gson.toJson(dataToSend);
            final String url = getString(R.string.apiUrlMessageReceive);
            final String method = "POST";
            final RequestBody body = RequestBody.create(JSON, json);
            final Request request = new Request.Builder()
                    .url(url)
                    .method(method, body)
                    .build();

            for(;true;) {
                try {
                    Thread.sleep(1000);
                    try (Response response = client.newCall(request).execute()) {
                        final String data = response.body().string();
                        String messages = data.replace("{\"messagesToReturn\":", "");
                        messages = messages.substring(0,messages.length()-1);
                        Log.i(TAG, messages);
                        if(messages.length()>2) {
                            ArrayList<MessageHolder> listOfMessages = gson.fromJson(messages, listOfMessageHolderObject);
                            int i;
                            for (i = 0; i < listOfMessages.size(); ++i) {
                                final String message = gson.toJson(listOfMessages.get(i));
                                Intent intent0 = new Intent(SERVICE_RESULT);
                                if(message != null)
                                    intent0.putExtra(SERVICE_MESSAGE, message);
                                localBroadcastManager.sendBroadcast(intent0);
                                Context context = getApplicationContext();
                                final String sharedPrefFile = getString(R.string.preference_file_key) + ChatActivity.TAG + handle + listOfMessages.get(i).getSender().getHandle();
                                final SharedPreferences sharedPref = context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE);
                                final String defaultValue = "[]";
                                ArrayList<MessageHolder> temporaryContainer = gson.fromJson(sharedPref.getString(sharedPrefFile, defaultValue), listOfMessageHolderObject);
                                temporaryContainer.add(listOfMessages.get(i));
                                final String toWrite = gson.toJson(temporaryContainer, listOfMessageHolderObject);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(sharedPrefFile, toWrite);
                                editor.commit();
                                CharSequence notiText = "You have new message from "+listOfMessages.get(i).getSender().getHandle();
                                Intent intent01 = new Intent(this, MainActivity.class);
                                PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent01, 0);
                                Notification n  = new Notification.Builder(this)
                                        .setContentTitle(notiText)
                                        .setContentText(notiText)
                                        .setSmallIcon(R.drawable.circle)
                                        .setContentIntent(pIntent)
                                        .setAutoCancel(true)
                                        .addAction(R.drawable.circle, "And more", pIntent).build();
                                NotificationManager notificationManager =
                                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                notificationManager.notify(0, n);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
