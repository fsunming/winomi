package com.example.onto_1921;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OldActivity extends AppCompatActivity implements OnMapReadyCallback {
    private FusedLocationProviderClient fusedLocationClient;
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1001;
    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private TextView guardianInfoTextView;
    private static final String urls = "http://192.168.219.104:8080";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old);

        guardianInfoTextView = findViewById(R.id.guardianInfoTextView);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // LocationRequest 초기화
        locationRequest = LocationRequest.create()
                .setInterval(60000) // 1분마다 위치 업데이트
                .setFastestInterval(60000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // 위치 업데이트 시작
        startLocationUpdates();

        // 프래그먼트에서 지도를 가져와 준비가 되면 콜백을 받습니다.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // 사용자 아이디(userid)를 서버로 보내어 관리자 정보를 가져오는 메서드 호출
        new FetchGuardianInfoTask().execute();
    }

    // 토스트 메세지 출력 메소드
    private void showToastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 위치 권한이 부여되어 있는지 확인
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        // 현재 위치 가져오기
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                //mMap.addMarker(new MarkerOptions().position(currentLocation).title("현재 위치"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                // 현재 위치의 위도와 경도를 토스트 메시지로 표시
                Toast.makeText(OldActivity.this, "현재 위치 - 위도: " + location.getLatitude() + ", 경도: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 허용됨
                onMapReady(mMap);
            } else {
                // 권한 거부됨
                showToastMessage("위치 권한이 필요합니다.");
            }
        }
    }

    // 위치 업데이트 시작 메서드
    private void startLocationUpdates() {
        // 위치 권한이 있는지 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            // 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    // 현재 위치 마커를 추적하는 변수 추가
    private Marker currentLocationMarker;


    // LocationCallback 구현
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            // 새로운 위치 업데이트가 있을 때 호출됨
            if (locationResult != null) {
                Location location = locationResult.getLastLocation();
                // 위치 정보를 데이터베이스에 저장할 수 있습니다.
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    sendLocationToServer(latitude, longitude);

                    // 현재 위치 생성
                    LatLng currentLocation = new LatLng(latitude, longitude);

                    if (currentLocationMarker == null) {
                        // 마커 생성
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(currentLocation) // 현재 위치로 설정
                                .title("현재 위치"); // 제목 설정 등

                        // 마커 추가 및 currentLocationMarker에 할당
                        currentLocationMarker = mMap.addMarker(markerOptions);
                    } else {
                        // 현재 위치 마커 업데이트
                        currentLocationMarker.setPosition(currentLocation);
                    }

                    // 이전 위치와 현재 위치 사이의 거리 계산
                    if (currentLocationMarker != null) {
                        LatLng previousLocation = currentLocationMarker.getPosition();
                        float[] distance = new float[1];
                        Location.distanceBetween(previousLocation.latitude, previousLocation.longitude, currentLocation.latitude, currentLocation.longitude, distance);

                        // 거리가 일정 값 이상이면 지도 이동
                        if (distance[0] > 1) { // 예시 거리: 1 미터
                            // 이전 위치와 현재 위치의 중간 지점 계산하여 지도 이동
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(previousLocation);
                            builder.include(currentLocation);
                            LatLngBounds bounds = builder.build();
                            int padding = 100; // px
                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                            mMap.animateCamera(cu);
                        }
                    }
                }
            }
        }
    };

    // 서버로 위치 정보 전송
    private void sendLocationToServer(double latitude, double longitude) {
        // 서버에 위치 정보 전송
        new SendLocationTask().execute(latitude, longitude);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    // 현재 위치 전송 AsyncTask
    public class SendLocationTask extends AsyncTask<Double, Void, String> {
        @Override
        protected String doInBackground(Double... params) {
            try {
                OkHttpClient client = new OkHttpClient();

                String userid = SharedPreferencesUtil.getUserId(OldActivity.this);

                double latitude = params[0];
                double longitude = params[1];
                // 현재 시간 가져오기
                long now = System.currentTimeMillis();
                // Date 생성하기
                Date date = new Date(now);
                // 가져오고 싶은 형식으로 가져오기
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateTime = sdf.format(date);

                // JSON 데이터 생성
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("userid", userid);
                jsonParam.put("date_time", dateTime);
                jsonParam.put("latitude", latitude);
                jsonParam.put("longitude", longitude);

                RequestBody reqBody = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        jsonParam.toString()
                );

                Request request = new Request.Builder()
                        .post(reqBody)
                        .url(urls + "/sendlocation")
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
                // 성공했을 때 메시지를 표시합니다.
                showToastMessage("위치 등록 성공");
            } else {
                // 실패했을 경우
                showToastMessage("위치 등록 실패");
            }
        }
    }

    private class FetchGuardianInfoTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String userid = SharedPreferencesUtil.getUserId(OldActivity.this);
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
                    .url(urls + "/getguardianinfo") // 서버의 관리자 정보를 가져오는 엔드포인트 URL
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
                    String guardianName = jsonResponse.getString("name");
                    String guardianPhone = jsonResponse.getString("phone");
                    String guardianAddress = jsonResponse.getString("address");

                    // 검색된 관리자 정보를 화면에 표시하는 메서드 호출
                    displayGuardianInfo(guardianName, guardianPhone, guardianAddress);
                } catch (JSONException e) {
                    e.printStackTrace();
                    // JSON 파싱에 실패한 경우, 사용자에게 알림을 줍니다.
                    showToastMessage("관리자 정보를 파싱하는 도중 오류가 발생했습니다."+ e.getMessage());
                }
            } else {
                // 서버로부터 응답이 null인 경우, 사용자에게 알림을 줍니다.
                showToastMessage("서버로부터 응답을 받을 수 없습니다.");
            }
        }
    }

    private void displayGuardianInfo(String name, String phone, String address) {
        // 보호자 정보를 HTML 형식으로 포맷팅
        String formattedText = "<html><body>" +
                "<div style='text-align: center;'>" +
                "<p style='font-size: 18px; font-weight: bold;'>보호자 정보</p>" +
                "<p>이름: " + name + "</p>" +
                "<p>연락처: " + phone + "</p>" +
                "<p>주소: " + address + "</p>" +
                "</div></body></html>";

        // HTML 형식의 텍스트를 텍스트뷰에 설정
        guardianInfoTextView.setText(Html.fromHtml(formattedText));
        // TextView에서 HTML 태그를 사용하기 위해 setMovementMethod 메서드를 호출하여 LinkMovementMethod.getInstance()를 전달합니다.
        guardianInfoTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
