package herblive.herbapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by greendeer on 16. 9. 15.
 */
public class Bluetooth {

    private static final String TAG = "HERB_BLUETOOTH";

    // Debugging
    static final int ACTION_ENABLE_BT = 101;
    final static int ACT_SUB = 3;

    // Intent request code
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private BluetoothAdapter btAdapter;

    private Activity mActivity;
    private Handler mHandler;

    static final String BLUE_NAME = "BluetoothEx";
    static final UUID BLUE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //fb155bc

    public ConnectedThread mThread = null;

    // Constructors
    public Bluetooth(Activity ac, Handler h) {
        mActivity = ac;
        mHandler = h;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void socketOpen(String addr){
        if(mThread!=null){
            mThread.cancel();}
        mThread=null;

        if(mThread!=null)return;
        BluetoothDevice device = btAdapter.getRemoteDevice(addr);
        BluetoothSocket blesock;
        try{
            blesock = device.createInsecureRfcommSocketToServiceRecord(BLUE_UUID);
            Log.d(TAG, "sock opened!");
        }catch(IOException e){
            Log.d(TAG, "sock open failed!");
            return;
        }
        mThread = new ConnectedThread(blesock);
    }
    //private class ConnectedThread extends Thread {
    private class ConnectedThread {
        private final BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                mmSocket.connect();
            }catch (IOException e){
                try{
                    mmSocket.close();
                }catch (IOException e2){

                }
                return;
            }

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

            Log.d(TAG, "thread generate complete");
        }

//        public void run() {
//            byte[] buffer = new byte[1024];  // buffer store for the stream
//            int bytes; // bytes returned from read()
//
//            // Keep listening to the InputStream until an exception occurs
//            while (true) {
//                try {
//                    // Read from the InputStream
//                    bytes = mmInStream.read(buffer);
//                    // Send the obtained bytes to the UI activity
//                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//                            .sendToTarget();
//                } catch (IOException e) {
//                    break;
//                }
//            }
//        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String strBuf) {
            try {
                byte[] buffer = strBuf.getBytes();
                mmOutStream.write(buffer);
                Log.d(TAG, "send messege");
            } catch (IOException e) {
                Log.d(TAG, "send failed"); }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    public void msgWrite(String buf){
        if(mThread == null) return;
        mThread.write(buf);
    }

    public void socketClose(){
        try {
            mThread.mmSocket.close();
        } catch (IOException e) { }
    }
}
