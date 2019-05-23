package com.ganlvtech.kahlanotify;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class LogActivity extends AppCompatActivity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        textView = findViewById(R.id.textViewLog);

        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            text.append(i).append("\n");
        }
        textView.setText(text.toString());
    }
}
