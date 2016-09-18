package herblive.herbapp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "HERB_MAIN";
    private static final int DEVICE_FIND = 7;
    private static final int BLUETOOTH_ENABLE = 8;

    EditText mEditData;
    TextView sun;
    TextView temp;
    TextView thirst;
    TextView textMsg;

    private static final int SUN = 21;
    private static final int TEMP = 22;
    private static final int THIRST = 23;

    Bluetooth mBT = null;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "BROADCAST RECEIVE!");
            String s = intent.getStringExtra(ServerService.COPA_MESSAGE);
            // do something here.
            if(mBT!=null) {
                mBT.msgWrite(s);
            }
            //parsing
            parseMsg(s);
        }
    };

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handler on!");
            String str;
            switch (msg.what) {
                case SUN:
                    str = (String) msg.obj;
                    sun.setText(str);
                    break;
                case TEMP:
                    str = (String) msg.obj;
                    temp.setText(str);
                    break;
                case THIRST:
                    str = (String) msg.obj;
                    thirst.setText(str);
                    break;
            }
        }

    };
    // #T3,B2,S2; (t- temp. b- light, s-thirst) M1 light. M2 Temp. M3 Thirst.
    // watering #M4;
    private void parseMsg(String s){
        if(s.startsWith("#") && s.endsWith(";")){
            int ind = s.indexOf("B");
            if(ind!=-1){
                //sun.setText(s.charAt(ind+1));
                char t = s.charAt(ind+1);
                Character ch = new Character(t);
                String str = ch.toString();
                Message msg = mHandler.obtainMessage(SUN, str);
                mHandler.sendMessage(msg);
            }
            ind = s.indexOf("T");
            if(ind!=-1){
                //temp.setText(s.charAt(ind+1));
                char t = s.charAt(ind+1);
                Character ch = new Character(t);
                String str = ch.toString();
                Message msg = mHandler.obtainMessage(TEMP, str);
                mHandler.sendMessage(msg);
            }
            ind = s.indexOf("S");
            if(ind!=-1){
                //thirst.setText(s.charAt(ind+1));
                char t = s.charAt(ind+1);
                Character ch = new Character(t);
                String str = ch.toString();
                Message msg = mHandler.obtainMessage(THIRST, str);
                mHandler.sendMessage(msg);
            }
            Log.d(TAG, "parse : "+s);
        }else{
            return;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditData = (EditText)findViewById(R.id.sendMsg);

        sun = (TextView) findViewById(R.id.lightLV);
        temp = (TextView) findViewById(R.id.tempLV);
        thirst = (TextView) findViewById(R.id.thirstLV);
        textMsg = (TextView) findViewById(R.id.TextMsg);

        //CHECK PERMISSION
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        2);
            }
        }

        Intent intent = new Intent(this, ServerService.class);
        startService(intent);
//
//        Log.d(TAG, "register receiver");
//        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
//                new IntentFilter(ServerService.COPA_RESULT)
//        );

    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onStop() {

        Log.d(TAG, "unregister receiver");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "register receiver");
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(ServerService.COPA_RESULT)
        );
        super.onResume();
    }



    protected void onActivityResult(int req, int res, Intent data){
        switch(req){
            case BLUETOOTH_ENABLE:
                if(res == RESULT_OK){
                    Log.d(TAG, "Bluetooth can be used.");
                }else{
                    isBluetooth();
                }

                break;
            case DEVICE_FIND:
                if (res == Activity.RESULT_OK) {
                    if(mBT == null) {
                        mBT = new Bluetooth(this, mHandler);
                    }
                    String mAddr = data.getStringExtra("ADDR");
                    mBT.socketOpen(mAddr);
                    Log.d(TAG, "connected device");
                    textMsg.setText("BLUETOOTH connected");
                    String str = "#B"+sun.getText().toString()+",T"+temp.getText().toString()+",S"+thirst.getText().toString()+";";
                    mBT.msgWrite(str);

                } else {
                    Log.d(TAG, "can't not connect device");
                }
                break;
        }
    }

    public void sendM(View v){
        switch(v.getId()){
            case R.id.sendBT:{
                String strBuf = mEditData.getText().toString();
                if(strBuf.length()<1) return;
                if(mBT==null){
                    return;
                }else{
                    mEditData.setText("");
                    mBT.msgWrite(strBuf);
                }
                break;
            }
        }
    }

    public void conBT(View v){
        switch(v.getId()){
            case R.id.btCNT:{

                while(!isBluetooth()){}
                Intent intent2 = new Intent (getApplicationContext(),DeviceFindActivity.class);
                startActivityForResult(intent2, DEVICE_FIND);
                break;
            }
        }
    }

    public boolean isBluetooth(){
        BluetoothAdapter mBA = BluetoothAdapter.getDefaultAdapter();
        if (mBA == null) {
            Log.d(TAG, "Bluetooth not founded.");
            return false;
        }

        if(mBA.isEnabled()){
            Log.d(TAG, "Bluetooth can be used.");
            return true;
        }

        Intent intent = new Intent (BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, BLUETOOTH_ENABLE);
        return false;
    }

}
