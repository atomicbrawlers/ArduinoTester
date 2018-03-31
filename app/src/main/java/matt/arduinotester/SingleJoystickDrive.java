package matt.arduinotester;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class SingleJoystickDrive extends AppCompatActivity {

    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    ListView lv;
    BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    OutputStream mOutStream;
    Boolean temp = false;
    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP =
            "00001101-0000-1000-8000-00805F9B34FB";
    private JoystickView myJoystick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_joystick_drive);

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
            } catch (IOException e) {
            }
        }

        myJoystick = findViewById(R.id.joystick);

        myJoystick.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    mOutStream.write((byte)myJoystick.getPercentageY()); //try casting as a byte if negatives don't work
                    //mOutStream.write((byte) (255 * .01 * myJoystick.getPercentageY()));
                } catch (IOException e) {

                }
                return false;
            }
        });
    }
}
