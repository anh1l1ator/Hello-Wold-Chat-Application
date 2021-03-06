package commonsware.com.charapplication1;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
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

public class backgroundServiceForMessages extends Service {
    private LocalBroadcastManager localBroadcastManager;

    public static final String SERVICE_RESULT = "com.service.result";
    public static final String SERVICE_MESSAGE = "com.service.message";

    private final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();
    private final Type listOfMessageHolderObject = new TypeToken<List<MessageHolder>>(){}.getType();
    private final String TAG = "backgroundService";
    private String handle ;
    private Gson gson;
    private Request request;

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        thread.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        if(intent==null)return START_STICKY;
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        handle = intent.getStringExtra(getString(R.string.keyForbgservice));
        DataToSend dataToSend = new DataToSend();
        gson = new Gson();
        dataToSend.setHandle(handle);
        String json = gson.toJson(dataToSend);
        String url = getString(R.string.apiUrlMessageReceive);
        String method = "POST";
        RequestBody body = RequestBody.create(JSON, json);
        request = new Request.Builder()
                .url(url)
                .method(method, body)
                .build();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void onHandleIntent() {
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

    Thread thread = new Thread(new Runnable(){
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            onHandleIntent();
        }
    });


}
