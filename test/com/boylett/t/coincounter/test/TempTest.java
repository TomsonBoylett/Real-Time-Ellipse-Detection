/*
 * Demonstrates gaussian blur
 */
package com.boylett.t.coincounter.test;

import com.boylett.t.coincounter.EllipseDetection;
import com.boylett.t.coincounter.QuadrantSet;
import java.lang.reflect.Method;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class TempTest {
    
    public static class EDGaussianBlur implements WebcamDriver.Process {
        @Override
        public Mat process(Mat m) {
            Mat copy = new Mat();
            m.copyTo(copy);
            EllipseDetection ed = new EllipseDetection.Builder().build();
            
            List<RotatedRect> es = ed.detect(m);
            for (RotatedRect e : es) {
                Imgproc.ellipse(copy, e, new Scalar(255, 0, 0), 2);
            }
            
            return copy;
        }
    }
    
    public static void main(String args[]) {
        // Get webcam driver
        WebcamDriver wd = WebcamDriver.getSingleton();
        
        // Add image processes
        wd.getProcessList().add(new EDGaussianBlur());;
        
        // Start webcam driver
        wd.start();
    }
}
