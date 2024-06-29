package com.example.onto_1921;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//게시판 목록에서 글 클릭시 보이는 화면
public class GpostDetailActivity extends AppCompatActivity {

    private TextView textViewTitle;
    private TextView textViewContent;
    private TextView textViewAuthorId;
    private Button buttonDelete;
    private static final String urls = "http://192.168.219.104:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpost_detail);

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewContent = findViewById(R.id.textViewContent);
        textViewAuthorId = findViewById(R.id.textViewauthorId);
        buttonDelete = findViewById(R.id.buttonDelete);

        String userid = SharedPreferencesUtil.getUserId(GpostDetailActivity.this);

        // Intent에서 게시글의 제목과 내용을 가져와서 화면에 표시
        Intent intent = getIntent();
        String postId = intent.getStringExtra("postId");
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        String authorId = intent.getStringExtra("authorId");

        textViewTitle.setText(title);
        textViewContent.setText(content);
        textViewAuthorId.setText("작성자: " + authorId);

        // 작성자와 현재 사용자가 일치하면 삭제 버튼을 표시
        if (authorId.equals(userid)) {
            buttonDelete.setVisibility(View.VISIBLE);
        }

        // 게시글 삭제 버튼 클릭 시
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 삭제를 확인하는 다이얼로그 표시
                showDeleteConfirmationDialog(postId, userid);
            }
        });
    }

    // 삭제를 확인하는 다이얼로그 표시
    private void showDeleteConfirmationDialog(String postId, String userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("게시글 삭제");
        builder.setMessage("정말 삭제하시겠습니까?");
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 사용자가 확인(예) 버튼을 클릭한 경우 삭제 작업을 수행합니다.
                deletePost(postId, userId);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 사용자가 취소(아니오) 버튼을 클릭한 경우 아무 작업도 수행하지 않습니다.
            }
        });
        builder.show();
    }


    // 삭제 요청 메서드
    private void deletePost(String postId, String userId) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(urls + "/deletepost/" + postId + "/" + userId) // postId와 userId를 경로에 추가
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(GpostDetailActivity.this, "삭제 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(GpostDetailActivity.this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(GpostDetailActivity.this, "삭제 실패", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
