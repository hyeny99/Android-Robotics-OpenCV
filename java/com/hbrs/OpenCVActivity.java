//*******************************************************************
/*!
\file   MainActivity.java
\author Thomas Breuer
\date   07.09.2021
\brief
*/

//*******************************************************************
package com.hbrs;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.hbrs.controller.DirectionController;
import com.hbrs.controller.LedController;

import androidx.appcompat.app.AppCompatActivity;
import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_ANY;
import static org.opencv.imgproc.Imgproc.HOUGH_GRADIENT;

//*************************************************************************************************
public class OpenCVActivity extends AppCompatActivity
                            implements CameraBridgeViewBase.CvCameraViewListener2
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

    JavaCameraView mCamView;
    Mat mat1, mat2;

    double distance = 0.0;
    double minArea = 500;
    double maxArea = 80000;

    double sumRadius = 0.0;

    String address;

    DirectionController directionController;
    LedController ledController;



    //-----------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        OpenCVLoader.initDebug();

        // Initialize OpenCV
        mCamView = (JavaCameraView)findViewById( R.id.camera_view);
        mCamView.setCameraIndex(CAMERA_ID_ANY);
        mCamView.setCvCameraViewListener(this);
        mCamView.enableView();


        //onBtnConnect(null);
        OpenCVLoader.initDebug();

        Intent intent = getIntent();
        address = intent.getStringExtra("passed data");

        mbot = new MBot();
        if (address != null)
        {
            mbot.connect( this, address );
        }
        directionController = new DirectionController(mbot);
        ledController = new LedController(mbot);

    }


    //-----------------------------------------------------------------
    public void onBtnConnect(View view)
    {
        Log.i(TAG, "Connect...");

        BT_DeviceListActivity.connect( this, REQUEST_BLUETOOTH_ENABLE, REQUEST_BLUETOOTH_GET_ADDR );
    }

    //*********************************************************************************************

    /**
     * connects MainActivity with OpenCVActivity
     * Once the button is clicked, it sends back to the MainActivity interface.
     */
    public void onImgViewPrevious(View view){
        directionController.stop();
        Intent intent = new Intent(this, com.hbrs.MainActivity.class);
        startActivityForResult(intent, 123);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    protected void onPause(){
        super.onPause();
        mCamView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamView.enableView();
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    protected void onDestroy() {
        mCamView.disableView();
        super.onDestroy();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {

        mat1 = new Mat();
        mat2 = new Mat();

        // Get frame in a rgb color system
        Mat mImg = inputFrame.rgba();
        //Information about the detected circles in will be saved in circles
        Mat circles = new Mat();

        // mat1(frame) : convert the frame to HSV from RGB
        Imgproc.cvtColor(mImg, mat1, Imgproc.COLOR_BGR2HSV);


        // set a range of colour
        Scalar scalarLow = new Scalar(45, 70, 50);
        Scalar scalarHigh = new Scalar(80, 255, 255);

        Core.inRange(mat1, scalarLow, scalarHigh, mat2);
        Imgproc.GaussianBlur(mat2, mat2, new Size(9, 9), 2, 2);

        //Find all circles in the image and save it to the matrix 'circles'

        //Fine-tuned HoughCircles --> more stable, less sensitive (fewer false detections)!
        Imgproc.HoughCircles(mat2, circles, HOUGH_GRADIENT, 2, 100, 200, 100, 1, 300);

        /**
         * Here the information will be processed.
         * 1. Save the information, so it can be used in a different function to controll the robot
         * 2. Use the information in this function to draw rectangles on the original frame, in order
         *    for the user of the app to see on the camera preview which circle the system detected in the frame
         */

        Point focusedCenter = new Point(0,0);

        //Info about the detected circles will show up on the console
        int[] focusedObject = new int[3];               // Information about the object. Format: X,Y,R
        for(int i=0; i < circles.cols(); i++){          // Iterate through all detected circles

            // [0] = x
            // [1] = y
            Point center = new Point( circles.get(0,i)[0], circles.get(0,i)[1] );

            //Check if the new Circle is bigger than the previous
            if(focusedObject[2] < (int)circles.get(0,i)[2]) {
                focusedObject[0] = (int) circles.get(0, i)[0];
                focusedObject[1] = (int) circles.get(0, i)[1];
                focusedObject[2] = (int) circles.get(0, i)[2];
                focusedCenter = center;
            }

            //Output to verify data
            System.out.println( "XY: " + center.toString() +
                    " | Radius: " + (int)circles.get(0,i)[2] +
                    " | " + (i+1) + "/" + circles.cols());
        }

        Imgproc.circle(mImg, focusedCenter, 1, new Scalar(0,100,100), 3, 8, 0);
        Imgproc.circle(mImg, focusedCenter, focusedObject[2], new Scalar(0, 100, 100), 5);
        // focusedObject[2] = radius

        //At this point the function to evaluate the coordinates can be called
        //EVALUATION-function: Parameter -> focusedObject -> int[3]
        double k = 0.9;
        sumRadius = k * sumRadius + (1-k) * focusedObject[2];
        double objArea = getArea(sumRadius);

        double objDis = getDistance(objArea);
        // return image to CameraView

        track(objArea, focusedCenter);

        Imgproc.putText (
                mImg,                           // Matrix obj of the image
                objDis + "cm",             // Text to be displayed
                new Point(170, 280),      // display location of the text box
                Core.FONT_HERSHEY_SIMPLEX ,     // font face
                1,                      // font scale
                new Scalar(0, 255, 0),           // Scalar object for color
                4                       // Thickness
        );
        return  mImg;

    }


    public double getDistance(double area){
        if(area <= 0)
            distance = 0.0;
        else{
            distance = 3739.2 * Math.pow(area, -0.532);
            distance = (double) Math.round(distance * 10000) / 10000;
        }

        //y = 8E-09x2 - 0.0009x + 35.626
        //more fitting function: y = 3739.2x^(-0.532)
        return distance;
    }

    public double getArea(double radius){
        double area = Math.PI * radius * radius;
        area = (double) Math.round(area * 10000) / 10000;
        return area;
    }

    public void track(double objArea, Point focusedCenter){
        double[] objLocation = null;
        double imgWidth = mat1.size().width;
        double centerImgX = imgWidth / 2;

        if (objArea > 0){
            objLocation = new double[3];
            objLocation[0] = objArea;
            objLocation[1] = focusedCenter.x;
            objLocation[2] = focusedCenter.y;
        }

        if (objLocation != null){
            if (objLocation[0] > minArea && objLocation[0] < maxArea){
                if(objLocation[1] > (centerImgX + imgWidth / 3)) {
                    directionController.moveRight();
                    ledController.turnRightLEDon();
                    System.out.println("Turning right!");
                }
                else if(objLocation[1] < (centerImgX - imgWidth / 3)){
                    directionController.moveLeft();
                    ledController.turnLeftLEDon();
                    System.out.println("Turning left!");
                }
                else{
                    directionController.moveForward();
                    ledController.forwardLEDon();
                    System.out.println("Forward!");
                }
            }else if(objLocation[0] < minArea){
                directionController.moveLeft();
                ledController.turnLeftLEDon();
                System.out.println("Target isn't large enough, searching!");
            }else{
                directionController.stop();
                ledController.turnLEDoff();
                System.out.println("Target is large enough, stopping!");
            }
        }else{
            directionController.moveLeft();
            ledController.turnLeftLEDon();
            System.out.println("Target not found, searching!");
        }

    }


}
