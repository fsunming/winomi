package com.example.onto_1921;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class GcallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gcall); // Ensure you have a corresponding layout

        Button call182Button = findViewById(R.id.button_call_182);
        call182Button.setOnClickListener(v -> callNumber("182"));

        Button call112Button = findViewById(R.id.button_call_112);
        call112Button.setOnClickListener(v -> callNumber("112"));
    }

    private void callNumber(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL); // Use ACTION_DIAL for user confirmation
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
        finish();
    }
}