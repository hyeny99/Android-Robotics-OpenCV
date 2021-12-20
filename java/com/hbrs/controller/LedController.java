package com.hbrs.controller;

import com.hbrs.MBot;

public class LedController {

    private MBot mbot;
    private int ledId;

    enum Command{
        LEFT,RIGHT,FORWARD,BACKWARD
    }

    private Command prevCommand = null;

    public LedController(MBot mbot){
        this.mbot = mbot;
        this.ledId = 12; // by default
    }

    public void turnLEDon(int colour){
        int r, g, b;
        r = g = b = 0;
        switch (colour){
            // red
            case 0: {
                r = 20;
                break;
            }
            // green
            case 1: {
                g = 20;
                break;
            }
            //blue
            case 2: {
                b = 20;
                break;
            }
        }
        this.mbot.setLight(0, r, g, b);
    }

    public void turnLeftLEDon(){
        if(prevCommand != Command.LEFT) {

            turnLEDoff();
            ledId = 8;
            for (int i = 0; i < 7; i++) {
                ledId = (ledId % 12) + 1;
                this.mbot.setLight(ledId, 0, 0, 20);
            }
            prevCommand = Command.LEFT;

        }
    }

    public void turnRightLEDon(){
        if(prevCommand != Command.RIGHT) {

            turnLEDoff();
            for (ledId = 3; ledId < 10; ledId++) {
                this.mbot.setLight(ledId, 0, 0, 20);
            }
            prevCommand = Command.RIGHT;

        }
    }

    public void forwardLEDon(){
        if(prevCommand != Command.FORWARD) {

            turnLEDoff();
            for (ledId = 1; ledId < 7; ledId++) {
                this.mbot.setLight(ledId, 0, 0, 20);
            }
            prevCommand = Command.FORWARD;

        }
    }

    public void backwardLEDon(){
        if(prevCommand != Command.BACKWARD) {

            turnLEDoff();
            for (ledId = 6; ledId < 13; ledId++) {
                this.mbot.setLight(ledId, 0, 0, 20);
            }
            prevCommand = Command.BACKWARD;

        }
    }

    public void joystickLED(double degrees){
        int index = (int)Math.round(degrees/30.0)+2;
        index = (index%12)+1;
        if(index != ledId) {
            turnLEDoff();
            ledId = index;
           this.mbot.setLight(ledId,0,0,20);
        }
        if(prevCommand != null){
            prevCommand = null;
        }
    }

    public void turnLEDoff(){
        this.mbot.setLight(0, 0, 0, 0);
    }

    public void setLedId(int ledId){
        if(ledId >= 0 && ledId < 13)
            this.ledId = ledId;
        else
            this.ledId = (ledId % 12) + 1;
    }
}
