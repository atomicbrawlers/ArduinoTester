package matt.arduinotester;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static android.R.color.holo_red_dark;
import static android.R.color.holo_red_light;

public class TankDriveSlider extends AppCompatActivity {

    private View mContentView;
    private Button //mStop,
                   mEmergencyStop;
    private SeekBar mLeftDriveInput, mRightDriveInput;
    private TextView mLeftDrivePercentage, mRightDrivePercentage;

    private boolean emergencyStop;
    private int leftInput, rightInput,
                leftSpeed, rightSpeed;

    private final int RELATIVE_ZERO = 100;
    private final int THRESH = 30;
    private final int FULLSCREEN = View.SYSTEM_UI_FLAG_IMMERSIVE
                                 | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                 | View.SYSTEM_UI_FLAG_FULLSCREEN
                                 | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    //Bluetooth Variables
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    ListView lv;
    BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    OutputStream mOutStream;
    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP =
            "00001101-0000-1000-8000-00805F9B34FB";

    private boolean connectionEstablished = false;

    class BluetoothTransfer implements Runnable {
        private volatile boolean exit = false;

        public void run(){
            while(!exit && connectionEstablished) { //thread runs inside loop
                if (!emergencyStop) {
                    //TODO: Send values through Bluetooth here
                    //Send left value
                    //Send right value

                    sendData();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        //Do nothing
                    }
                }
            } //thread stops outside loop
        }

        public void stop(){
            //TODO: Run values of ZERO here to ensure robot stops
            exit = true;
        }
    }

    private final BluetoothTransfer rDataTransfer= new BluetoothTransfer();

    private final Thread tDataTransfer = new Thread(rDataTransfer);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tank_drive_slider);

        establishBluetoothConnection();

        mContentView = findViewById(R.id.fullscreen_content_tank);
        mContentView.setSystemUiVisibility(FULLSCREEN);

     //   mStop                 = (Button)   findViewById(R.id.stop_tank);
        mEmergencyStop        = (Button)   findViewById(R.id.emergency_stop_tank);
        mLeftDriveInput       = (SeekBar)  findViewById(R.id.left_drive_bar_tank);
        mRightDriveInput      = (SeekBar)  findViewById(R.id.right_drive_bar_tank);
        mLeftDrivePercentage  = (TextView) findViewById(R.id.left_drive_percentage_tank);
        mRightDrivePercentage = (TextView) findViewById(R.id.right_drive_percentage_tank);

        emergencyStop = false;
        leftInput  = RELATIVE_ZERO;
        rightInput = RELATIVE_ZERO;
        leftSpeed  = 0;
        rightSpeed = 0;

        setUpListeners();
        tDataTransfer.start();
    }

    @Override
    protected void onPause(){
        super.onPause();
        rDataTransfer.stop();
    }

    private void setUpListeners(){
        mLeftDriveInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                leftInput = progress;

                //Converts raw SeekBar value to a relative one
                if (Math.abs(leftInput - RELATIVE_ZERO) > THRESH){ //Check left input
                    leftSpeed = leftInput - RELATIVE_ZERO;
                } else {
                    leftSpeed = 0;
                }

                mLeftDrivePercentage.setText(String.valueOf(leftSpeed) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }
        });

        mRightDriveInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rightInput = progress;

                //Converts raw SeekBar value to a relative one
                if (Math.abs(rightInput - RELATIVE_ZERO) > THRESH){ //Check right input
                    rightSpeed = rightInput - RELATIVE_ZERO;
                } else {
                    rightSpeed = 0;
                }

                mRightDrivePercentage.setText(String.valueOf(rightSpeed) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }
        });
    }

    public void emergencyStop(View view){
        emergencyStop = true;
        stop(view);
        rDataTransfer.stop();

        mEmergencyStop.setBackgroundColor(getResources().getColor(holo_red_dark,  this.getTheme()));
        mEmergencyStop.setTextColor      (getResources().getColor(holo_red_light, this.getTheme()));
        mEmergencyStop.setText           ("EMERGENCY STOPPED");
    }

    public void stop(View view){
        leftSpeed  = 0;
        rightSpeed = 0;

        mLeftDrivePercentage .setText(String.valueOf(leftSpeed)  + "%");
        mRightDrivePercentage.setText(String.valueOf(rightSpeed) + "%");

        mLeftDriveInput .setProgress(RELATIVE_ZERO);
        mRightDriveInput.setProgress(RELATIVE_ZERO);
    }

    //Currently has to run first thing, failures causes activity not to launch properly
    private void establishBluetoothConnection(){
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);
        BA = BluetoothAdapter.getDefaultAdapter();
        lv = findViewById(R.id.listView);

        mDevice = null;

        pairedDevices = BA.getBondedDevices();
        for (BluetoothDevice bt : pairedDevices) {
            System.out.println(bt.getName());
            if (bt.getName().equals("HC-05")) { //TODO: CHANGE TO YOUR DEVICE NAME
                System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n");
                mDevice = bt;
            }
        }

        try {
            mSocket = mDevice.createRfcommSocketToServiceRecord(myUUID);
        } catch (IOException e) {
            mSocket = null;
        }

        Boolean tmp = true;

        try {
            mSocket.connect();
        } catch (IOException connectException) {
            try {
                mSocket.close();
                tmp = false;
            } catch (IOException closeException) {
            }
        }

        mOutStream = null;
        if (tmp) {
            try {
                mOutStream = mSocket.getOutputStream();
                connectionEstablished = true;
            } catch (IOException e) {
            }
        }
    }

    public void sendData(){
        System.out.println("Data looks like... " + leftSpeed + "," + rightSpeed);
        try {
            String message = "<";

            int num = leftSpeed;
            if (num < 0) {
                message += "-";
                num = Math.abs(num);
            }
            message += num + ",";

            num = rightSpeed;
            if (num < 0) {
                message += "-";
                num = Math.abs(num);
            }
            message += num + ">";

            System.out.println("Sending... " + message);
            mOutStream.write(message.getBytes());

        } catch (IOException e) {
            System.out.println("ERROR: Message not sent");
            System.out.println("Connection Status?: " + mSocket.isConnected());
        }
    }
}