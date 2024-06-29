package com.example.onto_1921;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Friend extends AppCompatActivity {

    private Button addfriend;
    private TextView textViewFriendInfo;
    private static final String urls = "http://192.168.219.104:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend);

        // XML에서 UI 요소 찾기
        addfriend = findViewById(R.id.add_friend_button);

        // 친구 추가 버튼 클릭 리스너 설정
        addfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddFriendDialog();
            }
        });

        textViewFriendInfo = findViewById(R.id.textViewFriendInfo);

        // 서버로부터 친구 정보 가져오기
        new FetchFriendInfoTask().execute();

    }


    private void showAddFriendDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("친구 추가");

        View viewInflated = getLayoutInflater().inflate(R.layout.add_friend, null);

        final EditText inputName = viewInflated.findViewById(R.id.friend_name);
        final EditText inputPhone = viewInflated.findViewById(R.id.friend_phone);
        final Button saveButton = viewInflated.findViewById(R.id.submit);
        final Button closeButton = viewInflated.findViewById(R.id.close);
        // 저장 버튼 비활성화
        saveButton.setEnabled(false);

        // 이름과 전화번호 입력 감지
        inputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFieldsForEmptyValues(inputName, inputPhone, saveButton);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        inputPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFieldsForEmptyValues(inputName, inputPhone, saveButton);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        builder.setView(viewInflated);

        final AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputName.getText().toString().trim();
                String phone = inputPhone.getText().toString().trim();

                // AsyncTask를 통해 서버로 친구 정보 전송
                new SendFriendInfoTask().execute(name, phone);

                // 새로운 친구 정보를 가져와서 표시
                new FetchFriendInfoTask().execute();

                dialog.dismiss();
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show(); // 다이얼로그를 표시
    }

    private void checkFieldsForEmptyValues(EditText inputName, EditText inputPhone, Button saveButton) {
        String name = inputName.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();

        // 이름과 전화번호가 모두 입력되었을 때 저장 버튼 활성화
        saveButton.setEnabled(!name.isEmpty() && !phone.isEmpty());
    }

    // 토스트 메세지 출력 메소드
    private void showToastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // AsyncTask를 통해 서버로 친구 정보 전송하는 클래스
    private class SendFriendInfoTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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

                String userid = SharedPreferencesUtil.getUserId(Friend.this);

                // 사용자 아이디와 친구 정보를 JSON 객체에 추가
                JSONObject jsonInput = new JSONObject();
                jsonInput.put("userid", userid);
                jsonInput.put("friend_name", strings[0]); // 친구 이름
                jsonInput.put("friend_phone", strings[1]); // 친구 전화번호

                RequestBody reqBody = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        jsonInput.toString()
                );

                Request request = new Request.Builder()
                        .post(reqBody)
                        .url(urls + "/sendfriends")
                        .build();

                Response responses = client.newCall(request).execute();
                if (responses.isSuccessful()) {
                    return "success"; // 성공했음을 onPostExecute로 전달
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null && result.equals("success")) {
                // 친구 등록이 성공했을 때 메시지를 표시합니다.
                showToastMessage("친구 등록 성공");
            } else {
                // 실패했을 경우
                showToastMessage("친구 등록 실패");
            }
        }
    }


    private class FetchFriendInfoTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String userid = SharedPreferencesUtil.getUserId(Friend.this);;
            OkHttpClient client = new OkHttpClient();
            JSONObject jsonInput = new JSONObject();
            try {
                jsonInput.put("userid", userid);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody reqBody = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    jsonInput.toString()
            );
            Request request = new Request.Builder()
                    .post(reqBody)
                    .url(urls+"/setfriends") // 서버의 친구 정보를 가져오는 엔드포인트 URL
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    return response.body().string();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String responseString) {
            super.onPostExecute(responseString);
            if (responseString != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(responseString);
                    JSONArray friendArray = jsonResponse.getJSONArray("friends");

                    StringBuilder stringBuilder = new StringBuilder();

                    for (int i = 0; i < friendArray.length(); i++) {
                        JSONObject friendObject = friendArray.getJSONObject(i);
                        String name = friendObject.getString("friend_name");
                        String phone = friendObject.getString("friend_phone");

                        // 개별적으로 이름과 전화번호를 처리하여 추가합니다.
                        stringBuilder.append("이름: ").append(name).append("\n");
                        stringBuilder.append("전화번호: ").append(phone).append("\n\n");
                    }

                    textViewFriendInfo.setText(stringBuilder.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    // JSON 파싱에 실패한 경우, 사용자에게 알림을 줍니다.
                    showToastMessage("친구 정보를 가져오는 도중 오류가 발생했습니다.");
                }
            } else {
                // 서버로부터 응답이 null인 경우, 사용자에게 알림을 줍니다.
                showToastMessage("서버로부터 응답을 받을 수 없습니다.");
            }
        }


    }
}