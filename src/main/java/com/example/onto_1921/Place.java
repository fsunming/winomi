package com.example.onto_1921;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
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

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Place extends AppCompatActivity {

    private Geocoder geocoder;
    private Button addplace;
    private TextView placeTextView;
    private static final String urls = "http://192.168.219.104:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place); // 레이아웃 파일 이름을 place로 변경

        // XML에서 UI 요소 찾기
        addplace = findViewById(R.id.add_place_button); // 버튼 ID 변경

        // 장소 추가 버튼 클릭 리스너 설정
        addplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPlaceDialog();
            }
        });

        placeTextView = findViewById(R.id.placeTextView); // TextView ID 변경

        // 서버로부터 장소 가져오기
        new FetchPlaceInfoTask().execute();


    }


    private void showAddPlaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("장소 추가");

        View viewInflated = getLayoutInflater().inflate(R.layout.add_place, null);

        final EditText inputName = viewInflated.findViewById(R.id.place_name); // EditText ID 변경
        final EditText inputAdd = viewInflated.findViewById(R.id.place_add); // EditText ID 변경
        final EditText inputPhone = viewInflated.findViewById(R.id.place_phone); // EditText ID 변경
        final Button saveButton = viewInflated.findViewById(R.id.submit);
        final Button closeButton = viewInflated.findViewById(R.id.close);
        // 저장 버튼 비활성화
        saveButton.setEnabled(false);

        // 이름 입력 감지
        inputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFieldsForEmptyValues(inputName, saveButton);
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
                String address = inputAdd.getText().toString().trim();
                String phone = inputPhone.getText().toString().trim();

                // AsyncTask를 통해 서버로 장소 정보 전송
                new SendPlaceInfoTask().execute(name, address, phone);

                new FetchPlaceInfoTask().execute();

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


    private void checkFieldsForEmptyValues(EditText inputName, Button saveButton) {
        String name = inputName.getText().toString().trim();

        // 이름 입력되었을 때 저장 버튼 활성화
        saveButton.setEnabled(!name.isEmpty());
    }

    // 토스트 메세지 출력 메소드
    private void showToastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // AsyncTask를 통해 서버로 장소 정보 전송하는 클래스
    private class SendPlaceInfoTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                OkHttpClient client = new OkHttpClient();

                String userid = SharedPreferencesUtil.getUserId(Place.this);

                String placeName = strings[0];
                String placeAddress = strings[1];
                String placePhone = strings[2];

                // 주소를 위도와 경도로 변환
                LatLng location = getLocationFromAddress(placeAddress);

                if (location != null) {
                    // 사용자 아이디와 장소 정보 및 위도, 경도를 JSON 객체에 추가
                    JSONObject jsonInput = new JSONObject();
                    jsonInput.put("userid", userid);
                    jsonInput.put("place_name", placeName);
                    jsonInput.put("place_address", placeAddress);
                    jsonInput.put("place_phone", placePhone);
                    jsonInput.put("latitude", location.latitude);
                    jsonInput.put("longitude", location.longitude);

                    RequestBody reqBody = RequestBody.create(
                            MediaType.parse("application/json; charset=utf-8"),
                            jsonInput.toString()
                    );

                    Request request = new Request.Builder()
                            .post(reqBody)
                            .url(urls + "/sendplaces")
                            .build();

                    Response responses = client.newCall(request).execute();
                    if (responses.isSuccessful()) {
                        return "success"; // 성공했음을 onPostExecute로 전달

                    }
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
                // 장소 등록이 성공했을 때 메시지를 표시합니다.
                showToastMessage("장소 등록 성공");
            } else {
                // 실패했을 경우
                showToastMessage("장소 등록 실패");
            }
        }
    }


    // 주소를 위도와 경도로 변환하는 메서드
   /* private LatLng getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return p1;
    }*/

    private LatLng getLocationFromAddress(String strAddress) {
        List<Address> addressList;
        geocoder = new Geocoder(this);
        try {
            addressList = geocoder.getFromLocationName(strAddress, 5);
            if (addressList == null || addressList.isEmpty()) {
                return null;
            }
            Address location = addressList.get(0);
            return new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



    private class FetchPlaceInfoTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String userid = SharedPreferencesUtil.getUserId(Place.this);;
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
                    .url(urls+"/setplaces") // URL 변경
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
                    JSONArray placeArray = jsonResponse.getJSONArray("places"); // 키 이름 변경
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < placeArray.length(); i++) {
                        JSONObject placeObject = placeArray.getJSONObject(i);
                        String name = placeObject.getString("place_name"); // 키 이름 변경
                        String address = placeObject.getString("place_address"); // 키 이름 변경
                        String phone = placeObject.getString("place_phone"); // 키 이름 변경

                        stringBuilder.append("<").append(name).append(">").append("\n");
                        stringBuilder.append("주소: ").append(address).append("\n");
                        stringBuilder.append("전화번호: ").append(phone).append("\n\n");
                    }
                    placeTextView.setText(stringBuilder.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    // JSON 파싱에 실패한 경우, 사용자에게 알림을 줍니다.
                    showToastMessage("장소 정보를 가져오는 도중 오류가 발생했습니다.");
                }
            } else {
                // 서버로부터 응답이 null인 경우, 사용자에게 알림을 줍니다.
                showToastMessage("서버로부터 응답을 받을 수 없습니다.");
            }
        }

    }
}