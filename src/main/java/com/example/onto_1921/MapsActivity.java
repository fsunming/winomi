package com.example.onto_1921;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap googleMap;
    private boolean isAddMarkerMode = false;
    private Geocoder geocoder;
    private static final String TAG = "MapsActivity";
    private Map<String, ArrayAdapter<CharSequence>> spinner2Adapters;
    private static final String urls = "http://192.168.219.104:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        geocoder = new Geocoder(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setupSpinners();

        new FetchPlacesTask().execute();
    }

    // 토스트 메세지 출력 메소드
    private void showToastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setupSpinners() {
        Spinner spin01 = findViewById(R.id.spinner1);
        Spinner spin02 = findViewById(R.id.spinner2);

        ArrayAdapter<CharSequence> spinner01Adapter = ArrayAdapter.createFromResource(this, R.array.spinner01, android.R.layout.simple_spinner_dropdown_item);
        spin01.setAdapter(spinner01Adapter);

        spinner2Adapters = new HashMap<>();
        spinner2Adapters.put("강원도", ArrayAdapter.createFromResource(this, R.array.gangwondo, android.R.layout.simple_spinner_dropdown_item));
        spinner2Adapters.put("경기도", ArrayAdapter.createFromResource(this, R.array.gyeonggido, android.R.layout.simple_spinner_dropdown_item));
        spinner2Adapters.put("경상남도", ArrayAdapter.createFromResource(this, R.array.gyeongnam, android.R.layout.simple_spinner_dropdown_item));
        spinner2Adapters.put("경상북도", ArrayAdapter.createFromResource(this, R.array.gyeongbuk, android.R.layout.simple_spinner_dropdown_item));
        spinner2Adapters.put("광주광역시", ArrayAdapter.createFromResource(this, R.array.gwangju, android.R.layout.simple_spinner_dropdown_item));
        spinner2Adapters.put("대구광역시", ArrayAdapter.createFromResource(this, R.array.daegu, android.R.layout.simple_spinner_dropdown_item));
        spinner2Adapters.put("대전광역시", ArrayAdapter.createFromResource(this, R.array.daejeon, android.R.layout.simple_spinner_dropdown_item));
        spinner2Adapters.put("부산광역시", ArrayAdapter.createFromResource(this, R.array.busan, android.R.layout.simple_spinner_dropdown_item));
        spinner2Adapters.put("서울특별시", ArrayAdapter.createFromResource(this, R.array.seoul, android.R.layout.simple_spinner_dropdown_item));
        spinner2Adapters.put("세종특별자치시", ArrayAdapter.createFromResource(this, R.array.sejong, android.R.layout.simple_spinner_dropdown_item));
        spinner2Adapters.put("울산광역시", ArrayAdapter.createFromResource(this, R.array.ulsan, android.R.layout.simple_spinner_dropdown_item));
        spinner2Adapters.put("인천광역시", ArrayAdapter.createFromResource(this, R.array.incheon, android.R.layout.simple_spinner_dropdown_item));
        spinner2Adapters.put("전라남도", ArrayAdapter.createFromResource(this, R.array.jeonnam, android.R.layout.simple_spinner_dropdown_item));
        spinner2Adapters.put("전라북도", ArrayAdapter.createFromResource(this, R.array.jeonbuk, android.R.layout.simple_spinner_dropdown_item));
        spinner2Adapters.put("제주특별자치도", ArrayAdapter.createFromResource(this, R.array.jeju, android.R.layout.simple_spinner_dropdown_item));
        spinner2Adapters.put("충청남도", ArrayAdapter.createFromResource(this, R.array.chungnam, android.R.layout.simple_spinner_dropdown_item));
        spinner2Adapters.put("충청북도", ArrayAdapter.createFromResource(this, R.array.chungbuk, android.R.layout.simple_spinner_dropdown_item));

        spin01.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedItem = parentView.getItemAtPosition(position).toString();
                ArrayAdapter<CharSequence> spinner02Adapter = spinner2Adapters.get(selectedItem);
                if (spinner02Adapter != null) {
                    spin02.setAdapter(spinner02Adapter);
                } else {
                    spin02.setAdapter(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // 아무것도 선택되지 않았을 때 실행되는 코드
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);

            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        //MarkerOptions markerOptions = new MarkerOptions().position(currentLatLng).title("현재 위치");
                        //googleMap.addMarker(markerOptions);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                        //Toast.makeText(MapsActivity.this, "현재 위치: " + currentLatLng.toString(), Toast.LENGTH_SHORT).show();
                    } else {
                        showToastMessage("현재 위치를 가져올 수 없습니다.");
                        moveToDefaultLocation();
                    }
                }
            });
            // 기본적으로 마커 추가 모드는 비활성화 상태로 둡니다.
            // toggleAddMarkerMode();
        } else {
            checkLocationPermissionWithRationale();
            moveToDefaultLocation();
        }

        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
    }


    private void toggleAddMarkerMode() {
        isAddMarkerMode = !isAddMarkerMode;
        if (isAddMarkerMode) {
            Toast.makeText(this, "마커 추가 모드 활성화", Toast.LENGTH_SHORT).show();
            googleMap.setOnMapClickListener(this);
        } else {
            Toast.makeText(this, "마커 추가 모드 비활성화", Toast.LENGTH_SHORT).show();
            googleMap.setOnMapClickListener(null);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (isAddMarkerMode) {
            addMarker(latLng);
        }
    }

    private void addMarker(LatLng latLng) {
        googleMap.addMarker(new MarkerOptions().position(latLng).title("새로운 마커"));
    }

    private void moveToDefaultLocation() {
        LatLng latLng = new LatLng(35.180511, 128.553315);
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("경남대학교");
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermissionWithRationale() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this)
                    .setTitle("위치정보")
                    .setMessage("이 앱을 사용하기 위해서는 위치정보에 접근이 필요합니다. 위치정보 접근을 허용하여 주세요.")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                        }
                    }).create().show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (googleMap != null) {
                            googleMap.setMyLocationEnabled(true);
                            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                        }
                    }
                } else {
                    Toast.makeText(this, "위치 권한이 거부되었습니다.", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_marker:
                toggleAddMarkerMode();
                return true;
            case R.id.action_show_spinner:
                openSpinnerDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSpinnerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("지역 선택");

        View view = getLayoutInflater().inflate(R.layout.spinner_dialog, null);
        Spinner spinner1 = view.findViewById(R.id.spinner1);
        Spinner spinner2 = view.findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.spinner01, android.R.layout.simple_spinner_item);
        spinner1.setAdapter(adapter1);

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedItem = parentView.getItemAtPosition(position).toString();
                ArrayAdapter<CharSequence> adapter2 = spinner2Adapters.get(selectedItem);
                if (adapter2 != null) {
                    spinner2.setAdapter(adapter2);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // 아무것도 선택되지 않았을 때 실행되는 코드
            }
        });

        builder.setView(view);

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedArea = spinner1.getSelectedItem().toString();
                String selectedColumn = spinner2.getSelectedItem().toString();
                showMarkersForSelectedAreaAndColumn(selectedArea, selectedColumn);
            }

            private void showMarkersForSelectedAreaAndColumn(String selectedArea, String selectedColumn) {
                try {
                    // 기존 마커 제거
                    googleMap.clear();

                    InputStream is = getAssets().open(selectedArea + ".xlsx");
                    Workbook workbook = new XSSFWorkbook(is);
                    Sheet sheet = workbook.getSheet(selectedColumn);

                    if (sheet == null) {
                        Toast.makeText(MapsActivity.this, "시트를 찾을 수 없습니다: " + selectedColumn, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<LatLng> markerPositions = new ArrayList<>(); // 마커 위치를 저장할 리스트

                    for (Row row : sheet) {
                        Cell cell = row.getCell(0);
                        if (cell != null) {
                            String address = cell.getStringCellValue();
                            if (address == null || address.isEmpty()) {
                                Log.w(TAG, "Empty or null address at row: " + row.getRowNum());
                                continue;
                            }
                            Log.d(TAG, "Processing address: " + address);
                            try {
                                LatLng latLng = getLocationFromAddress(MapsActivity.this, address);
                                if (latLng != null) {
                                    googleMap.addMarker(new MarkerOptions().position(latLng).title(address).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                    markerPositions.add(latLng); // 마커 위치를 리스트에 추가
                                } else {
                                    Log.w(TAG, "Failed to get location for address: " + address);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error getting location for address: " + address, e);
                            }
                        } else {
                            Log.w(TAG, "Empty cell at row: " + row.getRowNum());
                        }
                    }

                    // 중간 마커로 카메라 이동
                    if (!markerPositions.isEmpty()) {
                        int middleIndex = markerPositions.size() / 2;
                        LatLng middleLatLng = markerPositions.get(middleIndex);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(middleLatLng, 13));
                    } else {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.5665, 126.978), 10)); // 기본 위치로 이동
                    }
                    // 마커의 수를 계산하여 토스트 메시지로 표시
                    int numMarkers = markerPositions.size();
                    String message = String.format("%d개의 지킴이집을 발견했습니다.", numMarkers);
                    Toast.makeText(MapsActivity.this, message, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e(TAG, "Error reading XLSX file", e);
                    Toast.makeText(MapsActivity.this, "파일을 읽는 중 오류가 발생했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } catch (StringIndexOutOfBoundsException e) {
                    Log.e(TAG, "String index out of bounds in XLSX file", e);
                    Toast.makeText(MapsActivity.this, "엑셀 파일 데이터 형식 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error", e);
                    Toast.makeText(MapsActivity.this, "예기치 못한 오류가 발생했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 취소 버튼 클릭 시 처리할 내용
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private LatLng getLocationFromAddress(Context context, String strAddress) {
        List<Address> addressList;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    // AsyncTask를 통해 서버로부터 장소 정보를 가져오는 클래스
    private class FetchPlacesTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String userid = SharedPreferencesUtil.getUserId(MapsActivity.this);
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
                    // 서버로부터 받은 장소 정보를 파싱하여 마커를 추가하는 코드 작성
                    JSONObject jsonResponse = new JSONObject(responseString);
                    JSONArray placeArray = jsonResponse.getJSONArray("places"); // 키 이름 변경
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < placeArray.length(); i++) {
                        JSONObject placeObject = placeArray.getJSONObject(i);
                        String placeName = placeObject.getString("place_name");
                        double latitude = placeObject.getDouble("place_latitude");
                        double longitude = placeObject.getDouble("place_longitude");

                        // 장소명과 위도 경도를 사용하여 지도에 마커를 추가
                        LatLng location = new LatLng(latitude, longitude);
                        googleMap.addMarker(new MarkerOptions().position(location).title(placeName));
                    }
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
}