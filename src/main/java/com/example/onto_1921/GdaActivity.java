package com.example.onto_1921;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class GdaActivity extends AppCompatActivity {
    private EditText editText;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gda);

        editText = findViewById(R.id.editTextInput);
        submitButton = findViewById(R.id.buttonSubmit);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = editText.getText().toString();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", inputText);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }
}