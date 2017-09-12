package commonsware.com.charapplication1;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.constraint.solver.SolverVariable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.github.bassaer.chatmessageview.views.ChatView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.R.attr.data;
import static android.R.attr.fromId;
import static android.R.attr.key;

public class addUsernameActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "addUsernameActivity";
    private ArrayList<DataToReceive> friendList = new ArrayList<>();
    private SharedPreferences sharedPref;
    private DataToReceive userData;
    private String sharedPrefFile;
    private final Type listOfStringObject = new TypeToken<List<DataToReceive>>(){}.getType();
    private Gson gson;

    private void updateUi() {
        ListView listView = (ListView)findViewById(R.id.listOfHandles);
        ArrayAdapter<DataToReceive> arrayAdapter = new ArrayAdapter<DataToReceive>(
                this,
                android.R.layout.simple_list_item_1,
                friendList);
        listView.setAdapter(arrayAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_username);
        findViewById(R.id.addFriend).setOnClickListener(this);
        EditText editText = (EditText)findViewById(R.id.handleTextBox);
        editText.setHint(getString(R.string.handle_eg_anh1l1ator));
        ListView listView = (ListView)findViewById(R.id.listOfHandles);
        listView.setOnItemClickListener(mMessageClickedHandler);
        final String data = getIntent().getStringExtra(getString(R.string.keyToPassData));
        gson = new Gson();
        userData = gson.fromJson(data, DataToReceive.class);
        Context context = getApplicationContext();
        sharedPrefFile = getString(R.string.preference_file_key) + TAG + userData.getHandle();
        sharedPref = context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE);
        final String defaultValue = "[]";
        final String arrayList = sharedPref.getString(sharedPrefFile, defaultValue);
        friendList = gson.fromJson(arrayList, listOfStringObject);
        updateUi();
        Intent serviceIntent = new Intent(this, backgroundServiceForMessages.class);
        serviceIntent.putExtra(getString(R.string.keyForbgservice),userData.getHandle());
        startService(serviceIntent);
    }

    @Override
    public void onClick(View view) {
        EditText editText = (EditText)findViewById(R.id.handleTextBox);
        DataToSend dataToSend= new DataToSend();
        dataToSend.setHandle(editText.getText().toString());
        String json = gson.toJson(dataToSend);
        String url  = getString(R.string.apiUrlHandleCheck);
        new HttpPostRequest().execute(url,json,"POST");
    }

    private AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            ListView listView = (ListView)findViewById(R.id.listOfHandles);
            DataToReceive handleFriend = (DataToReceive) listView.getItemAtPosition(position);
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            Bundle extras = new Bundle();
            String jsonReceivingHandle  = gson.toJson(userData);
            String jsonSendingHandle = gson.toJson(handleFriend);
            extras.putString(getString(R.string.receivingHandle),jsonReceivingHandle);
            extras.putString(getString(R.string.sendingHandle), jsonSendingHandle);
            intent.putExtras(extras);
            startActivity(intent);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
//        Intent serviceIntent = new Intent(this, backgroundServiceForMessages.class);
//        stopService(serviceIntent);
    }

    private class HttpPostRequest extends postRequestBaseClass {

        ProgressDialog pdLoading = new ProgressDialog(addUsernameActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdLoading.setMessage("\tLoading...");
            pdLoading.show();
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            Log.e(TAG, result);
            try {
                Gson gson = new Gson();
                DataToReceive dataToReceive = gson.fromJson(result, DataToReceive.class);
                if (dataToReceive.getHandleExists() != 0) {
                    friendList.add(dataToReceive);
                    final String updatedFile = gson.toJson(friendList, listOfStringObject);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(sharedPrefFile, updatedFile);
                    editor.apply();
                    updateUi();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.handleNotExists), Toast.LENGTH_LONG).show();
                }
            } catch (Exception exception) {
                Log.e(TAG, Arrays.toString(exception.getStackTrace()));
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            }
            Log.i(TAG, "Successful completion of post Method");
            pdLoading.dismiss();
        }
    }
}
