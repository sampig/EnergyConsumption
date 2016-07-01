/**
 * Copyright (C) 2016 Chenfeng ZHU
 */
package org.zhuzhu.energyconsumption.scanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * This is the application for Energy Consumption.
 *
 * @author Chenfeng ZHU
 */
public class ECSMainActivity extends AppCompatActivity {

    // TODO: change the type of button
    protected ImageButton btnScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_ecs_main);
//        this.startScanner();
        Intent intent = new Intent(ECSMainActivity.this, ScannerActivity.class);
        startActivity(intent);
    }

    protected void startScanner() {
        btnScanner = (ImageButton) findViewById(R.id.qr_btnScanner);
        btnScanner.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ECSMainActivity.this, ScannerActivity.class);
                startActivity(intent);
            }
        });
    }

}
