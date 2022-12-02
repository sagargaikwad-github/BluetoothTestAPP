package com.example.bluetoothtest1;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
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
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Spinner spinner;
    String[] DeviceName = {"Select Device", "A", "B", "C"};
    Button serverStartBTN, clientStartBTN;
    private static final String APP_NAME = "Bluetooth App";
    // private static final java.util.UUID UUID = java.util.UUID.fromString("9bbb4aaa-c772-4e30-853a-e6a64f5e30f3");
    BluetoothAdapter bluetoothAdapter;

    TextView statusOfBluetooth, CounterTV,CounterTVB,CounterTVC;

    ClientActivity.SendRecieveA sendRecieveA;
    ClientActivity.SendRecieveB sendRecieveB;
    ClientActivity.SendRecieveC sendRecieveC;

    int Time = 50;
    int TimeB = 500;
    int TimeC = 50000;
    int seconds = 0;
    String DevName;
    CountDownTimer countDownTimer;



    String ThreadName = "A";
    static final int STATE_CONNECTING = 1;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_WRITE = 6;
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
    }

    private void startServer(String servername) {

        if(servername=="A")
        {
            ServerClassA serverClassA=new ServerClassA();
            serverClassA.start();
        }
        if(servername=="B")
        {
            ServerClassB serverClassB=new ServerClassB();
            serverClassB.start();
        }
        if(servername=="C")
        {
            ServerClassC serverClassC=new ServerClassC();
            serverClassC.start();
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


    @Override
    protected void onPause() {
        super.onPause();
        countDownTimer.cancel();
    }
}