package com.example.onto_1921;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DepartureActivity extends AppCompatActivity {

    private TextView dateTextView;
    private ListView departureListView;
    private Button refreshButton, prevButton, nextButton;

    private static final String URL = "http://192.168.219.104:8080";
    private Calendar selectedDate;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_departure);

        dateTextView = findViewById(R.id.dateTextView);
        departureListView = findViewById(R.id.departureListView);
        refreshButton = findViewById(R.id.refresh_button);
        prevButton = findViewById(R.id.prevDateButton);
        nextButton = findViewById(R.id.nextDateButton);

        selectedDate = Calendar.getInstance();
        updateDateText();

        refreshButton.setOnClickListener(v -> fetchDepartureInfo());
        prevButton.setOnClickListener(v -> changeDate(-1));
        nextButton.setOnClickListener(v -> changeDate(1));

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        departureListView.setAdapter(adapter);

        fetchDepartureInfo();
    }

    private void updateDateText() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = sdf.format(selectedDate.getTime());
        dateTextView.setText(dateString);
    }

    private void changeDate(int offset) {
        selectedDate.add(Calendar.DATE, offset);
        updateDateText();
        fetchDepartureInfo();
    }

    private void fetchDepartureInfo() {
        String selectedDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());
        new FetchDepartureInfoTask().execute(selectedDateString);
    }

    // 토스트 메세지 출력 메소드
    private void showToastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private class FetchDepartureInfoTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String selectedDateString = params[0];
            String userid = SharedPreferencesUtil.getUserId(DepartureActivity.this);
            OkHttpClient client = new OkHttpClient();
            JSONObject jsonInput = new JSONObject();
            try {
                jsonInput.put("userid", userid);
                jsonInput.put("date", selectedDateString); // Send the selected date to the server
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody reqBody = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    jsonInput.toString()
            );
            Request request = new Request.Builder()
                    .post(reqBody)
                    .url(URL + "/getdepartureinfo") // 서버의 관리자 정보를 가져오는 엔드포인트 URL
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
                    JSONArray jsonArray = new JSONArray(responseString);
                    List<String> departureInfoList = new ArrayList<>();
                    String selectedDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String protectName = jsonObject.getString("protect_name");
                        String placeName = jsonObject.getString("place_name");
                        String date = jsonObject.getString("date");
                        String time = jsonObject.getString("time");
                        String departureArrival = jsonObject.getString("departure_arrival");

                        if (date.equals(selectedDateString)) {
                            String departureInfo = "[" + protectName + "] " + placeName + "에(서) " + date + " " + time + "에 " + departureArrival + "했습니다.";
                            departureInfoList.add(departureInfo);
                        }
                    }
                    // Update the adapter with the filtered data
                    adapter.clear();
                    adapter.addAll(departureInfoList);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    // JSON 파싱에 실패한 경우, 사용자에게 알림을 줍니다.
                    showToastMessage("출발 정보를 파싱하는 도중 오류가 발생했습니다." + e.getMessage());
                }
            } else {
                // 서버로부터 응답이 null인 경우, 사용자에게 알림을 줍니다.
                showToastMessage("출발/도착 정보가 없습니다.");
            }
        }
    }
}
