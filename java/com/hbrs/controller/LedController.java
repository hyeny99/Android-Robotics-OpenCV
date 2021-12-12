package com.hbrs.controller;

import com.hbrs.MBot;

public class LedController {

    private MBot mbot;
    private int ledId;

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
        turnLEDoff();
        //3,2,1,12,11,10,9
        // 8 -> 9
        // 9 -> 10
        // 10 -> 11
        // 11 -> 12
        // 12-> 1
        // 1 -> 2
        // 2 -> 3
        ledId = 8;
        for(int i = 0; i < 7; i++){
            ledId = (ledId % 12) + 1;
            this.mbot.setLight(ledId, 0, 0, 20);
        }
    }

    public void turnRightLEDon(){
        //3, 4, 5, 6, 7, 8, 9
        turnLEDoff();
        for(ledId = 3; ledId < 10; ledId++){
            this.mbot.setLight(ledId,0, 0, 20);
        }
    }

    public void forwardLEDon(){
        turnLEDoff();
        for(ledId = 1; ledId < 7; ledId++)
            this.mbot.setLight(ledId, 0, 0, 20);
    }

    public void backwardLEDon(){
        turnLEDoff();
        for(ledId = 6; ledId < 13; ledId++)
            this.mbot.setLight(ledId, 0, 0, 20);

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
