/*
 * Demonstrates finding contours in an image
 */
package com.boylett.t.coincounter.test;

import com.boylett.t.coincounter.EllipseDetection;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class EDContourTest {
    
    public static class EDContour implements WebcamDriver.Process {
        @Override
        public Mat process(Mat m) {
            EllipseDetection ed = new EllipseDetection(m);
            
            try {
                Mat edges = new Mat();
                m.copyTo(edges);
                edges = new EDCannyTest.EDCanny().process(edges);
                
                Method findContours = null;
                findContours = ed.getClass().getDeclaredMethod("findContours", Mat.class);
                findContours.setAccessible(true);
                List<MatOfPoint> contours =
                        (List<MatOfPoint>) findContours.invoke(ed, edges);
                Imgproc.drawContours(m, contours, -1, new Scalar(0, 255, 0), 2);
                
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            
            return m;
        }
    }
    
    public static void main(String args[]) {
        // Get webcam driver
        WebcamDriver wd = WebcamDriver.getSingleton();
        
        // Add image processes
        wd.getProcessList().add(new EDGaussianBlurTest.EDGaussianBlur());
        wd.getProcessList().add(new EDContour());
        
        // Start webcam driver
        wd.start();
    }
}
