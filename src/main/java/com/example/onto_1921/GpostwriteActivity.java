package com.example.onto_1921;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//게시글 작성
public class GpostwriteActivity extends AppCompatActivity {
    private EditText etPostTitle, etPostContent;
    private Button btnSubmitPost;
    private static final String urls = "http://192.168.219.104:8080";

    @Override
    protected void onCreate(Bundle savedInstatnceState){
        super.onCreate(savedInstatnceState);
        setContentView(R.layout.activity_gpostwrite);

        etPostTitle = findViewById(R.id.etPostTitle);
        etPostContent = findViewById(R.id.etPostContent);
        btnSubmitPost = findViewById(R.id.btnSubmitPost);

        btnSubmitPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etPostTitle.getText().toString();
                String content = etPostContent.getText().toString();

                Intent resultIntent = new Intent();
                resultIntent.putExtra("postTitle", title);
                resultIntent.putExtra("postContent", content);
                setResult(RESULT_OK, resultIntent);
                finish();

                new SendPostInfoTask().execute(title, content);
            }
        });
    }
    // AsyncTask를 통해 서버로 친구 정보 전송하는 클래스
    private class SendPostInfoTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                OkHttpClient client = new OkHttpClient();

                String userid = SharedPreferencesUtil.getUserId(GpostwriteActivity.this);

                // 사용자 아이디와 친구 정보를 JSON 객체에 추가
                JSONObject jsonInput = new JSONObject();
                jsonInput.put("userid", userid);
                jsonInput.put("title", strings[0]);
                jsonInput.put("content", strings[1]);

                RequestBody reqBody = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        jsonInput.toString()
                );

                Request request = new Request.Builder()
                        .post(reqBody)
                        .url(urls + "/sendpost")
                        .build();

                Response responses = client.newCall(request).execute();
                System.out.println(responses.body().string());

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}

