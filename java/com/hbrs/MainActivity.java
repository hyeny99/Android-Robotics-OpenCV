//*******************************************************************
/*!
\file   MainActivity.java
\author Thomas Breuer
\date   07.09.2021
\brief
*/

//*******************************************************************
package com.hbrs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hbrs.controller.DirectionController;
import com.hbrs.controller.LedController;

import androidx.appcompat.app.AppCompatActivity;

//*************************************************************************************************
public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MBot";

    private MBot mbot;

    private final static int REQUEST_BLUETOOTH_ENABLE = 1;
    private final static int REQUEST_BLUETOOTH_GET_ADDR = 2;

    /**
     * Der Folgende Abschnitt ist für die Buttonverfolgung
     *
     *
     */

    DirectionController directionController;
    LedController ledController;

    String address;

    Button joyStick_btn;
    TextView leftSpd_textView;
    TextView rightSpd_textView;

    //-----------------------------------------------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);

        address = BT_DeviceListActivity.getAddr( data );
        if( resultCode == Activity.RESULT_OK){
        // Retrieve intent
            Bundle bundle = data.getExtras();
        }
        switch(requestCode)
        {
            case REQUEST_BLUETOOTH_ENABLE:
                break;
            case REQUEST_BLUETOOTH_GET_ADDR:
                if (resultCode == Activity.RESULT_OK)
                {
                    mbot.connect( this, address );
                }
                break;
        }

    }

    //-----------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        leftSpd_textView = findViewById(R.id.leftSpeed);
        rightSpd_textView = findViewById(R.id.rightSpeed);

        joyStick_btn = findViewById(R.id.button_joystick);
        joyStick_btn.setOnTouchListener(new myOnTouchListener());

        mbot = new MBot();
        directionController = new DirectionController(mbot);
        ledController = new LedController(mbot);
    }

    //-----------------------------------------------------------------
    class myOnTouchListener implements View.OnTouchListener
    {

        // separate from the MainActivity
        // make a separate function(joy stick method)
        @Override
        public boolean onTouch(View v, MotionEvent e) {
            float x = e.getX() / v.getMeasuredWidth();
            float y = e.getY() / v.getMeasuredHeight();
            y = (y - (float)0.5);
            x = (x - (float)0.5);

            float left = y * 300;
            float right = y * 300;
            left = left + x * (-100);
            right = right + x * (100);
            right = right * (-1);

            switch(e.getAction())
            {
                case MotionEvent.ACTION_DOWN:

                    break;
                case MotionEvent.ACTION_MOVE:

                    directionController.moveWhereEver((int)left,(int)right);
                    //mbot.setDrive((int)left,(int)right);
                    break;
                case MotionEvent.ACTION_UP:

                    directionController.stop();
                    break;
            }

            return false;
        }
    }


    //-----------------------------------------------------------------
    public void onBtnConnect(View view)
    {
        Log.i(TAG, "Connect...");

        BT_DeviceListActivity.connect( this, REQUEST_BLUETOOTH_ENABLE, REQUEST_BLUETOOTH_GET_ADDR );
    }

    //-----------------------------------------------------------------

    // the given led id by default
    int ledId = 12;

    //-----------------------------------------------------------------

    /**
     *  this method makes LED colour buttons visible.
     *  user gets to choose the colour and corresponding led lamp lights up.
     *  a little bit redundant? How to improve?
     */
    public void onBtnLED(View view)
    {
        Button LED_red = findViewById(R.id.ledBtn_red);
        Button LED_blue = findViewById(R.id.ledBtn_blue);
        Button LED_green = findViewById(R.id.ledBtn_green);

        if(LED_red.getVisibility() == View.VISIBLE){
            LED_red.setVisibility(LED_red.INVISIBLE);
            LED_blue.setVisibility(LED_blue.INVISIBLE);
            LED_green.setVisibility(LED_green.INVISIBLE);
        }
        else{
            LED_red.setVisibility(LED_red.VISIBLE);
            LED_blue.setVisibility(LED_blue.VISIBLE);
            LED_green.setVisibility(LED_green.VISIBLE);
        }
    }


    public void onBtnLed_red(View view)
    {
        ledController.turnLEDon(0);
    }
    public void onBtnLed_green(View view)
    {
        ledController.turnLEDon(1);
    }
    public void onBtnLed_blue(View view)
    {
        ledController.turnLEDon(2);
    }


    //-----------------------------------------------------------------

    /**
     * controls the direction of the robot
     * controls the motors and speed
     */

    public void onBtnForward(View view) {

        ledController.forwardLEDon();
        int[] speeds = directionController.moveForward();
        changeText(speeds[0], speeds[1]);
    }

    public void onBtnRight(View view)
    {
        ledController.turnRightLEDon();
        int[] speeds = directionController.moveRight();
        changeText(speeds[0], speeds[1]);
    }

    public void onBtnLeft(View view)
    {
        int[] speeds = directionController.moveLeft();
        changeText(speeds[0], speeds[1]);
        ledController.turnLeftLEDon();
    }

    public void onBtnBackwards(View view)
    {
        ledController.backwardLEDon();
        int[] speeds = directionController.moveBackwards();
        changeText(speeds[0], speeds[1]);
    }

    public void onBtnStop(View view)
    {
        ledController.turnLEDoff();
        directionController.stop();
        changeText(0, 0);
    }

    public void changeText(int leftSpeed, int rightSpeed){
        String leftSpeedString = "LEFT WHEEL SPEED: " + leftSpeed;
        String rightSpeedString = "RIGHT WHEEL SPEED: " + rightSpeed;

        leftSpd_textView.setText(leftSpeedString);
        rightSpd_textView.setText(rightSpeedString);
    }

    //-----------------------------------------------------------------

    /**
     * change the speed by clicking the volume keys
     * volume key up => speed increases by 50
     * volume key down => speed decreases by 50
     */
    public boolean dispatchKeyEvent(KeyEvent event){
        int action, keycode;
        action = event.getAction();
        keycode = event.getKeyCode();

        switch(keycode){
            case KeyEvent.KEYCODE_VOLUME_UP:
            {
                if(KeyEvent.ACTION_UP==action){
                    directionController.changeSpeed(50);
                }
            }
            break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            {
                if(KeyEvent.ACTION_DOWN==action){
                    directionController.changeSpeed(-50);
                }
            }

        }
        DirectionController.Direction dir = directionController.getDir();
        if(dir.equals(DirectionController.Direction.Straight))
            onBtnForward(null);
        if(dir.equals(DirectionController.Direction.Left))
            onBtnLeft(null);
        if(dir.equals(DirectionController.Direction.Right))
            onBtnRight(null);
        if(dir.equals(DirectionController.Direction.Backwards))
            onBtnBackwards(null);

        return super.dispatchKeyEvent(event);
    }
    //-----------------------------------------------------------------


    //-----------------------------------------------------------------
    /**
     * connects MainActivity with OpenCVActivity
     * Once the button is clicked, it switches to the OpenCVActivity interface.
     */
    public void onImgViewCamera(View v)
    {
        // Create and fill intent
        Intent intent = new Intent(this, OpenCVActivity.class);

        intent.putExtra("passed data", address);

        startActivityForResult(intent, 123);
    }


}
