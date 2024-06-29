package com.example.onto_1921;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChildActivity extends AppCompatActivity implements OnMapReadyCallback {
    private FusedLocationProviderClient fusedLocationClient;
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1001;
    private GoogleMap googleMap;
    private LocationRequest locationRequest;
    private TextView guardianInfoTextView;
    private Button friendbtn, placebtn;
    private static final String urls = "http://192.168.219.104:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        guardianInfoTextView = findViewById(R.id.guardianInfoTextView);
        friendbtn = findViewById(R.id.friendButton);
        placebtn = findViewById(R.id.placeButton);

        Button viewLocationButton = findViewById(R.id.button_view_location);
        viewLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChildActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000); // 1분
        locationRequest.setFastestInterval(30000); // 30초
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // 위치 업데이트 시작
        startLocationUpdates();

        // 프래그먼트에서 지도를 가져와 준비가 되면 콜백을 받습니다.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        friendbtn.setOnClickListener(v -> {
            Intent intent = new Intent(ChildActivity.this, Friend.class);
            startActivity(intent);
        });

        placebtn.setOnClickListener(v -> {
            Intent intent = new Intent(ChildActivity.this, Place.class);
            startActivity(intent);
        });

        // 사용자 아이디(userid)를 서버로 보내어 관리자 정보를 가져오는 메서드 호출
        new FetchGuardianInfoTask().execute();

        // 서버로부터 장소 가져오기
        new FetchPlacesTask().execute();
    }

    // 토스트 메세지 출력 메소드
    private void showToastMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);

            // 현재 위치 가져오기
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    //MarkerOptions markerOptions = new MarkerOptions().position(currentLatLng).title("현재 위치");
                    //googleMap.addMarker(markerOptions);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                } else {
                    showToastMessage("현재 위치를 가져올 수 없습니다.");
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 허용됨
                onMapReady(googleMap);
            } else {
                // 권한 거부됨
                showToastMessage("위치 권한이 필요합니다.");
            }
        }
    }

    // 위치 업데이트 시작 메서드
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    // LocationCallback 구현
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                // 위치를 서버로 전송
                sendLocationToServer(location);
            }
        }
    };

    // 서버로 위치 정보 전송
    private void sendLocationToServer(Location location) {
        String userId = SharedPreferencesUtil.getUserId(this);
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonInput = new JSONObject();
        // 현재 시간 가져오기
        long now = System.currentTimeMillis();
        // Date 생성하기
        Date date = new Date(now);
        // 가져오고 싶은 형식으로 가져오기
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime = sdf.format(date);
        try {
            jsonInput.put("userid", userId);
            jsonInput.put("date_time", dateTime);
            jsonInput.put("latitude", location.getLatitude());
            jsonInput.put("longitude", location.getLongitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody reqBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonInput.toString());
        Request request = new Request.Builder()
                .post(reqBody)
                .url(urls + "/sendlocation")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
            }
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    private class FetchGuardianInfoTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String userid = SharedPreferencesUtil.getUserId(ChildActivity.this);
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

    // AsyncTask를 통해 서버로부터 장소 정보를 가져오는 클래스
    private class FetchPlacesTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            String userid = SharedPreferencesUtil.getUserId(ChildActivity.this);
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
                    .url(urls + "/setplaces")
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
                    // 서버로부터 받은 장소 정보를 파싱하여 리스트에 저장
                    JSONObject jsonResponse = new JSONObject(responseString);
                    JSONArray placeArray = jsonResponse.getJSONArray("places"); // 키 이름 변경

                    // 장소 정보를 저장할 리스트
                    List<PlaceInfo> placeList = new ArrayList<>();

                    for (int i = 0; i < placeArray.length(); i++) {
                        JSONObject placeObject = placeArray.getJSONObject(i);
                        String placeName = placeObject.getString("place_name");
                        double latitude = placeObject.getDouble("place_latitude");
                        double longitude = placeObject.getDouble("place_longitude");

                        // 장소 정보를 리스트에 추가
                        placeList.add(new PlaceInfo(latitude, longitude, placeName));

                        // 장소명과 위도 경도를 사용하여 지도에 마커를 추가
                        LatLng location = new LatLng(latitude, longitude);
                        googleMap.addMarker(new MarkerOptions().position(location).title(placeName));
                    }

                    // 장소 정보를 이용하여 위치 상태를 확인하는 메서드 호출
                    checkLocationStatus(placeList);

                } catch (JSONException e) {
                    e.printStackTrace();
                    showToastMessage("장소 정보를 가져오는 도중 오류가 발생했습니다.");
                }
            } else {
                // 서버로부터 응답이 null인 경우, 사용자에게 알림을 줍니다.
                showToastMessage("등록된 장소가 없습니다.");
            }
        }
    }

    // 위치 상태를 확인하는 메서드
    private void checkLocationStatus(List<PlaceInfo> placeList) {
        // 위치 권한이 있는지 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 위치 업데이트 요청
            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    // 새로운 위치 업데이트가 있을 때 호출됨
                    if (locationResult != null) {
                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            for (PlaceInfo place : placeList) {
                                // 현재 위치와 각 장소의 거리 계산
                                float distance = calculateDistance(latitude, longitude, place.getLatitude(), place.getLongitude());
                                if (distance <= 50) { // 50미터 이내 위치 변경 감지
                                    if (!place.hasArrived()) {
                                        // 일정 범위 내에 처음 들어왔으므로 도착으로 간주하고 상태 전송
                                        SendDeparture(place.getName(), "도착");
                                        place.setHasArrived(true); // 도착 상태로 설정
                                    }
                                } else {
                                    if (place.hasArrived()) {
                                        // 일정 범위를 처음 벗어났으므로 출발으로 간주하고 상태 전송
                                        SendDeparture(place.getName(), "출발");
                                        place.setHasArrived(false); // 출발 상태로 설정
                                    }
                                }
                            }
                        }
                    }
                }
            }, null);
        } else {
            // 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    // 거리 계산 메서드
    private float calculateDistance(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        float[] results = new float[1];
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
        return results[0];
    }


    private void SendDeparture(String placeName, String status) {
        // 사용자 아이디 가져오기
        String userid = SharedPreferencesUtil.getUserId(ChildActivity.this);

        // 현재 날짜와 시간 가져오기
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
        String currentDate = sdfDate.format(new Date());
        String currentTime = sdfTime.format(new Date());

        // JSON 데이터 생성
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("userid", userid);
            jsonParam.put("place_name", placeName);
            jsonParam.put("status", status);
            jsonParam.put("date", currentDate);
            jsonParam.put("time", currentTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // HTTP POST 요청 생성
        OkHttpClient client = new OkHttpClient();
        RequestBody reqBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonParam.toString()
        );
        Request request = new Request.Builder()
                .post(reqBody)
                .url(urls + "/senddepartureinfo") // 서버의 해당 엔드포인트 URL로 변경
                .build();

        // 요청 실행
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 요청 실패 처리
                e.printStackTrace();
                showToastMessage("서버와의 통신에 실패했습니다.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 요청 성공 처리
                if (response.isSuccessful()) {
                    // 응답 처리
                    String responseData = response.body().string();
                    // 응답 데이터에 따른 추가 처리 가능
                    showToastMessage("서버로 위치 상태를 전송했습니다.");
                } else {
                    // 서버 응답 실패 처리
                    showToastMessage("서버 응답이 실패했습니다.");
                }
            }
        });
    }
}