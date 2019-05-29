package com.aritra.loginappdemo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import java.util.Arrays;

import retrofit2.Call;


public class MainActivity extends AppCompatActivity {

    private CallbackManager mCallbackManager;
    private static final String EMAIL = "email";
    private static final String TAG = "FBLog";
    private FirebaseAuth mAuth;
    private Button fb_button;
    private TwitterLoginButton twitter_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig("PUBLIC_KEY ",
                "SECRET_KEY");
        TwitterConfig twitterConfig = new TwitterConfig.Builder(this)
                .twitterAuthConfig(authConfig)
                .build();
        Twitter.initialize(twitterConfig);
        setContentView(R.layout.activity_main);

         fb_button = findViewById(R.id.btnFacebook);
         twitter_button = findViewById(R.id.btn_twitter);
         mCallbackManager = CallbackManager.Factory.create();

         fb_button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("email", "public_profile"));
                 LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                     @Override
                     public void onSuccess(LoginResult loginResult) {
                         Log.d(TAG, "facebook:onSuccess:" + loginResult);
                         handleFacebookAccessToken(loginResult.getAccessToken());
                         Toast.makeText(MainActivity.this,"Facebook:Success",Toast.LENGTH_SHORT).show();
                     }

                     @Override
                     public void onCancel() {
                         Log.d(TAG, "facebook:onCancel");
                         Toast.makeText(MainActivity.this,"Facebook:Cancel",Toast.LENGTH_SHORT).show();
                         // ...
                     }

                     @Override
                     public void onError(FacebookException error) {
                         Log.d(TAG, "facebook:onError", error);
                         Toast.makeText(MainActivity.this,"ERROR",Toast.LENGTH_SHORT).show();
                         // ...
                     }
                 });
             }
         });

        twitter_button = findViewById(R.id.btn_twitter);
        twitter_button.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "twitterLogin:success" + result);
                Toast.makeText(getApplicationContext(),"Twitter:Success", Toast.LENGTH_SHORT).show();
//                handleTwitterSession(result.data);
                TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                TwitterAuthToken authToken = session.getAuthToken();
                String token = authToken.token;
                String secret = authToken.secret;


                Call<User> user = TwitterCore.getInstance().getApiClient().getAccountService().verifyCredentials(true, true,true);
                user.enqueue(new Callback<User>() {
                    @Override
                    public void success(Result<User> userResult) {
                        String name = userResult.data.name;
                        String email = userResult.data.email;

                        // _normal (48x48px) | _bigger (73x73px) | _mini (24x24px)
                        String photoUrlNormalSize   = userResult.data.profileImageUrl;
                        String photoUrlBiggerSize   = userResult.data.profileImageUrl.replace("_normal", "_bigger");
                        String photoUrlMiniSize     = userResult.data.profileImageUrl.replace("_normal", "_mini");
                        String photoUrlOriginalSize = userResult.data.profileImageUrl.replace("_normal", "");

                        Intent intent = new Intent(MainActivity.this,TwitterActivity.class);
                        intent.putExtra("username", name);
                        intent.putExtra("email", email);
                        intent.putExtra("picture", photoUrlNormalSize);
                        startActivity(intent);
                    }

                    @Override
                    public void failure(TwitterException exc) {
                        Log.d("TwitterKit", "Verify Credentials Failure", exc);
                    }
                });


                //Calling login method and passing twitter session
//                TwitterAuthClient authClient = new TwitterAuthClient();
//                authClient.requestEmail(session, new Callback<String>() {
//                    @Override
//                    public void success(Result<String> result) {
//                        // Do something with the result, which provides the email address
//                        Toast.makeText(getApplicationContext(),result.data, Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void failure(TwitterException exception) {
//                        // Do something on failure
//                    }
//                });

            }

            @Override
            public void failure(TwitterException exception) {
                Log.w(TAG, "twitterLogin:failure", exception);
                Toast.makeText(getApplicationContext(),"Twitter:Failure", Toast.LENGTH_SHORT).show();

            }
        });


// Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        twitter_button.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
//        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast toast = Toast.makeText(MainActivity.this,"USER: " + user.getDisplayName(),Toast.LENGTH_LONG);
                            toast.show();
//                            finish();
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void handleTwitterSession(final TwitterSession session) {
        Log.d(TAG, "handleTwitterSession:" + session);

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(MainActivity.this,"USER: " + user.getDisplayName(),Toast.LENGTH_LONG).show();

//                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI();
                        }

                        // ...
                    }
                });
    }

    private void updateUI(){

        Intent intent = new Intent(MainActivity.this,fb_activity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null)
            updateUI();
    }
}
