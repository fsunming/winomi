package com.example.onto_1921;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public  class LoginActivity extends Activity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private static final String urls = "http://192.168.219.104:8080/login";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.loginID);
        passwordEditText = findViewById(R.id.loginPW);
        loginButton = findViewById(R.id.loginBtn);

        // 로그인 버튼 클릭 리스너 설정
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 아이디와 비밀번호 가져오기
                String userid = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                new LoginAsyncTask(LoginActivity.this).execute(userid, password);
            }
        });

        // 회원가입 버튼 찾기
        Button signUpButton = findViewById(R.id.regBtn);
        // 회원가입 버튼 클릭 리스너 설정
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SignUpActivity로 이동하는 인텐트 생성
                Intent signUpIntent = new Intent(LoginActivity.this, ChoiceActivity.class);
                startActivity(signUpIntent);
            }
        });
    }

    public class LoginAsyncTask extends AsyncTask<String, Void, Boolean> {

        @SuppressLint("StaticFieldLeak")
        private Context mContext;

        public LoginAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String userid = params[0];
            String password = params[1];

            OkHttpClient client = new OkHttpClient();
            JSONObject jsonInput = new JSONObject();
            try {
                jsonInput.put("userid", userid);
                jsonInput.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            RequestBody reqBody = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    jsonInput.toString()
            );
            Request request = new Request.Builder()
                    .post(reqBody)
                    .url(urls)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    // 네트워크 오류 처리
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            JSONObject json = new JSONObject(responseBody);
                            String table = json.optString("table", "");
                            String userid = json.optString("userid", "");

                            // 서버 응답 코드가 200인 경우
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    SharedPreferencesUtil.saveUserId(LoginActivity.this, userid);

                                    Toast.makeText(LoginActivity.this, "로그인 성공" + userid, Toast.LENGTH_SHORT).show();
                                    // 다음 화면으로 이동
                                    Intent intent;
                                    if ("guardian".equals(table)) {
                                        intent = new Intent(LoginActivity.this, GuardianActivity.class);
                                    } else if ("child".equals(table)) {
                                        intent = new Intent(LoginActivity.this, ChildActivity.class);
                                    } else if ("old".equals(table)) {
                                        intent = new Intent(LoginActivity.this, OldActivity.class);
                                    } else {
                                        return;
                                    }
                                    startActivity(intent);
                                }
                            });
                        } else {
                            // 서버 응답 코드가 401인 경우
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "아이디 또는 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            return null;
        }
    }
}

