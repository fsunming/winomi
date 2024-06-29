package com.example.onto_1921;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GpostActivity extends AppCompatActivity {

    private Button write_post_button;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList; // postList 변수 추가

    private static final String urls = "http://localhost:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpost);

        write_post_button = findViewById(R.id.write_post_button);
        recyclerView = findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 새 게시글 작성 버튼 클릭 시
        write_post_button.setOnClickListener(v -> {
            Intent intent = new Intent(GpostActivity.this, GpostwriteActivity.class);
            startActivity(intent);
        });

        // postList 초기화
        postList = new ArrayList<>();

        // 게시글 목록 가져오기
        fetchPostList();
    }

    private void fetchPostList() {
        // GpostActivity의 fetchPostList() 메서드 내부에서 postAdapter를 설정하는 부분
        postAdapter = new PostAdapter(postList);
        recyclerView.setAdapter(postAdapter);

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(urls + "/getpostlist") // 서버의 게시글 목록을 가져오는 엔드포인트 URL
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            // JSON 데이터 파싱
                            postList.clear(); // 기존 목록 삭제
                            postList.addAll(parsePostList(responseData)); // 새로운 목록 추가
                            // 로그 추가: 데이터 확인
                            Log.d("FetchPostList", "Received data: " + postList.toString());
                            // RecyclerView에 어댑터 설정
                            postAdapter.notifyDataSetChanged(); // 변경된 데이터를 어댑터에 알려줌
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 서버 응답 데이터에서 게시글 목록을 파싱하는 메소드
    private List<Post> parsePostList(String responseData) throws JSONException {
        List<Post> postList = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(responseData);
        JSONArray jsonArray = jsonObject.getJSONArray("post"); // "post" 키를 가진 JSON 배열 추출
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject postObject = jsonArray.getJSONObject(i);
            String title = postObject.getString("title");
            String content = postObject.getString("content");
            String authorId = postObject.getString("author_id"); // 키 이름 수정(author_id)
            postList.add(new Post(title, content, authorId));
        }
        return postList;
    }

}


