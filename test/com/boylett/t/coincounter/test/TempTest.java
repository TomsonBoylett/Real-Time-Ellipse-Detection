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
            EllipseDetection ed = new EllipseDetection();
            
            QuadrantSet qs = ed.findQuadrantSet(m);
            
            ed.pickArcs();
            
            ed.fitEllipses();
            
            ed.removeDuplicates();
            
            Imgproc.polylines(m, qs.getArcI(), false, new Scalar(0,255,0));
            Imgproc.polylines(m, qs.getArcII(), false, new Scalar(0,255,0));
            Imgproc.polylines(m, qs.getArcIII(), false, new Scalar(0,255,0));
            Imgproc.polylines(m, qs.getArcIV(), false, new Scalar(0,255,0));
            
            List<RotatedRect> es = ed.getEllipses();
            for (RotatedRect e : es) {
                Imgproc.ellipse(m, e, new Scalar(255, 0, 0), 2);
            }
            
            return m;
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
