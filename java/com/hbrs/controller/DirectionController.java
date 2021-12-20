package com.hbrs.controller;

import com.hbrs.MBot;
import com.hbrs.controller.LedController;

public class DirectionController {
    private int power_default;
    private int speed_left;
    private int speed_right;

    private int[] speeds;
    private MBot mbot;
    private LedController ledController;

    public enum Direction {
        Straight, Left, Right, Backwards, Stop
    }

    public Direction dir;

    public DirectionController(MBot mbot){
        this.power_default = 200;
        this.mbot = mbot;
        this.ledController = new LedController(this.mbot);
        this.speeds = new int[2];
        this.speed_left = power_default;
        this.speed_right = power_default;
    }

    public int[] moveForward(){
        if(power_default > 0){
            speed_left = -1 * power_default;
            speed_right = power_default;
            mbot.setDrive(speed_left, speed_right);
            dir = Direction.Straight;
        }
        speeds[0] = Math.abs(speed_left);
        speeds[1] = Math.abs(speed_right);

        return speeds;
    }

    public int[] moveRight(){
        speed_left = power_default;
        speed_right = (int)(0.6 * power_default);
        mbot.setDrive(speed_left, speed_right);
        dir = Direction.Right;

        speeds[0] = Math.abs(speed_left);
        speeds[1] = Math.abs(speed_right);

        return speeds;
    }

    public int[] moveLeft(){
        speed_left = (int)(-0.6 * power_default);
        speed_right = -1 * power_default;
        mbot.setDrive(speed_left, speed_right);
        dir = Direction.Left;

        speeds[0] = Math.abs(speed_left);
        speeds[1] = Math.abs(speed_right);

        return speeds;
    }

    public int[] moveBackwards(){
        if((-1) * power_default < 0){
            speed_left = power_default;
            speed_right = (-1) * power_default;
            mbot.setDrive(speed_left, speed_right);
            dir = Direction.Backwards;
        }
        speeds[0] = (-1) * speed_left;
        speeds[1] = speed_right;

        return speeds;
    }

    public int[] joystick(float x, float y, int w, int h){
        //Keep button boundaries
        if(x > w){
            x = w;
        } else if (x < 0){
            x = 0;
        }
        if(y > h){
            y = h;
        } else if(y < 0){
            y = 0;
        }

        //Determine percentage left / right
        if(x > w/2) { //right
            x = (x-w/2)/(w/2);
        } else {//left
            x = ((x / (w/2)) - 1.0f);
        }

        //Determine percentage forward / backward
        if(y > h/2) {//backward
            y = (y-h/2)/(h/2) * (-1.0f);
        } else { //forward
            y = ( (-1.0f) + (y/(h/2)) ) * (-1.0f);
        }

        //Set motor speeds
        int left  = (int)Math.round( x * (150) + y * (-300) );
        int right = (int)Math.round( x * (150) + y * 300 );

        //convert coordinates into degrees
        double degrees = Math.toDegrees(Math.atan(Math.abs(y)/Math.abs(x)));
        if(x<0.0 && y>0.0) { //upper left
            degrees = 270.0 + degrees;
        }
        if(x>0.0 && y>0.0) {//upper right
            degrees = 90.0 - degrees;
        }
        if(x>0.0 && y<0.0) {//lower right
            degrees = 90.0 + degrees;
        }
        if(x<0.0 && y<0.0) {//lower left
            degrees = 270.0 - degrees;
        }
        System.out.println("Winkel: " + degrees + " X: " + x + " Y: " + y + "\n");

        mbot.setDrive(left, right);
        int[] data = {left,right, (int)degrees};
        return data;
    }


    public void stop(){
        mbot.setDrive(0, 0);
        dir = Direction.Stop;
    }

    public void changeSpeed(int changeSpeed){
        this.power_default += changeSpeed;
    }

    public void setPower_default(int power) {
        this.power_default = power;
    }

    public Direction getDir() {
        return dir;
    }


}
