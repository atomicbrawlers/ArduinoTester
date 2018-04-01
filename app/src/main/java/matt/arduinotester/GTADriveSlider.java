package matt.arduinotester;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import static android.R.color.holo_red_dark;
import static android.R.color.holo_red_light;

public class GTADriveSlider extends AppCompatActivity {

    private View mContentView;
    private Button //mStop,
                   mEmergencyStop;
    private SeekBar mSlewInput, mThrottleInput;
    private TextView mSlewPercentage, mThrottlePercentage;

    private boolean emergencyStop;
    private int leftSpeed, rightSpeed,
                slewInput, throttleInput,
                slew, throttle;

    private final int RELATIVE_ZERO = 100;
    private final int THRESH = 30;
    private final int FULLSCREEN = View.SYSTEM_UI_FLAG_IMMERSIVE
                                 | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                 | View.SYSTEM_UI_FLAG_FULLSCREEN
                                 | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    class BluetoothTransfer implements Runnable {
        private volatile boolean exit = false;

        public void run(){
            while(!exit) { //thread runs inside loop
                //TODO: Send values through Bluetooth here
                //Send left speed
                //Send right speed
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
        setContentView(R.layout.activity_gta_drive_slider);

        mContentView = findViewById(R.id.fullscreen_content_gta);
        mContentView.setSystemUiVisibility(FULLSCREEN);

     //   mStop                 = (Button)   findViewById(R.id.stop_gta);
        mEmergencyStop        = (Button)   findViewById(R.id.emergency_stop_gta);
        mSlewInput            = (SeekBar)  findViewById(R.id.slew_gta);
        mThrottleInput        = (SeekBar)  findViewById(R.id.throttle_gta);
        mSlewPercentage       = (TextView) findViewById(R.id.slew_percentage_gta);
        mThrottlePercentage   = (TextView) findViewById(R.id.throttle_percentage_gta);

        emergencyStop = false;
        leftSpeed  = 0;
        rightSpeed = 0;
        slewInput     = 0;
        throttleInput = 0;
        slew     = 0;
        throttle = 0;

        setUpListeners();
        tDataTransfer.start();
    }

    @Override
    protected void onPause(){
        super.onPause();
        rDataTransfer.stop();
    }

    private void setUpListeners(){
        mSlewInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                slewInput = progress;

                //Converts raw SeekBar value to a relative one
                if (Math.abs(slewInput - RELATIVE_ZERO) > THRESH){ //Check slew input
                    slew = slewInput - RELATIVE_ZERO;
                } else {
                    slew = 0;
                }

                mSlewPercentage.setText(String.valueOf(slew + "%"));

                calculateLogic();
            }

            @Override
            public void onStartTrackingTouch (SeekBar seekBar){
                //Do nothing
            }

            @Override
            public void onStopTrackingTouch (SeekBar seekBar){
                //Do nothing
            }
        });

        mThrottleInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                throttleInput = progress;

                //Converts raw SeekBar value to a relative one
                if (Math.abs(throttleInput - RELATIVE_ZERO) > THRESH){ //Check throttle input
                    throttle = throttleInput - RELATIVE_ZERO;
                } else {
                    throttle = 0;
                }

                mThrottlePercentage.setText(String.valueOf(throttle + "%"));

                calculateLogic();
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

    private void calculateLogic(){
        if (!emergencyStop) {
            if (slew > 0) { //Slew is positive; turning right
                leftSpeed = throttle;
                rightSpeed = throttle * (1 - slew); //Send right partial power; based on slew amount
            } else if (slew < 0) { //Slew is negative; turning left
                leftSpeed = throttle * (1 + slew); //Send left partial power; based on slew amount
                rightSpeed = throttle;
            } else { //No slew; go straight
                leftSpeed = throttle;
                rightSpeed = throttle;
            }
        }
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
        throttle = 0;

        mThrottlePercentage.setText(String.valueOf(throttle) + "%");

        mThrottleInput.setProgress(RELATIVE_ZERO);
    }
}