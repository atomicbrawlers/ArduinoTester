package matt.arduinotester;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Blink extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blink);
    }

    public void button1Pressed(View view){
        System.out.println("Button 1 Pressed");
    }

    public void button2Pressed(View view){
        System.out.println("Button 2 Pressed");
    }

}
