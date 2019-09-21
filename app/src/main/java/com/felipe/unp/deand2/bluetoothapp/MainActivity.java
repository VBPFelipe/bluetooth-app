package com.felipe.unp.deand2.bluetoothapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothDevice bluetoothDevice = null;
    BluetoothAdapter bluetoothAdapter = null;
    BluetoothSocket bluetoothSocket = null;

    UUID uuid;

    ConnectedThread connectedThread;

    Switch swOnOff;
    ImageView ivbluetooth;
    Button btnPaired, btnConnect, btnSend;
    EditText edtSend;
    ListView lstPairedDevices;
    public ArrayAdapter<String> lstAdapter;
    public ArrayList<String> btData = new ArrayList<>();
    String[] macAddrs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
        //Bluetooth via serial port
        uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        //Bluetooth via RFCOMM
//        uuid = UUID.fromString("00000003-0000-1000-8000-00805f9b34fb");

        // Initiates objects from components on screen
        intializer();

        //Creates an bluetooth adapter instance
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //Verifies if the bluetooth is accessible
        if(bluetoothAdapter == null){
            msgToasts("Bluetooth is not accessible");
        }else{
            msgToasts("Bluetooth is accessible");
        }

        //Verifies if the bluetooth is enabled
        try {
            if(bluetoothAdapter.isEnabled()){
                msgToasts("Bluetooth is enabled");
                ivbluetooth.setImageResource(R.drawable.ic_bluetooth_on);
                swOnOff.setChecked(true);
            }
            else{
                msgToasts("Bluetooth is disabled");
                ivbluetooth.setImageResource(R.drawable.ic_bluetooth_off);
                swOnOff.setChecked(false);
            }
        }catch (Exception e){
            e.printStackTrace();
            msgToasts(e.getMessage());
        }

        // Switch On/Off events
        /* Switch is watched on each event, which generates an Intent
            and makes a request to the device feature.
            Enables/disables Bluetooth
         */
        swOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swOnOff.isChecked()){
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, 0);
                } else if(!swOnOff.isChecked()){
                    if(bluetoothAdapter.isEnabled()) {
                        bluetoothAdapter.disable();
                        msgToasts("Bluetooth disabled");
                        ivbluetooth.setImageResource(R.drawable.ic_bluetooth_off);
                        btnConnect.setEnabled(false);
                        btnPaired.setEnabled(false);
                        btnSend.setEnabled(false);
                        edtSend.setEnabled(false);
                        //Clear the list
                        if(!btData.isEmpty()){
                            btData.clear();
                            lstAdapter.clear();
                        }
                    }
                }
            }
        });

        /* Pairement Button is watched on each event, which generates an Intent
            and makes a request to the device feature.
            Shows a list of other paired devices with the bluetooth request device.
         */
        btnPaired = (Button) findViewById(R.id.btn_paired);
        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bluetoothAdapter.isDiscovering()){
                    Intent intent2 = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent2, 1);
                }
            }
        });


        /*
        *
        * */
        lstPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                macAddrs = lstAdapter.getItem(position).split(",");
                bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddrs[1].trim());
                msgToasts("MAC Address: "+macAddrs[1].trim());
                btnConnect.setEnabled(true);
            }
        });

        /* Connection Button is watched on each event, which generates an Intent
            and
         */
        btnConnect = (Button) findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swOnOff.isChecked() && btnConnect.getText().equals("BT Conectar")) {
                    try {
                        bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                        bluetoothSocket.connect();
                        ivbluetooth.setImageResource(R.drawable.ic_bluetooth_connect);
                        msgToasts("Connected with the device: " + macAddrs[1].trim());
                        btnConnect.setText("BT Desconectar");
                        btnSend.setEnabled(true);
                        edtSend.setEnabled(true);

                        // Invoke the Thread
                        connectedThread = new ConnectedThread(bluetoothSocket);
                        connectedThread.start();


                    } catch (IOException e) {
                        e.printStackTrace();
                        msgToasts(e.getMessage());
                    }
                } else{
                    try {
                        bluetoothSocket.close();
                        btnConnect.setText("BT Conectar");
                        ivbluetooth.setImageResource(R.drawable.ic_bluetooth_on);
                        msgToasts("Disconnected from the device.");
                        btnSend.setEnabled(false);
                        edtSend.setEnabled(false);
                    }catch(IOException e){
                        e.printStackTrace();
                        msgToasts(e.getMessage());
                    }
                }
            }
        });

        btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtData = edtSend.getText().toString();
                if(!txtData.isEmpty()){
                    connectedThread.write(txtData);
                }else{
                    msgToasts("Please, insert some information to send through Bluetooth");
                }
            }
        });

    }

    /**
     * Initiates the objects from application component
     * */
    private void intializer() {
        swOnOff = (Switch) findViewById(R.id.swt_on_off);
        ivbluetooth = (ImageView) findViewById(R.id.iv_bluetooth);
        lstPairedDevices = (ListView) findViewById(R.id.lst_paired_devices);
        edtSend = (EditText) findViewById(R.id.edt_send);
    }

    /**
     * Exhibits messages
     * */
    private void msgToasts(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case 0:
                //
                if(resultCode==RESULT_OK) {
                    msgToasts("Bluetooth is activated");
                    ivbluetooth.setImageResource(R.drawable.ic_bluetooth_on);
//                    btnConnect.setEnabled(true);
                    btnPaired.setEnabled(true);
                }
                else{
                    msgToasts("Bluetooth is deactivated");
                    ivbluetooth.setImageResource(R.drawable.ic_bluetooth_off);
                }
                break;
            case 1:
                if(resultCode==120){//120 é o código igual ao tempo de discovery
                    if(bluetoothAdapter.isEnabled()){
                        btData.clear();
                        btData = new ArrayList<>();
                        Set<BluetoothDevice> btDevices = bluetoothAdapter.getBondedDevices();

                        for(BluetoothDevice btDevice : btDevices){
                            btData.add(btDevice.getName()+", "+btDevice.getAddress());
                        }

                        lstAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, btData);
                        lstPairedDevices.setAdapter(lstAdapter);

                        if(btData.isEmpty()){
                            btnConnect.setEnabled(true);
                        }
                    }
                    else
                        msgToasts("Bluetooh is not activated");
                }
                break;
            default:
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
