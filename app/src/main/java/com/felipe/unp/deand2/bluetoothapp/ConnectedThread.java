package com.felipe.unp.deand2.bluetoothapp;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class ConnectedThread extends Thread{
//    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private byte[] mmBuffer; // mmBuffer store for the stream

    public ConnectedThread(BluetoothSocket socket) {
//        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
//            Log.e(Tag, "Error occurred when creating input stream", e);
            e.printStackTrace();
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
//            Log.e(Tag, "Error occurred when creating output stream", e);
            e.printStackTrace();

        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        /*mmBuffer = new byte[1024];
        int numBytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                // Read from the InputStream.
                numBytes = mmInStream.read(mmBuffer);
                // Send the obtained bytes to the UI activity.
                Message readMsg = handler.obtainMessage(
                        MessageConstants.MESSAGE_READ, numBytes, -1,
                        mmBuffer);
                readMsg.sendToTarget();
            } catch (IOException e) {
//                Log.d(Tag, "Input stream was disconnected", e);
                e.printStackTrace();

                break;
            }
        }*/
    }

    // Call this from the main activity to send data to the remote device.
    public void write(String msg) {
        try {
            mmOutStream.write(msg.getBytes());

            //Share the sent message with the UI activity
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}