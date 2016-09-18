package herblive.herbapp;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerService extends Service {

    private static final String TAG = "HERB_SERVICE";
    private static final String IP = "192.168.0.80";
    private static final int PORT = 2017;

    int mInterval = 1000;

    private Socket mSock = null;
    private BufferedReader mReader;

    private LocalBroadcastManager broadcaster;


    static final public String COPA_RESULT = "com.controlj.copame.backend.COPAService.REQUEST_PROCESSED";
    static final public String COPA_MESSAGE = "com.controlj.copame.backend.COPAService.COPA_MSG";

    public void sendResult(String message) {
        Intent intent = new Intent(COPA_RESULT);
        if(message != null)
            intent.putExtra(COPA_MESSAGE, message);
        broadcaster.sendBroadcast(intent);
    }

    Handler mTimer = new Handler(){
        public void handleMessage(Message msg){
            Log.d(TAG, "TIMER");
            String s;
            try{
                Log.d(TAG,mReader.readLine());
            }catch(Exception e){
                Log.d(TAG, "RECEIVE ERROR");
            }
            mTimer.sendEmptyMessageDelayed(0, mInterval);
        }
    };

    private Thread checkUpdate = new Thread() {

        public void run() {
            try {
                String line;
                Log.d(TAG, "Start Thread");
                while (true) {
                    Log.d(TAG, "chatting is running");
                    line = mReader.readLine();
                    if(line!=null){
                        Log.d(TAG, line);
                        sendResult(line);
                    }
                }
            } catch (Exception e) {

            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d(TAG, "start service.");

        new ConnectTask().execute();

        //mTimer.sendEmptyMessageDelayed(0, mInterval);
        broadcaster = LocalBroadcastManager.getInstance(this);

        return Service.START_NOT_STICKY;
    }

    public void onDestroy(){
        Log.d(TAG, "stop service.");
        mTimer.removeMessages(0);
    }

    private class ConnectTask extends AsyncTask<String,String,String>{
        @Override
        protected String doInBackground(String... arg){
            Log.d(TAG, "TRY! connect!");
            try {
                mSock = new Socket(IP, PORT);
                mReader = new BufferedReader(new InputStreamReader(mSock.getInputStream()));
                Log.d(TAG, "SOCKET CONNECTED");
            }catch(Exception e){
                Log.d(TAG, "Socket connect Error");
            }


            checkUpdate.start();
            return "GET";
        }
    }

    public void sendHandler(Handler h){

    }
}
