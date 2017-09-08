package commonsware.com.charapplication1;

import android.content.Intent;
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
import java.util.concurrent.ExecutionException;

class DataToSend {
    public String idToken;
    public DataToSend(String token) {
        idToken = token;
    }
    public String getidToken(){
        return idToken;
    }
    public void setidToken(String token){
        idToken=token;
    }
};

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void handleSignInResult(GoogleSignInResult result) throws IOException {
        Log.i(TAG, "handle Sign in Result Called");
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            Gson gson = new Gson();
            DataToSend dataToSend = new DataToSend(acct.getIdToken());
            final String json = gson.toJson(dataToSend);
            final String url =  getString(R.string.apiUrl);
            String res;
            try {
                res = new HttpPostRequest().execute(url,json).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                res = getString(R.string.wentWrong);
            } catch (ExecutionException e) {
                e.printStackTrace();
                res = getString(R.string.wentWrong);
            }
            Log.i(TAG, "Successful completion of post Method");
            Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
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

}
