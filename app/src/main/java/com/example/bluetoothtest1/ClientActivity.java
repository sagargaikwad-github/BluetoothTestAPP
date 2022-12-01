package com.example.bluetoothtest1;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class ClientActivity extends AppCompatActivity {
    BluetoothAdapter bluetoothAdapter;
    Button showPairedDevices;
    static TextView DeviceA, DeviceAStatus, DeviceACounter;

    BluetoothDevice[] paired_device_array;
    private ArrayAdapter arrayAdapter;
    ListView deviceListView;
    private static final java.util.UUID UUID = java.util.UUID.fromString("9bbb4aaa-c772-4e30-853a-e6a64f5e30f3");


    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECIEVED = 7;
    static String DeviceName;

    private BluetoothSocket socket;
    // private static BluetoothSocket bluetoothSocket;

    static SendRecieve sendRecieve;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        showPairedDevices = findViewById(R.id.showPairedDevices);
        deviceListView = findViewById(R.id.deviceListView);
        DeviceA = findViewById(R.id.deviceA);
        DeviceAStatus = findViewById(R.id.deviceAStatus);
        DeviceACounter = findViewById(R.id.deviceACounter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        showPairedDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkBluetoothOn();
            }
        });

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (bluetoothAdapter.isEnabled()) {
                    ClientClass connectThread = new ClientClass(paired_device_array[i]);
                    connectThread.start();
                    DeviceAStatus.setText("Connecting");
                    DeviceName = connectThread.device.getName();
                } else {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, 100);
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void checkBluetoothOn() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 100);
        } else {
            showPairedDEVICES();
        }
    }

    @SuppressLint("MissingPermission")
    private void showPairedDEVICES() {
        @SuppressLint("MissingPermission") Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        String[] strings = new String[pairedDevices.size()];
        paired_device_array = new BluetoothDevice[pairedDevices.size()];
        int i = 0;

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                paired_device_array[i] = device;
                strings[i] = device.getName();
                i++;
            }
            arrayAdapter = new ArrayAdapter(ClientActivity.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, strings);
            deviceListView.setAdapter(arrayAdapter);
        }
    }

    static Handler handler = new Handler(new Handler.Callback() {
        @SuppressLint("MissingPermission")
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case STATE_CONNECTED:
                    DeviceAStatus.setText("Connected To : " + DeviceName);
                    break;
                case STATE_CONNECTION_FAILED:
                    DeviceAStatus.setText("Connection Failed..");
                    break;
                case STATE_MESSAGE_RECIEVED:
                    byte[] readBuff1 = (byte[]) message.obj;
                    String tempMsg1 = new String(readBuff1, 0, message.arg1);
                    DeviceACounter.setText(tempMsg1);
                    break;
            }
            return true;
        }
    });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Toast.makeText(ClientActivity.this, "Bluetooth is Turned on", Toast.LENGTH_SHORT).show();
            showPairedDEVICES();
        } else {
            Toast.makeText(ClientActivity.this, "You need to Turn on Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    private class ClientClass extends Thread {
        private BluetoothDevice device;
        //private BluetoothSocket socket;

        @SuppressLint("MissingPermission")
        public ClientClass(BluetoothDevice device1) {
            device = device1;
            try {
                if (bluetoothAdapter.isEnabled()) {
                    socket = device.createRfcommSocketToServiceRecord(UUID);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressLint("MissingPermission")
        public void run() {
            try {
                socket.connect();

                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);

                SendRecieve sendRecieve = new SendRecieve(socket);
                sendRecieve.start();

            } catch (IOException e) {
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                cancel();
            }
        }
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    public static class SendRecieve extends Thread {

        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendRecieve(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = tempIn;
            outputStream = tempOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECIEVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
//                    Message message = Message.obtain();
//                    message.what = STATE_CONNECTION_FAILED;
//                    handler.sendMessage(message);
                    cancel();
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
//                Message message = Message.obtain();
//                message.what = STATE_CONNECTION_FAILED;
//                handler.sendMessage(message);
                cancel();
            }
        }

        public void cancel() {
            try {
                inputStream.close();
                outputStream.close();
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}