package com.android.settings.wifi.p2p;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import com.android.settings.R;


/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the the p2p peer and writing the file. Inspired from wifi
 * direct demo.
 */
public class FileTransferService extends IntentService {

    private static String TAG= "WifiP2pSettings.FileTransferService";
    private static final int SOCKET_TIMEOUT = 10000;
    public static final String ACTION_SEND_FILE = "com.android.settings.wifi.p2p.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String PEER_ADDRESS = "peer_address";
    public static final String PEER_PORT = "peer_port";
    public static final String FILE_NOT_FOUND = "FILE NOT FOUND";
    public static final String UNKNOWN_URI = "UNKNOWN_URI";
    public static final String UNKNOWN_FILE_NAME = "UNKNOWN_FILE_NAME";
    public static final String UNKNOWN_HOST_NAME = "UNKNOWN_FILE_NAME";
    public static final int UNDEFINED_PORT = -1;
    private NotificationManager nManager;

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService( ) {
        super("WifiP2pSettings.FileTransferService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "In oncreate of File transfer service");
        nManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private String getFileNameFromUri(Uri fileUri) {
        String fileName = FILE_NOT_FOUND;
        String absolutePath = getPathFromUri(fileUri);
        if (absolutePath != null) {
            fileName = new File(absolutePath).getName();
        }
        return fileName;
    }

     private String getPathFromUri(Uri fileUri) {
        String path = null;
        String[] projection = {MediaStore.MediaColumns.DATA};
        ContentResolver cr = getApplicationContext().getContentResolver();
        Cursor metaCursor = cr.query(fileUri,
                projection, null, null, null);
        if (metaCursor != null) {
            try {
                if (metaCursor.moveToFirst()) {
                    path = metaCursor.getString(0);
                }
            } finally {
                metaCursor.close();
            }
        }
        return path;
    }

    private void  displayBeginningOfTransferMessage(String fileUri) {
        Resources r = getResources();
        Notification notif = new Notification.Builder(this)
        .setContentTitle(r.getString(R.string.wifi_p2p_send_begin)+ getFileNameFromUri(Uri.parse(fileUri)))
        .setContentText(r.getString(R.string.wifi_p2p_send_begin)+ getFileNameFromUri(Uri.parse(fileUri)))
        .setSmallIcon(R.drawable.ic_tab_selected_download)
        .build();
        notif.when = System.currentTimeMillis();
        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        notif.tickerText = r.getString(R.string.wifi_p2p_send_begin)+ getFileNameFromUri(Uri.parse(fileUri));
        notif.defaults = 0; // please be quiet
        notif.sound = null;
        // After a 100ms delay then vibrate for 500ms
        notif.vibrate = new long[]{100,500};
        notif.priority = Notification.PRIORITY_HIGH;
        nManager.notify(R.drawable.ic_tab_selected_download,notif);
    }

    private void displayEndOfTransferMessage(String fileUri) {
        Resources r = getResources();
        Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(fileUri)
                    , "image/*");
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notif = new Notification.Builder(this)
        .setContentTitle(r.getString(R.string.wifi_p2p_send_end)+ getFileNameFromUri(Uri.parse(fileUri)))
        .setContentText(r.getString(R.string.wifi_p2p_send_end)+ getFileNameFromUri(Uri.parse(fileUri)))
        .setSmallIcon(R.drawable.ic_tab_selected_download)
        .setContentIntent(pi)
        .build();
        notif.when = System.currentTimeMillis();
        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        notif.tickerText = r.getString(R.string.wifi_p2p_send_end)+ getFileNameFromUri(Uri.parse(fileUri));
        notif.defaults = 0; // please be quiet
        notif.sound = null;
        // After a 100ms delay, vibrate for 200ms 3 times
        notif.vibrate = new long[]{100, 200, 100, 200,100,200};
        notif.priority = Notification.PRIORITY_HIGH;
        nManager.notify(R.drawable.ic_tab_selected_download,notif);
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String fileUri = UNKNOWN_URI;
            String fileName = UNKNOWN_FILE_NAME;
            String host = UNKNOWN_HOST_NAME;
            int port = UNDEFINED_PORT;
            Socket socket = new Socket();
            Bundle b = intent.getExtras();
            if (b != null) {
                fileUri = b.getString(EXTRAS_FILE_PATH);
                fileName = getFileNameFromUri(Uri.parse(fileUri));
                host = b.getString(PEER_ADDRESS);
                port = b.getInt(PEER_PORT);
            }
            Log.d(TAG, "Starting file transfer service: host "+host+" port: "+port);
            try {
                Log.d(TAG, "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
                Log.d(TAG, "Client socket - " + socket.isConnected());
                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                DataOutputStream dataoutputStream = new DataOutputStream(stream);
                displayBeginningOfTransferMessage(fileUri);
                dataoutputStream.writeUTF(fileName);
                InputStream is = cr.openInputStream(Uri.parse(fileUri));
                if (is != null) {
                    FileTransferServerService.copyFile(is, stream);
                }
                Log.d(TAG, "Client: Data written, file: "+fileName);
                displayEndOfTransferMessage(fileUri);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException ioe) {
                    Log.e(TAG, ioe.getMessage());
                }
            }
        }
    }
}