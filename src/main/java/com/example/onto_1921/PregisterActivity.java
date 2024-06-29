package com.example.onto_1921;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PregisterActivity extends AppCompatActivity {

    private EditText pIDEdit;
    private EditText pPWEdit;
    private EditText pPWEdit2;
    private EditText pNameEdit;
    private EditText pBirthEdit;
    private EditText pNumEdit;
    private EditText gpIDEdit;
    private TextView pPWcheckTV;
    private RadioGroup pSexRG;
    private RadioGroup pmodeRG;
    private RelativeLayout protectInfo;
    private Button regPBtn;
    private Button pidDupBtn;
    private static final String urls = "http://192.168.219.104:8080/sendprotect";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregister);

        pmodeRG = findViewById(R.id.protectMode);
        pIDEdit = findViewById(R.id.protectId);
        pPWEdit = findViewById(R.id.protectPW);
        pPWEdit2 = findViewById(R.id.protectPW2);
        pNameEdit = findViewById(R.id.protectName);
        pBirthEdit = findViewById(R.id.protectBirth);
        pNumEdit = findViewById(R.id.protectNum);
        regPBtn = findViewById(R.id.regPBtn);
        pSexRG = findViewById(R.id.protectSex);
        gpIDEdit = findViewById(R.id.gprotectId);

        pidDupBtn = findViewById(R.id.pidDupBtn);

        pidDupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 아이디 입력란에서 아이디를 가져옴
                String id = pIDEdit.getText().toString();
                // AsyncTask를 실행하여 중복 여부를 확인하는 요청을 보냄
                CheckDuplicateTask task = new CheckDuplicateTask();
                task.execute(id);
            }
        });
        // 아이디 입력란에 대한 텍스트 변경 감지기 추가
        pIDEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkIDValidity(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 비밀번호 입력란에 대한 텍스트 변경 감지기 추가
        pPWEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkPasswordValidity(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 비밀번호 확인 입력란에 대한 텍스트 변경 감지기 추가
        pPWEdit2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkPasswordMatch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void checkIDValidity(String id) {
        TextView idWarning = findViewById(R.id.pIDwarning);
        if (id.length() < 8 || !id.matches(".*\\d.*") || !id.matches(".*[a-zA-Z].*")) {
            // 조건 미충족: 아이디에 대한 경고 표시
            idWarning.setVisibility(View.VISIBLE);
            idWarning.setText("아이디는 영문자와 숫자를 포함하여 8자 이상이어야 합니다.");
        } else {
            // 조건 충족: 경고문 숨김
            idWarning.setVisibility(View.GONE);
        }
    }

    private void checkPasswordValidity(String password) {
        TextView passwordWarning = findViewById(R.id.pPWwarning);
        if (password.length() < 4) {
            // 조건 미충족: 비밀번호에 대한 경고 표시
            passwordWarning.setVisibility(View.VISIBLE);
            passwordWarning.setText("비밀번호는 4자 이상이어야 합니다.");
        } else {
            // 조건 충족: 경고문 숨김
            passwordWarning.setVisibility(View.GONE);
        }
    }

    private void checkPasswordMatch(String password2) {
        TextView passwordMatchWarning = findViewById(R.id.pPWmatchwarning);
        String password1 = pPWEdit.getText().toString();
        if (!password1.equals(password2)) {
            // 조건 미충족: 비밀번호 확인과 비밀번호가 일치하지 않음을 알리는 경고 표시
            passwordMatchWarning.setVisibility(View.VISIBLE);
            passwordMatchWarning.setText("비밀번호가 일치하지 않습니다.");
        } else {
            // 조건 충족: 경고문 숨김
            passwordMatchWarning.setVisibility(View.GONE);
        }
    }

    public String getMode() {
        RadioButton selectedRadioButton = findViewById(pmodeRG.getCheckedRadioButtonId());
        String mode = selectedRadioButton.getText().toString();
        if ("아동".equals(mode)) {
            return "child";
        } else if ("노인".equals(mode)) {
            return "old";
        } else {
            return "";
        }
    }

    public String getSex() {
        RadioButton selectedRadioButton = findViewById(pSexRG.getCheckedRadioButtonId());
        return selectedRadioButton.getText().toString();
    }
    public void ClickButton1(View v){
        sendServer();
    }
    public void sendServer(){ // 서버로 전송하기위한 함수
        class sendData extends AsyncTask<Void, Void, String> { // 쓰레드 만들기

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                // 서버로 데이터를 보냈으므로, 다음 화면으로 이동하는 코드를 추가합니다.
                Intent intent = new Intent(PregisterActivity.this, LoginActivity.class); // NextActivity는 다음 화면의 액티비티 이름입니다. 실제로는 알맞는 액티비티 이름으로 바꿔주세요.
                startActivity(intent);
                finish();
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
            protected String doInBackground(Void... voids) {

                try {
                    OkHttpClient client = new OkHttpClient();
                    // okHttpClient 호출
                    JSONObject jsonInput = new JSONObject();
                    // Json객체 생성
                    jsonInput.put("protect_mode", getMode());
                    jsonInput.put("protect_id",  pIDEdit.getText().toString());
                    jsonInput.put("p_guardian_id", gpIDEdit.getText().toString());
                    jsonInput.put("protect_pw",  pPWEdit.getText().toString());
                    jsonInput.put("protect_name", pNameEdit.getText().toString());
                    jsonInput.put("protect_birth", pBirthEdit.getText().toString());
                    jsonInput.put("protect_sex", getSex());
                    jsonInput.put("protect_phone", pNumEdit.getText().toString());


                    // json객체에 데이터 추가

                    RequestBody reqBody = RequestBody.create(
                            MediaType.parse("application/json; charset=utf-8"),
                            jsonInput.toString()
                    );

                    Request request = new Request.Builder()
                            .post(reqBody)
                            .url(urls)
                            .build();

                    Response responses = null;
                    responses = client.newCall(request).execute();
                    System.out.println(responses.body().string());

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
        sendData sendData = new sendData();
        sendData.execute();
    }

    protected void checkDuplicateId() {
        // 아이디 입력란에서 아이디를 가져옴
        String id = pIDEdit.getText().toString();
        // AsyncTask를 실행하여 중복 여부를 확인하는 요청을 보냄
        CheckDuplicateTask task = new CheckDuplicateTask();
        task.execute(id);
    }

    // AsyncTask 클래스를 정의하여 중복 여부를 확인하는 작업을 수행
    private class CheckDuplicateTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String id = params[0];
            return checkDuplicateIdOnServer(id);
        }

        @Override
        protected void onPostExecute(Boolean isDuplicate) {
            super.onPostExecute(isDuplicate);

            if (isDuplicate != null) {
                if (isDuplicate) {
                    // 중복된 아이디일 경우
                    Toast.makeText(PregisterActivity.this, "중복된 아이디입니다.", Toast.LENGTH_SHORT).show();
                } else {
                    // 중복되지 않은 아이디일 경우
                    Toast.makeText(PregisterActivity.this, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 서버 응답 실패
                Toast.makeText(PregisterActivity.this, "서버 응답 실패", Toast.LENGTH_SHORT).show();
            }
        }

        // 서버로 아이디 중복 여부를 확인하는 메서드
        private boolean checkDuplicateIdOnServer(String id) {
            OkHttpClient client = new OkHttpClient();
            // 요청할 JSON 데이터 생성
            JSONObject jsonInput = new JSONObject();
            try {
                jsonInput.put("userid", id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // 서버에 전송할 요청 생성
            RequestBody reqBody = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    jsonInput.toString()
            );
            Request request = new Request.Builder()
                    .post(reqBody)
                    .url("http://172.20.10.2:5000/checkduplicate")
                    .build();
            // 서버로 요청을 보내고 응답 처리
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    // 응답이 성공적으로 도착했을 때
                    JSONObject jsonResponse = new JSONObject(response.body().string());
                    return jsonResponse.getBoolean("is_duplicate");
                } else {
                    // 응답이 실패했을 때
                    return false;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}