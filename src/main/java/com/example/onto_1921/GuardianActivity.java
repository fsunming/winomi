package com.example.onto_1921;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuardianActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient fusedLocationClient;
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1001;
    private GoogleMap mMap;
    private WebSocket webSocket;
    private OkHttpClient client;
    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private static final String urls = "http://192.168.219.104:8080";
    private Button selectImgBtn;
    private ImageView imageView;
    private TextView g_noticeBoard, g_protectDAInfo;
    private UploadService uploadService;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardian);

        imageView = findViewById(R.id.g_protectImg);
        selectImgBtn = findViewById(R.id.g_selectImgBtn);
        g_noticeBoard = findViewById(R.id.g_noticeBoard);
        g_protectDAInfo = findViewById(R.id.g_protectDAInfo);

        // 이미지를 서버에서 가져와서 보여줌
        loadUserImage();

        g_protectDAInfo.setOnClickListener(v -> {
            Intent intent = new Intent(GuardianActivity.this, DepartureActivity.class);
            startActivity(intent);
        });

        selectImgBtn.setOnClickListener(v -> openGallery());

        g_noticeBoard.setOnClickListener(v -> {
            Intent intent = new Intent(GuardianActivity.this, GpostActivity.class);
            startActivity(intent);
        });

        // Retrofit 클라이언트 초기화
        uploadService = RetrofitClient.getClient().create(UploadService.class);

        // 이미지 선택을 위한 ActivityResultLauncher 초기화
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    Uri imageUri = data.getData();
                    if (imageUri != null) {
                        imageView.setImageURI(imageUri);
                        // 이미지를 서버에 업로드
                        copyImageToAppDirectory(imageUri);
                    }
                }
            }
        });

        // 외부 저장소 쓰기 권한 요청
        requestPermissionsAndOpenGallery();

        client = new OkHttpClient();
        startWebSocket();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // 프래그먼트에서 지도를 가져와 준비가 되면 콜백을 받습니다.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        new FetchLatestLocationTask().execute();
    }

    // 토스트 메세지 출력 메소드
    private void showToastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void startWebSocket() {
        Request request = new Request.Builder().url("ws://192.168.219.104:8080/locationUpdates").build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(text);
                        double latitude = jsonObject.getDouble("latitude");
                        double longitude = jsonObject.getDouble("longitude");
                        updateMap(new LatLng(latitude, longitude));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 위치 권한 확인
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }

        mMap.setMyLocationEnabled(true);
    }

    private void updateMap(LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title("Protected User Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webSocket.close(1000, null);
    }

    private class FetchLatestLocationTask extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... voids) {
            String userId = SharedPreferencesUtil.getUserId(GuardianActivity.this);
            OkHttpClient client = new OkHttpClient();
            JSONObject jsonInput = new JSONObject();
            try {
                jsonInput.put("userid", userId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody reqBody = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    jsonInput.toString()
            );
            Request request = new Request.Builder()
                    .post(reqBody)
                    .url(urls + "/setlocation") // 서버의 최신 위치 정보를 가져오는 엔드포인트 URL
                    .build();

            try {
                okhttp3.Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string(); // 문자열 데이터로 변환
                    return new JSONObject(responseBody);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonResponse) {
            super.onPostExecute(jsonResponse);
            if (jsonResponse != null) {
                try {
                    double latitude = jsonResponse.getDouble("latitude");
                    double longitude = jsonResponse.getDouble("longitude");

                    // 받아온 위치 정보를 이용하여 지도에 마커 표시 등의 작업 수행
                    LatLng latLng = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(latLng).title("Last Known Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                } catch (JSONException e) {
                    e.printStackTrace();
                    showToastMessage("서버 응답 데이터를 처리하는 중 오류가 발생했습니다.");
                }
            } else {
                showToastMessage("서버로부터 응답을 받을 수 없습니다.");
            }
        }
    }

    private void loadUserImage() {
        String userId = SharedPreferencesUtil.getUserId(this);

        // Retrofit 클라이언트 초기화
        userService = RetrofitClient.getClient().create(UserService.class);

        // 서버에서 사용자 이미지를 가져오는 요청 보내기
        Call<UserImage> call = userService.getUserImage(userId);

        call.enqueue(new Callback<UserImage>() {
            @Override
            public void onResponse(Call<UserImage> call, Response<UserImage> response) {
                if (response.isSuccessful()) {
                    UserImage imageResponse = response.body();
                    if (imageResponse != null) {
                        // 서버에서 받은 이미지 데이터를 디코딩하여 비트맵으로 변환
                        byte[] decodedString = Base64.decode(imageResponse.getImageData(), Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        // 이미지뷰에 비트맵 설정
                        imageView.setImageBitmap(bitmap);
                    } else {
                        showToastMessage("이미지를 가져올 수 없습니다.");
                    }
                } else {
                    showToastMessage("이미지를 등록해주세요.");
                }
            }

            @Override
            public void onFailure(Call<UserImage> call, Throwable t) {
                showToastMessage("이미지를 가져오는 데 실패했습니다.");
            }
        });
    }


    // 권한 요청 후 갤러리 열기
    private void requestPermissionsAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_PICK_IMAGE);
        } else {
            //openGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                onMapReady(mMap);
                openGallery();
            } else {
                // Permission denied
                return;
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    // 이미지 선택 후에 앱 전용 디렉토리로 이미지를 복사하는 메서드 추가
    private void copyImageToAppDirectory(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            File outputDir = getApplicationContext().getFilesDir(); // 앱 전용 디렉토리
            File outputFile = new File(outputDir, "image.jpg"); // 복사할 이미지 파일명
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            // 이미지를 서버에 업로드
            uploadImage(Uri.fromFile(outputFile));
        } catch (IOException e) {
            e.printStackTrace();
            showToastMessage("이미지 복사 실패");
        }
    }

    // 이미지를 서버에 업로드하는 메서드
    private void uploadImage(Uri imageUri) {
        File imageFile = new File(imageUri.getPath());
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestBody);

        String userid = SharedPreferencesUtil.getUserId(GuardianActivity.this);

        RequestBody userIdBody = RequestBody.create(MediaType.parse("application/json"), userid);

        Call<Void> call = uploadService.uploadImage(body, userIdBody);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showToastMessage("이미지 업로드 성공");
                } else {
                    showToastMessage("이미지 업로드 실패");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Upload Error", t.getMessage(), t);
                showToastMessage("이미지 업로드 성공");
            }
        });
    }
}