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

public class SendTwoNumbers extends AppCompatActivity {

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

    private EditText[] inputs = new EditText[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_two_numbers);

        inputs[0] = findViewById(R.id.input_box_1);
        inputs[1] = findViewById(R.id.input_box_2);

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
        System.out.println("Data looks like... " + inputs[0].getText().toString() + "," + inputs[1].getText().toString());
        try {
            int num = 0;
            String message = "<";
            for (EditText inputBox : inputs) {
                num = Integer.valueOf(inputBox.getText().toString());
                if (num < 0) {
                    message += "-";
                    num = Math.abs(num);
                }
                message += num + ",";
            }
            message = message.substring(0, message.length()-1); //remove last comma
            message += ">";
            System.out.println("Sending... " + message);
            mOutStream.write(message.getBytes());
        } catch (IOException e) {
            System.out.println("ERROR: Value not sent");
        }
    }
}
