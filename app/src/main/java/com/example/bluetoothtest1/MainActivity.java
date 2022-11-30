package com.example.bluetoothtest1;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Spinner spinner;
    String[] DeviceName = {"Select Device", "A", "B", "C"};
    Button serverStartBTN,clientStartBTN;

    BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.Spinner);
        serverStartBTN = findViewById(R.id.serverStartBTN);
        clientStartBTN = findViewById(R.id.clientStartBTN);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        spinner.setOnItemSelectedListener(MainActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, DeviceName);
        spinner.setAdapter(arrayAdapter);

        serverStartBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 checkBluetoothIsOn();
            }
        });

        clientStartBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkBluetoothIsOn();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void checkBluetoothIsOn() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 100);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (i != 0) {
            Toast.makeText(this, DeviceName[i], Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Toast.makeText(MainActivity.this, "Bluetooth is Turned on", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "You need to Turn on Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }
}