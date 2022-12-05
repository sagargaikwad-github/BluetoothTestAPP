package com.example.bluetoothtest1;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Spinner spinner;
    String[] DeviceName = {"Select Device", "A", "B", "C"};
    Button serverStartBTN, clientStartBTN,fourthDeviceBTN;
    private static final String APP_NAME = "Bluetooth App";
     private static final java.util.UUID UUID4th = java.util.UUID.fromString("9bbb4aaa-c772-4e30-853a-e6a64f5e30f3");
    BluetoothAdapter bluetoothAdapter;

    TextView statusOfBluetooth, CounterTV,CounterTVB,CounterTVC;
    static TextView fourthDeviceTV;

    ClientActivity.SendRecieveA sendRecieveA;
    ClientActivity.SendRecieveB sendRecieveB;
    ClientActivity.SendRecieveC sendRecieveC;

    int Time = 50;
    int TimeB = 500;
    int TimeC = 50000;
    int seconds = 0;
    String DevName;
    CountDownTimer countDownTimer;
    BluetoothDevice[] paired_device_array;



    String ThreadName = "A";
    static final int STATE_CONNECTING = 1;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_DATA_RECIEVED_FOR_4TH_DEVICE = 6;
    private final ArrayList<UUID> uuidList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.Spinner);
        serverStartBTN = findViewById(R.id.serverStartBTN);
        clientStartBTN = findViewById(R.id.clientStartBTN);
        statusOfBluetooth = findViewById(R.id.statusOfBluetooth);
        CounterTV = findViewById(R.id.counterTV);

        fourthDeviceBTN=findViewById(R.id.fourthDeviceBTN);
        fourthDeviceTV=findViewById(R.id.fourthDeviceTV);

        CounterTVB = findViewById(R.id.counterTVB);
        CounterTVC = findViewById(R.id.counterTVC);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        spinner.setOnItemSelectedListener(MainActivity.this);


        uuidList.clear();
        uuidList.add(UUID.fromString("fe964a9c-184c-11e6-b6ba-3e1d05defe78"));
        uuidList.add(UUID.fromString("fe964e02-184c-11e6-b6ba-3e1d05defe78"));
        uuidList.add(UUID.fromString("fe964f9c-184c-11e6-b6ba-3e1d05defe78"));
        uuidList.add(UUID.fromString("fe965438-184c-11e6-b6ba-3e1d05defe78"));


    }

    private void coundownTimerA() {
        countDownTimer = new CountDownTimer(Time * 1000, 1000) {
            @Override
            public void onTick(long l) {
                seconds = (int) (l / 1000);
                try {
                    CounterTV.setText(String.valueOf(seconds));

                        sendRecieveA.write(CounterTV.getText().toString().getBytes());



                } catch (Exception e) {
                    Log.e("TAG", e.toString());
                }
            }

            @Override
            public void onFinish() {
                coundownTimerA();
            }
        }.start();
    }
    private void coundownTimerB() {
        countDownTimer = new CountDownTimer(TimeB * 1000, 1000) {
            @Override
            public void onTick(long l) {
                seconds = (int) (l / 1000);
                try {
                    CounterTVB.setText(String.valueOf(seconds));
                    sendRecieveB.write(CounterTVB.getText().toString().getBytes());

                } catch (Exception e) {
                    Log.e("TAG", e.toString());
                }
            }

            @Override
            public void onFinish() {
                coundownTimerB();
            }
        }.start();
    }
    private void coundownTimerC() {
        countDownTimer = new CountDownTimer(TimeC * 1000, 1000) {
            @Override
            public void onTick(long l) {
                seconds = (int) (l / 1000);
                try {
                    CounterTVC.setText(String.valueOf(seconds));
                    sendRecieveC.write(CounterTVC.getText().toString().getBytes());

                } catch (Exception e) {
                    Log.e("TAG", e.toString());
                }
            }

            @Override
            public void onFinish() {
                coundownTimerC();
            }
        }.start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, DeviceName);
        spinner.setAdapter(arrayAdapter);

        coundownTimerA();
        coundownTimerB();
        coundownTimerC();

        serverStartBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //checkBluetoothIsOn();
                ThreadName=spinner.getSelectedItem().toString();
                startServer(ThreadName);
            }
        });

        clientStartBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ClientActivity.class);
                intent.putExtra("TN", ThreadName);
                startActivity(intent);
            }
        });

        fourthDeviceBTN.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("MissingPermission")
                public void onClick(View view) {
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
                        ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, strings);
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Choose Device From List");
                        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                Client4thDevice connectThreadA = new Client4thDevice(paired_device_array[item]);
                                connectThreadA.start();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();

                    }
                }
            });
            }



    private void startServer(String servername) {
        if(servername=="A")
        {
            ServerClassA serverClassA=new ServerClassA();
            serverClassA.start();
            CounterTV.setVisibility(View.VISIBLE);
            CounterTVB.setVisibility(View.GONE);
            CounterTVC.setVisibility(View.GONE);

        }
        if(servername=="B")
        {
            ServerClassB serverClassB=new ServerClassB();
            serverClassB.start();
            CounterTV.setVisibility(View.GONE);
            CounterTVB.setVisibility(View.VISIBLE);
            CounterTVC.setVisibility(View.GONE);

        }
        if(servername=="C")
        {
            ServerClassC serverClassC=new ServerClassC();
            serverClassC.start();
            CounterTV.setVisibility(View.GONE);
            CounterTVB.setVisibility(View.GONE);
            CounterTVC.setVisibility(View.VISIBLE);
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @SuppressLint("MissingPermission")
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case STATE_CONNECTING:
                    statusOfBluetooth.setText("Connecting...");
                    break;
                case STATE_CONNECTED:
                    statusOfBluetooth.setText("Connected To : " + DevName);
                    break;
                case STATE_CONNECTION_FAILED:
                    if (DevName == null) {
                        statusOfBluetooth.setText("Connection Failed");
                    } else {
                        statusOfBluetooth.setText("Connection Failed.." + DevName);
                    }
                    break;
            }
            return true;
        }
    });

    Handler handlerB = new Handler(new Handler.Callback() {
        @SuppressLint("MissingPermission")
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case STATE_CONNECTING:
                    statusOfBluetooth.setText("Connecting...");
                    break;
                case STATE_CONNECTED:
                    statusOfBluetooth.setText("Connected To : " + DevName);
                    break;
                case STATE_CONNECTION_FAILED:
                    if (DevName == null) {
                        statusOfBluetooth.setText("Connection Failed");
                    } else {
                        statusOfBluetooth.setText("Connection Failed.." + DevName);
                    }
                    break;
            }
            return true;
        }
    });

    Handler handlerC = new Handler(new Handler.Callback() {
        @SuppressLint("MissingPermission")
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case STATE_CONNECTING:
                    statusOfBluetooth.setText("Connecting...");
                    break;
                case STATE_CONNECTED:
                    statusOfBluetooth.setText("Connected To : " + DevName);
                    break;
                case STATE_CONNECTION_FAILED:
                    if (DevName == null) {
                        statusOfBluetooth.setText("Connection Failed");
                    } else {
                        statusOfBluetooth.setText("Connection Failed.." + DevName);
                    }
                    break;
            }
            return true;
        }
    });

    static Handler handler4thDevice = new Handler(new Handler.Callback() {
        @SuppressLint("MissingPermission")
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case STATE_DATA_RECIEVED_FOR_4TH_DEVICE:
                    byte[] readBuffC = (byte[]) message.obj;
                    String tempMsgC = new String(readBuffC, 0, message.arg1);
                    fourthDeviceTV.setText(tempMsgC);
                    break;
            }
            return true;
        }
    });


//    @SuppressLint("MissingPermission")
//    private void checkBluetoothIsOn() {
//        if (!bluetoothAdapter.isEnabled()) {
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(intent, 100);
//        } else {
//            ServerClassA serverClassA = new ServerClassA();
//            serverClassA.start();
//        }
//    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (i != 0) {
            ThreadName = DeviceName[i];
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }



//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 100 && resultCode == RESULT_OK) {
//            ServerClassA serverClassA = new ServerClassA();
//            serverClassA.start();
//        } else {
//            Toast.makeText(MainActivity.this, "You need to Turn on Bluetooth", Toast.LENGTH_SHORT).show();
//        }
//    }

//    public class ServerClass extends Thread {
//        private BluetoothServerSocket serverSocket;
//
//        @SuppressLint("MissingPermission")
//        public ServerClass() {
//            try {
////                for (int i=0;i<uuidList.size();i++)
////                {
//                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, uuidList.get(0));
//                // }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @SuppressLint("MissingPermission")
//        public void run() {
//            BluetoothSocket socket = null;
//            while (true) {
//                Message message1 = Message.obtain();
//                message1.what = STATE_CONNECTING;
//                handler.sendMessage(message1);
//
//                try {
//
//                    if (bluetoothAdapter.isEnabled() && serverSocket != null) {
//                        socket = serverSocket.accept();
//                        DevName = socket.getRemoteDevice().getName();
//
//                        socket.close();
//
//                        Message message = Message.obtain();
//                        message.what = STATE_CONNECTED;
//                        handler.sendMessage(message);
//
//                        sendRecieve = new ClientActivity.SendRecieve(socket);
//                        sendRecieve.start();
//                    }
//
//                } catch (IOException e) {
//
//                }
//
//                if (socket != null) {
//                    try {
//                        serverSocket.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    break;
//                }
//            }
//        }
//
//        public void cancel() {
//            try {
//                serverSocket.close();
//            } catch (IOException e) {
//                Log.e(TAG, "Could not close the connect socket", e);
//            }
//        }
//
//    }

    public class ServerClassA extends Thread {
        private BluetoothServerSocket serverSocket;

        @SuppressLint("MissingPermission")
        public ServerClassA() {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, uuidList.get(0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressLint("MissingPermission")
        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                Message message1 = Message.obtain();
                message1.what = STATE_CONNECTING;
                handler.sendMessage(message1);

                try {
                    if(bluetoothAdapter.isEnabled() && serverSocket!=null) {
                        socket = serverSocket.accept();


                        Message message = Message.obtain();
                        message.what = STATE_CONNECTED;
                        handler.sendMessage(message);

                        DevName = socket.getRemoteDevice().getName();

                        sendRecieveA = new ClientActivity.SendRecieveA(socket);
                        sendRecieveA.start();
                    }

                } catch (IOException e) {

                }

                if (socket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }

    }

    public class ServerClassB extends Thread {
        private BluetoothServerSocket serverSocket;

        @SuppressLint("MissingPermission")
        public ServerClassB() {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, uuidList.get(1));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressLint("MissingPermission")
        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                Message message1 = Message.obtain();
                message1.what = STATE_CONNECTING;
                handlerB.sendMessage(message1);

                try {
                    if(bluetoothAdapter.isEnabled() && serverSocket!=null) {
                        socket = serverSocket.accept();


                        Message message = Message.obtain();
                        message.what = STATE_CONNECTED;
                        handler.sendMessage(message);

                        DevName = socket.getRemoteDevice().getName();

                        sendRecieveB = new ClientActivity.SendRecieveB(socket);
                        sendRecieveB.start();

                    }

                } catch (IOException e) {

                }

                if (socket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }

    }

    public class ServerClassC extends Thread {
        private BluetoothServerSocket serverSocket;

        @SuppressLint("MissingPermission")
        public ServerClassC() {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, uuidList.get(2));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressLint("MissingPermission")
        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                Message message1 = Message.obtain();
                message1.what = STATE_CONNECTING;
                handlerC.sendMessage(message1);

                try {
                    if(bluetoothAdapter.isEnabled() && serverSocket!=null) {
                        socket = serverSocket.accept();

                        Message message = Message.obtain();
                        message.what = STATE_CONNECTED;
                        handler.sendMessage(message);

                        DevName = socket.getRemoteDevice().getName();

                        sendRecieveC = new ClientActivity.SendRecieveC(socket);
                        sendRecieveC.start();
                    }

                } catch (IOException e) {

                }

                if (socket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }

    }



    //For FourthDevice
    private class Client4thDevice extends Thread {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        @SuppressLint("MissingPermission")
        public Client4thDevice(BluetoothDevice device1) {
            device = device1;
            try {
                if (bluetoothAdapter.isEnabled()) {
                    for (int i = 0; i < uuidList.size(); i++) {
                        socket = device.createRfcommSocketToServiceRecord(UUID4th);
                        return;
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

                SendRecieve4thDevice sendRecieve4thDevice = new SendRecieve4thDevice(socket);
                sendRecieve4thDevice.start();

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

    public static class SendRecieve4thDevice extends Thread {

        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendRecieve4thDevice(BluetoothSocket socket) {
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
                    handler4thDevice.obtainMessage(STATE_DATA_RECIEVED_FOR_4TH_DEVICE, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
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



    @Override
    protected void onPause() {
        super.onPause();
        countDownTimer.cancel();
    }
}