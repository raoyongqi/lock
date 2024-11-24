package com.tangledbytes.phoneblocker.dialogs;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tangledbytes.phoneblocker.R;

public class AboutDialog extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_about);
        Button btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener((View view) -> finish());
    }
}
