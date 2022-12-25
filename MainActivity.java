package com.example.clientserver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


import androidx.appcompat.app.AppCompatActivity;

//===================================================
// JAVA Client-Server
//
//  SERVER
//
//===================================================

@SuppressLint("SetTextI18n") // stands for "Internationalization"
public class MainActivity extends AppCompatActivity {
    ServerSocket serverSocket;
    Thread Thread1 = null;
    TextView tvIP, tvPort; // TextView
    TextView tvMessages; // TextView
    EditText etMessage; // EditText
    Button btnSend;
    public static String SERVER_IP = "";
    public static final int SERVER_PORT = 8080; // Port web 8080
    String message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvIP = findViewById(R.id.tvIP);
        tvPort = findViewById(R.id.tvPort);
        tvMessages = findViewById(R.id.tvMessages);
        etMessage = findViewById(R.id.etMessage); // Entered Message
        btnSend = findViewById(R.id.btnSend);
        
        try {
            SERVER_IP = getLocalIpAddress(); // Method private String getLocalIpAddress()
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Thread1 = new Thread(new Thread1()); // Use Threads - class Thread1 implements Runnable
        Thread1.start();
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = etMessage.getText().toString().trim(); // Entered message in EditText. Trim() - Returns a string whose value is this string, with all leading and trailing space removed
                if (!message.isEmpty()) {
                    new Thread(new Thread3(message)).start(); // SEND message
                }
            }
        });



    } //onCreate



    private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress(); // Get IP address
        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
    }



    private PrintWriter output; // Prints formatted representations of objects to a text-output stream.
    private BufferedReader input;



    class Thread1 implements Runnable { // Main I/O class
        @Override
        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVER_PORT); // Create socket. ServerSocket defined in class MainActivity extends AppCompatActivity
                runOnUiThread(new Runnable() { // Write text to TextView in UI thread
                    @Override
                    public void run() {
                        tvMessages.setText("Not connected");
                        tvIP.setText("IP: " + SERVER_IP);
                        tvPort.setText("Port: " + String.valueOf(SERVER_PORT));
                    }
                });
                try {
                    socket = serverSocket.accept(); // Waits/blocks until a client connects
                    output = new PrintWriter(socket.getOutputStream()); // Create output strean
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Create input strean
                    runOnUiThread(new Runnable() { // Write text to TextView in UI thread
                        @Override
                        public void run() {
                            tvMessages.setText("Connected");
                        }
                    });
                    new Thread(new Thread2()).start(); // RECEIVE
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } // class Thread1


    private class Thread2 implements Runnable { // Use input stream. RECEIVE
        @Override
        public void run() {
            while (true) {
                try {
                    final String message = input.readLine(); // Read input stream
                    if (message != null) {
                        // Write text to TextView in UI thread
                        runOnUiThread(new Runnable() { // Runs the specified action on the UI thread https://developer.android.com/reference/android/app/Activity
                            @Override
                            public void run() {
                                tvMessages.append("client:" + message + "");
                            }
                        });
                    } else {
                        Thread1 = new Thread(new Thread1());
                        Thread1.start();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } // while (true)
        }
    } // private class Thread2


    class Thread3 implements Runnable { // Use output stream. SEND
        private String message;
        Thread3(String message) {
            this.message = message;
        }
        @Override
        public void run() {
            output.write(message); // Write massage to output stream
            output.flush(); // Flushes the stream
            runOnUiThread(new Runnable() { // Write to TextView in UI thread
                @Override
                public void run() {
                    tvMessages.append("server: " + message + "");
                            etMessage.setText("");
                }
            });
        }
    } // class Thread3


} //MainActivity
