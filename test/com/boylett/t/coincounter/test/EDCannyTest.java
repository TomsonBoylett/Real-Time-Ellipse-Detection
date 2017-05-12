/*
 * Demonstrates canny edge detection
 */
package com.boylett.t.coincounter.test;

import com.boylett.t.coincounter.EllipseDetection;
import com.boylett.t.coincounter.HalfSetDetection;
import java.lang.reflect.Method;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class EDCannyTest {
    
    public static class EDCanny implements WebcamDriver.Process {
        @Override
        public Mat process(Mat m) {
            Mat copy = new Mat();
            m.copyTo(copy);
            HalfSetDetection hsd = new HalfSetDetection.Builder().build();
            hsd.detectArcHalfSets(m);
            
            Imgproc.polylines(copy, hsd.getContoursII_IV(), false, new Scalar(0,255,0));
            Imgproc.polylines(copy, hsd.getContoursI_III(), false, new Scalar(255,0,0));
            return copy;
        }
    }
    
    public static void main(String args[]) {
        // Get webcam driver
        WebcamDriver wd = WebcamDriver.getSingleton();
        
        // Add image processes
        wd.getProcessList().add(new EDCanny());
        
        // Start webcam driver
        wd.start();
    }
}
