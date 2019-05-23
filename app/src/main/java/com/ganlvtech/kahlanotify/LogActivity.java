package com.ganlvtech.kahlanotify;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class LogActivity extends Activity {
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
