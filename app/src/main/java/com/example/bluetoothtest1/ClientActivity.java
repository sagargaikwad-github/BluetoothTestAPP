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
import java.util.ArrayList;
import java.util.Set;

public class ClientActivity extends AppCompatActivity {
    BluetoothAdapter bluetoothAdapter;
    Button showPairedDevices;
    static TextView DeviceA, DeviceAStatus, DeviceACounter;
    static TextView DeviceB, DeviceBStatus, DeviceBCounter;
    static TextView DeviceC, DeviceCStatus, DeviceCCounter;

    BluetoothDevice[] paired_device_array;
    private ArrayAdapter arrayAdapter;
    ListView deviceListView;
    private static final java.util.UUID UUID = java.util.UUID.fromString("9bbb4aaa-c772-4e30-853a-e6a64f5e30f3");


    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECIEVED_FROM_A = 7;
    static final int STATE_MESSAGE_RECIEVED_FROM_B = 8;
    static final int STATE_MESSAGE_RECIEVED_FROM_C = 9;
    static String DeviceName;

    private final ArrayList<java.util.UUID> uuidList = new ArrayList<>();

    private BluetoothSocket socket;
    // private static BluetoothSocket bluetoothSocket;



    String ThreadName="A";


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

        DeviceBStatus = findViewById(R.id.deviceBStatus);
        DeviceBCounter = findViewById(R.id.deviceBCounter);

        DeviceCStatus = findViewById(R.id.deviceCStatus);
        DeviceCCounter = findViewById(R.id.deviceCCounter);

        //Bundle b = new Bundle();
       // ThreadName = b.getString("TN");
        Toast.makeText(this, ThreadName, Toast.LENGTH_SHORT).show();


        uuidList.clear();
        uuidList.add(UUID.fromString("fe964a9c-184c-11e6-b6ba-3e1d05defe78"));
        uuidList.add(UUID.fromString("fe964e02-184c-11e6-b6ba-3e1d05defe78"));
        uuidList.add(UUID.fromString("fe964f9c-184c-11e6-b6ba-3e1d05defe78"));
        uuidList.add(UUID.fromString("fe965438-184c-11e6-b6ba-3e1d05defe78"));




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

                    if (ThreadName == "A") {
                        ClientAClass connectThread = new ClientAClass(paired_device_array[i]);
                        connectThread.start();
                        DeviceAStatus.setText("Connecting");
                        DeviceName = connectThread.device.getName();
                    }
                    if (ThreadName == "B") {
                        ClientBClass connectThread = new ClientBClass(paired_device_array[i]);
                        connectThread.start();
                        DeviceBStatus.setText("Connecting");
//                        DeviceName = connectThread.device.getName();
                    }
                    if (ThreadName == "C") {
                        ClientCClass connectThread = new ClientCClass(paired_device_array[i]);
                        connectThread.start();
                        DeviceCStatus.setText("Connecting");
//                        DeviceName = connectThread.device.getName();
                    }

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
                case STATE_MESSAGE_RECIEVED_FROM_A:
                    byte[] readBuffA = (byte[]) message.obj;
                    String tempMsgA = new String(readBuffA, 0, message.arg1);
                    DeviceACounter.setText(tempMsgA);
                    break;
                case STATE_MESSAGE_RECIEVED_FROM_B:
                    byte[] readBuffB = (byte[]) message.obj;
                    String tempMsgB = new String(readBuffB, 0, message.arg1);
                    DeviceBCounter.setText(tempMsgB);
                    break;
                case STATE_MESSAGE_RECIEVED_FROM_C:
                    byte[] readBuffC = (byte[]) message.obj;
                    String tempMsgC = new String(readBuffC, 0, message.arg1);
                    DeviceACounter.setText(tempMsgC);
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

    private class ClientAClass extends Thread {
        private BluetoothDevice device;
        //private BluetoothSocket socket;

        @SuppressLint("MissingPermission")
        public ClientAClass(BluetoothDevice device1) {
            device = device1;
            try {
                if (bluetoothAdapter.isEnabled()) {
                    for (int i = 0; i < uuidList.size(); i++) {
                        socket = device.createRfcommSocketToServiceRecord(uuidList.get(0));
                    }
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

    private class ClientBClass extends Thread {
        private BluetoothDevice device;
        //private BluetoothSocket socket;

        @SuppressLint("MissingPermission")
        public ClientBClass(BluetoothDevice device1) {
            device = device1;
            try {
                if (bluetoothAdapter.isEnabled()) {
                    for (int i = 0; i < uuidList.size(); i++) {
                        socket = device.createRfcommSocketToServiceRecord(uuidList.get(1));
                    }
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

                SendRecieveB sendRecieveB = new SendRecieveB(socket);
                sendRecieveB.start();

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

    private class ClientCClass extends Thread {
        private BluetoothDevice device;
        //private BluetoothSocket socket;

        @SuppressLint("MissingPermission")
        public ClientCClass(BluetoothDevice device1) {
            device = device1;
            try {
                if (bluetoothAdapter.isEnabled()) {
                    for (int i = 0; i < uuidList.size(); i++) {
                        socket = device.createRfcommSocketToServiceRecord(uuidList.get(2));
                    }
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

                SendRecieveC sendRecieveC = new SendRecieveC(socket);
                sendRecieveC.start();

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
                    handler.obtainMessage(STATE_MESSAGE_RECIEVED_FROM_A, bytes, -1, buffer).sendToTarget();
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
              //  cancel();
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

    public static class SendRecieveB extends Thread {

        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendRecieveB(BluetoothSocket socket) {
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
                    handler.obtainMessage(STATE_MESSAGE_RECIEVED_FROM_B, bytes, -1, buffer).sendToTarget();
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

    public static class SendRecieveC extends Thread {

        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendRecieveC(BluetoothSocket socket) {
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
                    handler.obtainMessage(STATE_MESSAGE_RECIEVED_FROM_C, bytes, -1, buffer).sendToTarget();
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