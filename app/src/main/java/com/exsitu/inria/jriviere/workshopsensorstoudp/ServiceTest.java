package com.exsitu.inria.jriviere.workshopsensorstoudp;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by jriviere on 18/05/17.
 */

public class ServiceTest extends Service implements SensorEventListener {



        private static final String TAG = "HelloService";
     final int OSC = 0;
        final int UDP = 1;
         UDPClient udpClient;
        SensorManager mSensorManager;
    Sensor gyroscopeDefault;
    Sensor mAccelerometer;
    Sensor orientation;
    Sensor proximite;
    int packetNumber = 0;



    private int currentProtocole;

    public Activity activity;
    Context context;

    LocalBroadcastManager broadcaster;


        @Override
        public void onCreate() {
            Log.i(TAG, "Service onCreate");
            currentProtocole = OSC;

        }



        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.i(TAG, "Service onStartCommand");
            initNetwork();
            initComponentSensors();
            return Service.START_STICKY;
        }


    private void initComponentSensors() {

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // Pour trouver un capteur spécifique
        gyroscopeDefault = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mAccelerometer =    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        orientation = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        proximite = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);


//        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
     //   mSensorManager.registerListener(this, gyroscopeDefault, SensorManager.SENSOR_DELAY_NORMAL);
      //  mSensorManager.registerListener(this,orientation,SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this,proximite,SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void initNetwork() {
        udpClient = UDPClient.getInstance();
        udpClient.run();
    }

    private void sendMessage(String message) {
        if(currentProtocole == OSC){
            udpClient.sendMessageOSC(message);
        } else if(currentProtocole == UDP){
            udpClient.sendMessageUDP(message);
        }
    }

        @Override
        public IBinder onBind(Intent arg0) {
            Log.i(TAG, "Service onBind");
            return null;
        }

        @Override
        public void onDestroy() {
            Log.i(TAG, "Service onDestroy");
        }


    @Override
    public void onSensorChanged(SensorEvent event) {

        packetNumber ++;
        String message = "";
        if ( event.sensor.getType() == Sensor.TYPE_GYROSCOPE ){
            message = "gyro|||" + event.values[0] + "|||" + event.values[1] + "|||" + event.values[2] ;
            //  System.out.println("gyro" + " " + event.values[0] + " " + event.values[1] + " " + event.values[2] + ";");
        } else if( event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            message = "accelero|||" + event.values[0] + "|||" + event.values[1] + "|||" + event.values[2] ;
            //   System.out.println("accelero" + " " + event.values[0] + " " + event.values[1] + " " + event.values[2] + ";");
        }  else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION){
            float degree = Math.round(event.values[0]);
            // System.out.println( "degree : " +  degree + " "      );
            message = "degree|||"+Math.round(event.values[0])+"|||"+Math.round(event.values[1])+"|||"+Math.round(event.values[2]) ;
        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY){
            message = "proximite|||"+ event.values[0]  ;
            //System.out.println(message);
        }
        sendMessage(message);
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }



}

