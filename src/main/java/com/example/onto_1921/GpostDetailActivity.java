package com.example.onto_1921;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GpostDetailActivity extends AppCompatActivity {

    private TextView textViewTitle;
    private TextView textViewContent;
    private TextView textViewAuthorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpost_detail);

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewContent = findViewById(R.id.textViewContent);
        textViewAuthorId = findViewById(R.id.textViewauthorId);

        // Intent에서 게시글의 제목과 내용을 가져와서 화면에 표시
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        String authorId = intent.getStringExtra("authorId");

        textViewTitle.setText(title);
        textViewContent.setText(content);
        textViewAuthorId.setText("작성자: " + authorId);
    }
}
