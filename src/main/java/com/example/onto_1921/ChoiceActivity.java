package com.example.onto_1921;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class ChoiceActivity extends AppCompatActivity {

    private RadioGroup choiceModeRG;
    private Button nextBtn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);

        choiceModeRG = findViewById(R.id.choiceModeRG);
        nextBtn = findViewById(R.id.nextBtn);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedRadioButtonId = choiceModeRG.getCheckedRadioButtonId();
                RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);

                // 선택된 라디오버튼에 따라 다른 화면으로 이동
                if (selectedRadioButton != null) {
                    if (selectedRadioButton.getId() == R.id.pttRbtn) {
                        Intent intent = new Intent(ChoiceActivity.this, GregisterActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (selectedRadioButton.getId() == R.id.ptRbtn) {
                        Intent intent = new Intent(ChoiceActivity.this, PregisterActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
    }
}