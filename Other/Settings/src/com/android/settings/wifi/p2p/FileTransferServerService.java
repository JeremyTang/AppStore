package com.android.settings.wifi.p2p;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import com.android.settings.R;

public class FileTransferServerService extends Service {
    private static String TAG = "WifiP2pSettings.FileTransferServerService";
    public static final String ACTION_START_SERVER_THREAD = "com.android.settings.wifi.p2p.ACTION_START_SERVER_THREAD";
    public static final String PEER_PORT = "peer_port";
    public static final int WRONG_PORT = -1;
    private NotificationManager nManager;
    private boolean taskIsRunning;
    private  ServerSocket serverSocket;
    private Socket client;
    Resources appResources;
    private final IntentFilter mIntentFilter = new IntentFilter();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null && !networkInfo.isConnected()) {
                    stopServerSocketThread();
                }
            }
        }
    };

    private void displayBeginningOfTransferMessage(String fileUri) {
        Notification notif = new Notification.Builder(this)
        .setContentTitle(appResources.getString(R.string.wifi_p2p_receive_begin)+fileUri)
        .setContentText(appResources.getString(R.string.wifi_p2p_receive_begin)+ fileUri)
        .setSmallIcon(R.drawable.ic_tab_selected_download)
        .build();
        notif.when = System.currentTimeMillis();
        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        notif.tickerText = appResources.getString(R.string.wifi_p2p_receive_begin)+fileUri;
        notif.defaults = 0; // please be quiet
        notif.sound = null;
        // After a 100ms delay, vibrate for 500ms
        notif.vibrate = new long[]{100,500};
        notif.priority = Notification.PRIORITY_HIGH;
        nManager.notify(R.drawable.ic_tab_selected_download,notif);
    }

    private void displayEndOfTransferMessage(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            intent.setDataAndType(Uri.parse("file://"+ file.getCanonicalPath())
                    , "image/*");
        } catch (IOException e) {
            Log.e(TAG, "Did not get fiel path: "+file);
        }
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notif = new Notification.Builder(this)
        .setContentTitle(appResources.getString(R.string.wifi_p2p_receive_end)+file.getName())
        .setContentText(appResources.getString(R.string.wifi_p2p_receive_end)+ file.getName())
        .setSmallIcon(R.drawable.ic_tab_selected_download)
        .setContentIntent(pi)
        .build();
        notif.when = System.currentTimeMillis();
        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        notif.tickerText = appResources.getString(R.string.wifi_p2p_receive_end)+file.getName();
        notif.defaults = 0; // please be quiet
        notif.sound = null;
        // After a 100ms delay, vibrate for 200ms, repeat 3 times
        notif.vibrate = new long[]{100, 200, 100, 200,100,200};
        notif.priority = Notification.PRIORITY_HIGH;
        nManager.notify(R.drawable.ic_tab_selected_download,notif);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        nManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        appResources = getResources();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopServerSocketThread();
    }

    private void startServerSocketThread(int port) {
        try {
            serverSocket = new ServerSocket(port);
            taskIsRunning = true;
            Log.i(TAG, "Server socket created");
        } catch (IOException e) {
            Log.e(TAG, "Error opening server socket");
            taskIsRunning = false;
        }
        new Thread() {
            public void run() {
                try {
                    while (taskIsRunning) {
                        client = serverSocket.accept();
                        Log.d(TAG, "Server: connection done");
                        InputStream inputstream = client.getInputStream();
                        DataInputStream dataInputStream = new DataInputStream(inputstream);
                        String fileName = dataInputStream.readUTF();
                        final File f = new File(Environment.getExternalStorageDirectory() +
                                File.separator+"DCIM"+File.separator+"wifip2psettings-"+
                                + System.currentTimeMillis()
                                + fileName);
                        String parent = f.getParent();
                        if (parent != null) {
                            File dirs = new File(parent);
                            if (!dirs.exists())
                                dirs.mkdirs();
                        }
                        f.createNewFile();
                        displayBeginningOfTransferMessage(fileName);
                        copyFile(inputstream, new FileOutputStream(f));
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                                Uri.parse("file://"+ Environment.getExternalStorageDirectory()+File.separator+"DCIM")));
                        displayEndOfTransferMessage(f);
                        Log.i(TAG, "Server: File received: "+fileName);
                        client.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    try {
                        serverSocket.close();
                        if (client != null)
                            client.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing server or client socket: "+e.getMessage());
                    }
                }
            }
        }.start();
    }

    private void stopServerSocketThread() {
        taskIsRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
               Log.e(TAG, "Error closing server socket!!");
            }
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && action.equals(ACTION_START_SERVER_THREAD)) {
                int port = intent.getIntExtra(PEER_PORT, WRONG_PORT);
                if (port == WRONG_PORT) {
                    Log.e(TAG, "The port was not specified, connot start server socket");
                } else {
                    if (!taskIsRunning) startServerSocketThread(port);
                }
            }
        }
        return ret;
    }

    public static void copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } finally {
            try {
                out.close();
            } catch (IOException ioe) {
                Log.e(TAG, ioe.getMessage());
            }
            try {
                inputStream.close();
            } catch (IOException ioe) {
                Log.e(TAG, ioe.getMessage());
            }
        }
    }
}