package matt.arduinotester;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class SendNumber extends AppCompatActivity {

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

    private EditText inputBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_number);

        inputBox = findViewById(R.id.input_box);

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
    }

    public void sendData(View view){
        //char neg = inputBox.getText().toString().substring(0,1).toCharArray()[0];
        //char[] input = inputBox.getText().toString().toCharArray();
        System.out.println("Data looks like... " + inputBox.getText().toString());
        //for (char i : input) {
        //    System.out.println(i);
        //}
        try {
            String message = "<";
            int num = Integer.valueOf(inputBox.getText().toString());
            if (num < 0) {
                message += "-";
                num = Math.abs(num);
            }
            message += num + ">";
            System.out.println("Sending... " + message);
            mOutStream.write(message.getBytes());
        } catch (IOException e) {
            System.out.println("ERROR: Value not sent");
        }
    }
}
