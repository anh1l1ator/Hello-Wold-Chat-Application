package commonsware.com.charapplication1;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private String sharedPrefFile;
    private SharedPreferences sharedPref;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void handleSignInResult(GoogleSignInResult result) throws IOException {
        Log.i(TAG, "handle Sign in Result Called");
        if (result.isSuccess() && result.getSignInAccount()!=null) {
            GoogleSignInAccount acct = result.getSignInAccount();
            Gson gson = new Gson();
            DataToSend dataToSend = new DataToSend();
            dataToSend.setIdToken(acct.getIdToken());
            final String json = gson.toJson(dataToSend);
            final String url =  getString(R.string.apiUrlSignup);
            new HttpPostRequest().execute(url,json,"POST");
        } else {
            Toast.makeText(getApplicationContext(), R.string.wentWrong, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.webId))
                .requestProfile()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        Context context = getApplicationContext();
        sharedPrefFile = getString(R.string.preference_file_key) + TAG ;
        sharedPref = context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE);
        final String defaultValue = "";
        final String handle = sharedPref.getString(sharedPrefFile, defaultValue);
        if( handle!=null && !handle.isEmpty()){
            Intent intent = new Intent(getApplicationContext(), addUsernameActivity.class);
            intent.putExtra(getString(R.string.keyToPassData), handle);
            intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            try {
                handleSignInResult(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        signIn();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private class HttpPostRequest extends postRequestBaseClass {

        ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdLoading.setMessage("\tLoading...");
            pdLoading.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                Gson gson = new Gson();
                Log.i(TAG, result);
                DataToReceive dataToReceive = gson.fromJson(result, DataToReceive.class);
                if (dataToReceive != null) {
                    Log.i(TAG, dataToReceive.getHandle());
                    Intent intent = new Intent(getApplicationContext(), addUsernameActivity.class);
                    intent.putExtra(getString(R.string.keyToPassData), result);
                    intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(sharedPrefFile, result);
                    editor.commit();
                    Log.i(TAG, "Successful completion of post Method");
                    Log.i(TAG, result);
                }
            }
            catch(Exception exception) {
                Log.e(TAG, Arrays.toString(exception.getStackTrace()));
                Toast.makeText(getApplicationContext(),R.string.wentWrong, Toast.LENGTH_LONG).show();
            }
            pdLoading.dismiss();
        }
    }

}
