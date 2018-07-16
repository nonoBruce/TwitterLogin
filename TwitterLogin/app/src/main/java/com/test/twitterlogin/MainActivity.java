package com.test.twitterlogin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
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
import com.twitter.sdk.android.core.services.AccountService;

import retrofit2.Call;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private TwitterLoginButton mLoginButton;
    private TwitterAuthClient mTwitterAuthClient;

    private long mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //要放到setContentView之前，不然twitter会灰色，或者放到Application里面也可以。
        initTwitter();

        setContentView(R.layout.activity_main);

        initUI();

        twitterButton();
    }

    private void initTwitter() {
        String key = getString(R.string.twitter_consumer_key);
        String secret = getString(R.string.twitter_consumer_secret);

        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.INFO))
                .twitterAuthConfig(new TwitterAuthConfig(key, secret))
                .debug(false)
                .build();
        Twitter.initialize(config);
    }

    private void initUI() {
        mLoginButton = (TwitterLoginButton) findViewById(R.id.login_button);

        findViewById(R.id.login_customer_button).setOnClickListener(this);
        findViewById(R.id.user_info_button).setOnClickListener(this);
        findViewById(R.id.user_logout_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.login_customer_button: {
                //自定义的twitter login button
                authTwitter();
                break;
            }
            case R.id.user_info_button: {
                //用户信息
                twitterUserInfo();
                break;
            }

            case R.id.user_logout_button: {
                logout();
                break;
            }

            default:{
                break;
            }
        }
    }


    private void twitterButton() {
        mLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
// Do something with result, which provides a TwitterSession for making API calls
// result里面包含了用户的信息，我们可以从中取出token，tokenSecret
// 如果我们有自己的后台服务器，发送这两个到我们自己的后台，后台再去验证）
                TwitterAuthToken authToken = result.data.getAuthToken();

                String token = authToken.token;
                String tokenSecret = authToken.secret;
                String userName = result.data.getUserName();
                String userId = result.data.getUserId() + "";

                Log.i("token", token);
                Log.i("tokenSecret", tokenSecret);
                Log.i("userName", userName);
                Log.i("userId", userId);

                String message = "userName = "+userName+"\t"+"userId = "+userId+"\t"+"token = "+token;
                Toast.makeText(MainActivity.this,message,Toast.LENGTH_SHORT).show();

                mUserId = result.data.getUserId();

            }

            @Override
            public void failure(TwitterException exception) { // Do something on failure
                exception.printStackTrace();
            }
        });
    }

    //自定义的twitter登录，使用TwitterAuthClient
    private void authTwitter() {
        mTwitterAuthClient = new TwitterAuthClient();
        mTwitterAuthClient.authorize(this, new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                TwitterAuthToken authToken = result.data.getAuthToken();

                String token = authToken.token;
                String tokenSecret = authToken.secret;
                String userName = result.data.getUserName();
                String userId = result.data.getUserId() + "";

                Log.i("token", token);
                Log.i("tokenSecret", tokenSecret);
                Log.i("userName", userName);
                Log.i("userId", userId);

                String message = "userName = "+userName+"\t"+"userId = "+userId+"\t"+"token = "+token;
                Toast.makeText(MainActivity.this,message,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(TwitterException exception) {
                exception.printStackTrace();
            }
        });

    }

    //注销twitter
    private void logout() {
        TwitterCore.getInstance().getSessionManager().clearActiveSession();
    }


    //获取用户信息
    private void twitterUserInfo() {

        final TwitterSession activeSession = TwitterCore.getInstance().getSessionManager().getActiveSession();

        if (activeSession == null) {
            String message = "用户还没有登录";
            Toast.makeText(MainActivity.this,message,Toast.LENGTH_SHORT).show();
            return;
        }
        TwitterApiClient client = new TwitterApiClient(activeSession);
        AccountService accountService = client.getAccountService();
        Call<User> show = accountService.verifyCredentials(false, false, true);
        show.enqueue(new Callback<User>() {
            @Override
            public void success(Result<User> result) {
                User data = result.data;
                String profileImageUrl = data.profileImageUrl.replace("_normal", "");
                String idStr = data.idStr;
                String name = data.name;
                String email = data.email;
                String description = data.description;

                Log.i("profileImageUrl", profileImageUrl);
                Log.i("idStr", idStr);
                Log.i("name", name);

                //邮箱信息需要在permissions里面勾选"Request email addresses from users"
                Log.i("email", email);

                Log.i("description", description);

                String message = "userName = "+name+"\t"+"idStr = "+idStr+"\t"+"email = "+email;
                Toast.makeText(MainActivity.this,message,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(TwitterException exception) {
                exception.printStackTrace();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        if (mLoginButton != null) {
            mLoginButton.onActivityResult(requestCode, resultCode, data);

        }

        if (mTwitterAuthClient != null) {
            mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);

        }

    }


}
