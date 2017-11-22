package com.exsitu.inria.jriviere.workshopsensorstoudp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    SensorManager mSensorManager;

    private static final int OSC = 0;
    private static final int UDP = 1;


    Sensor gyroscopeDefault;
    Sensor mAccelerometer;
    Sensor orientation;
    Sensor proximite;
    UDPClient udpClient;
    TextView infoTextView;
    int packetNumber = 0;

    RadioButton radioBtnOSC;
    RadioButton radioBtnUDP;
    TextView textViewPort;
    TextView textViewIP;
    Switch switchEnabled;

    private int currentProtocole;

    // To keep track of activity's window focus
    boolean currentFocus;

    // To keep track of activity's foreground/background status
    boolean isPaused;

    Handler collapseNotificationHandler;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        System.out.println("Start the service....");

        //ServiceTest serviceTest = new ServiceTest(this);
        Intent intent = new Intent(this, ServiceTest.class);
        startService(intent);

        initComponentSensors();
        initComponentUI();
        initNetwork();


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        currentFocus = hasFocus;

        if (!hasFocus) {

            // Method that handles loss of window focus
            collapseNow();
        }
    }

    public void collapseNow() {

        // Initialize 'collapseNotificationHandler'
        if (collapseNotificationHandler == null) {
            collapseNotificationHandler = new Handler();
        }

        // If window focus has been lost && activity is not in a paused state
        // Its a valid check because showing of notification panel
        // steals the focus from current activity's window, but does not
        // 'pause' the activity
        if (!currentFocus && !isPaused) {

            // Post a Runnable with some delay - currently set to 300 ms
            collapseNotificationHandler.postDelayed(new Runnable() {

                @Override
                public void run() {

                    // Use reflection to trigger a method from 'StatusBarManager'

                    Object statusBarService = getSystemService("statusbar");
                    Class<?> statusBarManager = null;

                    try {
                        statusBarManager = Class.forName("android.app.StatusBarManager");
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    Method collapseStatusBar = null;

                    try {

                        // Prior to API 17, the method to call is 'collapse()'
                        // API 17 onwards, the method to call is `collapsePanels()`

                        if (Build.VERSION.SDK_INT > 16) {
                            collapseStatusBar = statusBarManager .getMethod("collapsePanels");
                        } else {
                            collapseStatusBar = statusBarManager .getMethod("collapse");
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                    collapseStatusBar.setAccessible(true);

                    try {
                        collapseStatusBar.invoke(statusBarService);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }

                    // Check if the window focus has been returned
                    // If it hasn't been returned, post this Runnable again
                    // Currently, the delay is 100 ms. You can change this
                    // value to suit your needs.
                    if (!currentFocus && !isPaused) {
                        collapseNotificationHandler.postDelayed(this, 100L);
                    }

                }
            }, 300L);
        }
    }
   
    @Override
    public void onBackPressed(){
       // Toast.MakeText(getApplicationContext(),"You Are Not Allowed to Exit the App", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        return false;
    }



    private void initComponentSensors() {
        List<Sensor> sensors;

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Pour trouver un capteur spécifique
        gyroscopeDefault = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mAccelerometer =    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        orientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        proximite = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    private void initNetwork() {
        udpClient = UDPClient.getInstance();
        udpClient.run();
    }

    private void initComponentUI() {

        radioBtnOSC = (RadioButton) findViewById(R.id.radioButtonOSC);
        radioBtnUDP = (RadioButton) findViewById(R.id.radioButtonUDP);
        textViewPort = (EditText) findViewById(R.id.editTextPort);
        textViewIP  = (EditText) findViewById(R.id.editTextIP);
        infoTextView = (TextView) findViewById(R.id.textView3);
        switchEnabled = (Switch) findViewById(R.id.switchEnable) ;

        radioBtnOSC.setChecked(true);

        radioBtnOSC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentProtocole = OSC;
            }
        });

        radioBtnUDP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentProtocole = UDP;
            }
        });

        textViewPort.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    // is actually the old value
                    udpClient.setPort(Integer.parseInt( textViewPort.getText().toString()));
                    refreshInfoTextView();
                }
                return false;
            }
        });


        textViewIP.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    // is actually the old value
                    udpClient.setIP( textViewIP.getText().toString());
                    refreshInfoTextView();
                }
                return false;
            }
        });

        switchEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position

                if (isChecked) {
                    radioBtnOSC.setEnabled(false);
                    radioBtnUDP.setEnabled(false);
                    textViewIP.setEnabled(false);
                    textViewPort.setEnabled(false);
                } else {
                    radioBtnOSC.setEnabled(true);
                    radioBtnUDP.setEnabled(true);
                    textViewIP.setEnabled(true);
                    textViewPort.setEnabled(true);
                }
            }});


    }

    private void refreshInfoTextView() {
        infoTextView.setText("sending data... id : " + packetNumber + "  to port : " + udpClient.getPort()+ "\nprotocole : " + currentProtocoleToString()
                + "\n IP : " + udpClient.getIP() );
    }


    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
         mSensorManager.registerListener(this, gyroscopeDefault, SensorManager.SENSOR_DELAY_NORMAL);
         mSensorManager.registerListener(this,orientation,SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this,proximite,SensorManager.SENSOR_DELAY_NORMAL);

    }



    protected void onPause() {
        super.onPause();
     //   mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        packetNumber ++;
        String message = "";
        if ( event.sensor.getType() == Sensor.TYPE_GYROSCOPE ){
                message = "gyro " + event.values[0] + " " + event.values[1] + " " + event.values[2] ;
              //  System.out.println("gyro" + " " + event.values[0] + " " + event.values[1] + " " + event.values[2] + ";");
        } else if( event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            message = "accelero " + event.values[0] + " " + event.values[1] + " " + event.values[2] ;
        //   System.out.println("accelero" + " " + event.values[0] + " " + event.values[1] + " " + event.values[2] + ";");
        }  else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION){
            float degree = Math.round(event.values[0]);
           // System.out.println( "degree : " +  degree + " "      );
            message = "degree "+Math.round(event.values[0])+" "+Math.round(event.values[1])+" "+Math.round(event.values[2]) ;
        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY){
            message = "proximite "+ event.values[0]  ;
            //System.out.println(message);
        }



        refreshInfoTextView();
        sendMessage(message);



    }

    private String currentProtocoleToString() {
        return currentProtocole==1?"UDP":"OSC";

    }

    private void sendMessage(String message) {
        if(currentProtocole == OSC){
            udpClient.sendMessageOSC(message);
        } else if(currentProtocole == UDP){
            udpClient.sendMessageUDP(message);
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
