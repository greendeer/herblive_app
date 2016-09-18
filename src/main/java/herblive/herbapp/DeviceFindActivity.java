package herblive.herbapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

public class DeviceFindActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "HERB_DEVICEFIND";


    BluetoothAdapter mBA;
    ListView mListDevice;
    ArrayList<String> mArDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_find);

        mBA = BluetoothAdapter.getDefaultAdapter();
        initListView();
        getParedDevice();
    }

    public void getParedDevice(){
        Set<BluetoothDevice> devices = mBA.getBondedDevices();
        for(BluetoothDevice device : devices){
            addDeviceToList(device.getName(), device.getAddress());
        }

        startFindDevice();
    }

    private final BroadcastReceiver mBlueRecv = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() != BluetoothDevice.BOND_BONDED){
                    addDeviceToList(device.getName(), device.getAddress());
                }
            }
        }
    };

    public void startFindDevice(){
        stopFindDevice();
        Log.d(TAG, "start finding device");
        mBA.startDiscovery();
        IntentFilter intFilter = new IntentFilter();
        intFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBlueRecv, intFilter);
    }

    public void stopFindDevice(){
        if(mBA.isDiscovering()){
            mBA.cancelDiscovery();
            unregisterReceiver(mBlueRecv);
        }
        Log.d(TAG, "stop finding device");
    }

    public void addDeviceToList(String name, String address){
        String deviceInfo = name + " - " + address;
        //Log.d("tag1", "Device FInd : "+deviceInfo);
        mArDevice.add(deviceInfo);
        ArrayAdapter adapter = (ArrayAdapter)mListDevice.getAdapter();
        adapter.notifyDataSetChanged();

    }

    public void initListView(){
        mArDevice = new ArrayList<String>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mArDevice);
        mListDevice = (ListView) findViewById(R.id.listView);
        mListDevice.setAdapter(adapter);
        mListDevice.setOnItemClickListener(this);
    }

    public void onItemClick(AdapterView parent, View view, int position, long id){
        String strItem = mArDevice.get(position);

        int pos = strItem.indexOf(" - ");
        if(pos <= 0) return;
        String address = strItem.substring(pos+3);

        Log.d(TAG, "connecting to "+address);

        stopFindDevice();

        Intent intent = new Intent();
        intent.putExtra("ADDR", address);
        setResult(Activity.RESULT_OK, intent);

        finish();
        ///finish subactivity!

    }



    protected void onDestroy() {
        super.onDestroy();

        // Unregister broadcast listeners
        try{

            unregisterReceiver(mBlueRecv);

        }catch(IllegalArgumentException e){}
    }


}
