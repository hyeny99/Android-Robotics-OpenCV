package com.hbrs.controller;

import com.hbrs.MBot;

public class DirectionController {
    private int power_default;
    private int speed_left;
    private int speed_right;

    private int[] speeds;
    private MBot mbot;

    public enum Direction {
        Straight, Left, Right, Backwards, Stop
    }

    public Direction dir;

    public DirectionController(MBot mbot){
        this.power_default = 200;
        this.mbot = mbot;
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
        speed_left = (int)(0.6 * power_default);
        speed_right = -1 * power_default;

        mbot.setDrive(speed_left, speed_right);
        dir = Direction.Left;

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

    public void moveWhereEver(int speedL, int speedR){
        mbot.setDrive(speedL, speedR);
    }


    public void stop(){
        mbot.setDrive(0, 0);
        power_default = 200;
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
