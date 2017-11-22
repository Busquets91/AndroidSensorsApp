package com.exsitu.inria.jriviere.workshopsensorstoudp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import static com.exsitu.inria.jriviere.workshopsensorstoudp.MainActivity2.StateVideoProfessor.PAUSED;

public class MainActivity2 extends AppCompatActivity implements SensorEventListener {
    SensorManager mSensorManager;

    private static final int OSC = 0;
    private static final int UDP = 1;

    float[] mGravity = new float[11];
    float[] mGeomagnetic = new float[11];

    Sensor gyroscopeDefault;
    Sensor mAccelerometer;
    Sensor orientation;
    Sensor proximite;
    UDPClient udpClient;
    TextView infoTextView;
    int packetNumber = 0;
    int revertState = 0;

    //UI COMPONENT
    RadioButton radioBtnOSC;
    RadioButton radioBtnUDP;
    Button buttonRecordStart1;
    Button buttonRecordStop1;
    Button buttonRecordStart2;
    Button buttonRecordStop2;
    Button buttonProfRestart;
    Button buttonProfPlayPause;
    Button buttonFlipProfesseur;

    Button buttonSonificationOff;
    Button buttonSonificationOn;
    TextView textViewPort;
    TextView textViewIP;
    Switch switchEnabled;

    Button buttonPlay1;
    Button buttonStop1;


    Button buttonSonificationRecordedOn;
    Button buttonSonificationRecordedOff;
    Button buttonSonVideo;

    TextView textViewSonificationRecorded;

    public enum StateRecord {
        STARTED,STOPED,INIT
    }
    enum StateSoundRecorded {
        ON,OFF
    }
    enum StateSonification {
        ON,OFF
    }
    enum StateVideoProfessor {
        PAUSED,PLAYED,INIT
    }
    enum StateSonVideoRecorded {
        ON,OFF
    }

    StateSonification stateSonification = StateSonification.ON;
    StateSoundRecorded stateSoundRecorded = StateSoundRecorded.ON;
    StateRecord stateRecord = StateRecord.INIT;
    StateVideoProfessor stateVideoProfessor = StateVideoProfessor.INIT;
    StateSonVideoRecorded stateSonVideoRecorded = StateSonVideoRecorded.OFF;

    private int currentProtocole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        System.out.println("Start the service....");
        //ServiceTest serviceTest = new ServiceTest(this);
        Intent intent = new Intent(this, ServiceTest.class);
        startService(intent);

        initComponentUI();
        initNetwork();
        initComponentSensors();

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


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        // Pour trouver un capteur spécifique&#160;:
        gyroscopeDefault = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mAccelerometer =    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //orientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        //orientation = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //proximite = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
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
        buttonRecordStart1 = (Button) findViewById(R.id.buttonRecordStart1);
        buttonRecordStop1 = (Button) findViewById(R.id.buttonRecordStop1);
        buttonProfPlayPause = (Button) findViewById(R.id.buttonProfPlayPause);
        buttonProfRestart = (Button) findViewById(R.id.buttonProfRestart);
        buttonFlipProfesseur = (Button)findViewById(R.id.buttonFlipProfesseur);
      //  buttonProfStart = (Button) findViewById(R.id.buttonProfStart);
        buttonPlay1 = (Button) findViewById(R.id.buttonPlay1);
        buttonStop1= (Button) findViewById(R.id.buttonStop1);

        buttonSonificationOn = (Button) findViewById(R.id.buttonSonificationOn);
        buttonSonificationRecordedOn = (Button) findViewById(R.id.buttonSonificationRecordedOn);
        buttonSonificationRecordedOff = (Button) findViewById(R.id.buttonSonificationRecordedOff);
        textViewSonificationRecorded = (TextView) findViewById(R.id.textViewSonificationRecorded);
        buttonSonVideo = (Button) findViewById(R.id.buttonSonVideo);


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

        initComponentUIRecord();
        initComponentUIProfessor();
        initComponentUISonification();
    }




    private void initComponentUIRecord() {
        buttonRecordStart1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (stateRecord){
                    case STOPED :

                        buttonPlay1.setEnabled(false);
                        buttonStop1.setEnabled(false);
                        buttonSonificationRecordedOn.setEnabled(false);
                        buttonSonVideo.setEnabled(false);
                        buttonSonificationRecordedOn.setTextColor(Color.GRAY);
                        buttonSonVideo.setTextColor(Color.GRAY);


                        buttonRecordStart1.setText("■");
                        buttonRecordStart1.setTextColor(Color.GREEN);

                        // buttonRecordStart1.setBackground(new ColorDrawable(1));
                        sendMessage("videoRec 1 on");
                        sendMessage("professor pause");
                        sendMessage("videoPlay 1 reset");
                        stateRecord = (stateRecord.STARTED);

                        buttonProfPlayPause.setEnabled(false);
                        buttonProfRestart.setEnabled(false);
                        resetProfessorCommand();

                        break;
                    case INIT:
                        //  buttonRecordStop1.setEnabled(true);
                        //  buttonRecordStart1.setEnabled(false);
                        buttonPlay1.setEnabled(false);
                        buttonStop1.setEnabled(false);
                        buttonSonificationRecordedOn.setEnabled(false);
                        buttonSonVideo.setEnabled(false);
                        buttonSonificationRecordedOn.setTextColor(Color.GRAY);
                        buttonSonVideo.setTextColor(Color.GRAY);

                        // buttonRecordStart1.setBackground(new ColorDrawable(1));
                        stateRecord = stateRecord.STARTED;
                        buttonRecordStart1.setText("■");
                        buttonRecordStart1.setTextColor(Color.GREEN);

                        sendMessage("videoRec 1 on");
                        sendMessage("professor pause");
                        sendMessage("videoPlay 1 reset");
                        buttonProfPlayPause.setEnabled(false);
                        buttonProfRestart.setEnabled(false);
                        resetProfessorCommand();

                        break;
                    case STARTED :
                        //  buttonRecordStop1.setEnabled(true);
                        //  buttonRecordStart1.setEnabled(false);

                        // buttonRecordStart1.setBackground(new ColorDrawable(1));

                        stateRecord = stateRecord.STOPED;
                        buttonRecordStart1.setText("•");
                        sendMessage("videoRec 1 off");
                        buttonRecordStart1.setTextColor(Color.RED);


                        buttonProfPlayPause.setEnabled(true);
                        buttonProfRestart.setEnabled(true);
                        updateRecordUI();

                    break;
                }


            }

            private void updateRecordUI() {
                buttonPlay1.setEnabled(false);
                buttonStop1.setEnabled(false);
                buttonPlay1.setVisibility(View.VISIBLE);
                buttonStop1.setVisibility(View.VISIBLE);
                buttonSonVideo.setVisibility(View.VISIBLE);
                buttonSonificationRecordedOn.setVisibility(View.VISIBLE);
                textViewSonificationRecorded.setVisibility(View.VISIBLE);

                buttonSonificationRecordedOn.setEnabled(true);
                buttonPlay1.setEnabled(false);
                buttonStop1.setEnabled(false);

                buttonPlay1.setEnabled(true);
                buttonStop1.setEnabled(true);

                buttonSonificationRecordedOn.setEnabled(true);
                buttonSonVideo.setEnabled(true);
                buttonSonificationRecordedOn.setTextColor(Color.RED);
                buttonSonVideo.setTextColor(Color.RED);
            }
        });

        buttonSonificationRecordedOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(stateSoundRecorded) {
                    case OFF:
                        //  buttonSonificationRecordedOff.setEnabled(true);
                        //  buttonSonificationOn.setEnabled(true);
                        // buttonSonificationOff.setEnabled(false);
                        sendMessage("sonificationRecorded off");
                        buttonSonificationRecordedOn.setText("OFF");
                        buttonSonificationRecordedOn.setTextColor(Color.RED);

                        stateSoundRecorded = StateSoundRecorded.ON;
                        break;

                    case ON:
                        //  buttonSonificationRecordedOff.setEnabled(true);
                        //  buttonSonificationOn.setEnabled(true);
                        // buttonSonificationOff.setEnabled(false);
                        sendMessage("sonificationRecorded on");
                        buttonSonificationRecordedOn.setText("ON");
                        stateSoundRecorded = StateSoundRecorded.OFF;
                        buttonSonificationRecordedOn.setTextColor(Color.GREEN);
                        sonificationSwitchOff();
                        break;
                }
            }
        });



        buttonSonVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("cliked");

                switch(stateSonVideoRecorded){
                    case OFF:
                        enableButtonSonificationRecordedOn(false);
                        enableButtonSonificationOn(false);

                        sendMessage("videoPlay 1 start");
                        sendMessage("sonificationRecorded on" );
                        resetProfessorCommand();

                        buttonSonVideo.setTextColor(Color.GREEN);
                        stateSonVideoRecorded = StateSonVideoRecorded.ON;
                        buttonPlay1.setEnabled(false);
                        buttonStop1.setEnabled(false);
                        buttonRecordStart1.setEnabled(false);
                        buttonRecordStart1.setTextColor(Color.GRAY);


                        break;
                    case ON:
                        enableButtonSonificationRecordedOn(true);
                        enableButtonSonificationOn(true);
                        sendMessage("videoPlay 1 reset");
                        sendMessage("sonificationRecorded off" );
                        sendMessage("professor pause");

                        buttonSonVideo.setTextColor(Color.RED);
                        stateSonVideoRecorded = StateSonVideoRecorded.OFF;
                        buttonPlay1.setEnabled(true);
                        buttonStop1.setEnabled(true);
                        buttonRecordStart1.setEnabled(true);
                        buttonRecordStart1.setTextColor(Color.RED);

                        break;

                }
                System.out.println(" " + stateSonVideoRecorded);



            }
        });


        buttonPlay1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonPlay1.setEnabled(true);
                buttonStop1.setEnabled(true);
                sendMessage("videoPlay 1 play");
                sendMessage("professor pause");

                resetProfessorCommand();

            }
        });
        buttonStop1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonPlay1.setEnabled(true);
                buttonStop1.setEnabled(true);
                sendMessage("professor pause");
                sendMessage("videoPlay 1 stop");

            }
        });
    }

    private void resetProfessorCommand() {

        buttonProfPlayPause.setTextColor(Color.RED);
        stateVideoProfessor = PAUSED;
    }

    private void enableButtonSonificationOn(boolean b) {
        buttonSonificationOn.setEnabled(b);
        if(b){
            buttonSonificationOn.setTextColor(Color.RED);
        } else {
            buttonSonificationOn.setTextColor(Color.GRAY);
            sendMessage("sonification off");
        }
        stateSonification = StateSonification.ON;
        buttonSonificationOn.setText("OFF");
    }

    private void enableButtonSonificationRecordedOn(boolean b) {
        buttonSonificationRecordedOn.setEnabled(b);
        if(b){
            buttonSonificationRecordedOn.setTextColor(Color.RED);
        } else {
            buttonSonificationRecordedOn.setTextColor(Color.GRAY);
            sendMessage("sonificationRecorded off");
        }
        stateSoundRecorded = StateSoundRecorded.ON;
        buttonSonificationRecordedOn.setText("OFF");

    }

    public void initComponentUIProfessor() {


        buttonProfPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    switch(stateVideoProfessor){
                        case INIT :

                            sendMessage("professor play");
                            sendMessage("videoPlay 1 reset");
                            buttonProfPlayPause.setTextColor(Color.GREEN);
                            stateVideoProfessor = StateVideoProfessor.PLAYED ;
                            break;
                        case PAUSED:

                            sendMessage("professor resume");
                            sendMessage("videoPlay 1 reset");
                            buttonProfPlayPause.setTextColor(Color.GREEN);

                            stateVideoProfessor = StateVideoProfessor.PLAYED;
                            break;
                        case PLAYED:



                            buttonProfPlayPause.setTextColor(Color.RED);
                            sendMessage("professor pause");
                            stateVideoProfessor = PAUSED;
                            break;
                    }

            }
        });
        buttonProfRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage("professor play");
                sendMessage("videoPlay 1 reset");


                buttonProfPlayPause.setTextColor(Color.GREEN);

                stateVideoProfessor = StateVideoProfessor.PLAYED;
            }
        });
        buttonFlipProfesseur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(revertState == 0) {
                    buttonFlipProfesseur.setTextColor(Color.GREEN);
                } else {
                    buttonFlipProfesseur.setTextColor(Color.RED);
                }
                revertState++;
                revertState = revertState%2;
                System.out.println(revertState);
                sendMessage("professor revert " + revertState);

            }
        });
    }


    private void initComponentUISonification() {



        buttonSonificationOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (stateSonification) {
                    case ON:
                        buttonSonificationOn.setEnabled(true);
                        // buttonSonificationOff.setEnabled(true);
                        //   buttonSonificationRecordedOff.setEnabled(false);
                        sendMessage("sonification on");
                        stateSonification = StateSonification.OFF;
                        buttonSonificationOn.setText("ON");
                        buttonSonificationOn.setTextColor(Color.GREEN);
                        sonificationRecordedSwitchOff();

                        break;

                    case OFF:
                        buttonSonificationOn.setEnabled(true);
                        // buttonSonificationOff.setEnabled(true);
                        //   buttonSonificationRecordedOff.setEnabled(false);
                        sendMessage("sonification off");
                        stateSonification = StateSonification.ON;
                        buttonSonificationOn.setText("OFF");
                        buttonSonificationOn.setTextColor(Color.RED);

                        sonificationRecordedSwitchOff();
                        break;
                }
            }
        });
    }

    //La sonification en temps reel et la sonification recorded ne doivent pas etre joué en meme temps
    private void sonificationRecordedSwitchOff() {
        sendMessage("sonificationRecorded off");
        buttonSonificationRecordedOn.setText("OFF");
        buttonSonificationRecordedOn.setTextColor(Color.RED);


        stateSoundRecorded = StateSoundRecorded.ON;
    }

    private void sonificationSwitchOff() {
        sendMessage("sonification off");
        stateSonification = StateSonification.ON;
        buttonSonificationOn.setText("OFF");
        buttonSonificationOn.setTextColor(Color.RED);
    }


    private void refreshInfoTextView() {
        infoTextView.setText("sending data... id : " + packetNumber + "  to port : " + udpClient.getPort()+ "\nprotocole : " + currentProtocoleToString()
                + "\n IP : " + udpClient.getIP() );
    }


    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, gyroscopeDefault, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,orientation,SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,proximite,SensorManager.SENSOR_DELAY_GAME);
    }



    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }





    @Override
    public void onSensorChanged(SensorEvent event) {
        packetNumber ++;
        String message = "";
        String messageCompas = "";
        if ( event.sensor.getType() == Sensor.TYPE_GYROSCOPE ){
                message = "gyro " + event.values[0] + " " + event.values[1] + " " + event.values[2] ;
        } else if( event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            message = "accelero " + event.values[0] + " " + event.values[1] + " " + event.values[2] ;
            mGravity = event.values;
        }  else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            float degree = Math.round(event.values[0]);
            mGeomagnetic = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY){
            message = "proximite "+ event.values[0]  ;
        }
        //traitement special sur le magnetometre, selon les recommandation google
        if (mAccelerometer != null && gyroscopeDefault != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                messageCompas = "degree "+(orientation[0])+" "+(orientation[1])+" "+(orientation[2]) ;

            }
        }
        refreshInfoTextView();
        sendMessage(message);
        //sendMessage(messageCompas);
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
