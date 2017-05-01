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
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class EDStartMidEndTest {
    
    public static class EDStartMidEnd implements WebcamDriver.Process {
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
                
                Method filterCont = null;
                filterCont = ed.getClass().getDeclaredMethod("filterContourByLength", List.class, int.class);
                filterCont.setAccessible(true);
                filterCont.invoke(ed, contours, 10);
                
                Method findSME = null;
                findSME = ed.getClass().getDeclaredMethod("getSMEList", List.class);
                findSME.setAccessible(true);
                contours = (List<MatOfPoint>) findSME.invoke(ed, contours);
                for (MatOfPoint contour : contours) {
                    Point start = new Point(contour.get(0, 0));
                    Point mid = new Point(contour.get(1, 0));
                    Point end = new Point(contour.get(2, 0));
                    Imgproc.circle(m, start, 5, new Scalar(255, 0, 0), -1);
                    Imgproc.circle(m, mid, 5, new Scalar(0, 0, 255), -1);
                    Imgproc.circle(m, end, 5, new Scalar(255, 0, 0), -1);
                }
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
        wd.getProcessList().add(new EDStartMidEnd());
        
        // Start webcam driver
        wd.start();
    }
}
