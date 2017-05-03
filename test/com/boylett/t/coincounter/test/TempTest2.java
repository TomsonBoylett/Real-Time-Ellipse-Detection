/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boylett.t.coincounter.test;

import com.atul.JavaOpenCV.ImShow;
import com.boylett.t.coincounter.EllipseDetection;
import com.boylett.t.coincounter.QuadrantSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author tomson
 */
public class TempTest2 {
    public static void main(String[] args) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat m = Imgcodecs.imread("/home/tomson/Documents/CoinCounting/test2.png");
        EllipseDetection ed = new EllipseDetection();
        QuadrantSet qs = ed.findQuadrantSet(m);
        
        Imgproc.polylines(m, qs.getArcI(), false, new Scalar(0,0,255), 2);
        Imgproc.polylines(m, qs.getArcII(), false, new Scalar(0,0,255), 2);
        Imgproc.polylines(m, qs.getArcIII(), false, new Scalar(0,0,255), 2);
        Imgproc.polylines(m, qs.getArcIV(), false, new Scalar(0,0,255), 2);
        
        ed.pickArcs();
        ed.fitEllipses();
        ed.removeDuplicates();
        
        List<RotatedRect> es = ed.getEllipses();
        for (RotatedRect e : es) {
            Imgproc.ellipse(m, e, new Scalar(255, 0, 0), 2);
        }
        
        //Imgproc.polylines(m, ed.getArcComb(), false, new Scalar(255,0,0), 2);
        /*
        for (MatOfPoint mop : qs.getArcIII()) {
            System.out.println(Arrays.toString(mop.toArray()));
        }
        for (MatOfPoint mop : qs.getArcIV()) {
            System.out.println(Arrays.toString(mop.toArray()));
        }
        
        for (int i = 0; i < 4; i++) {
            List<List<MatOfPoint>> pairs = ed.getCandidatePairs(i);
            for(List<MatOfPoint> pair : pairs) {
                List<Point> pair1 = new ArrayList(pair.get(0).toList());
                pair1.addAll(pair.get(1).toList());
                List<MatOfPoint> lol = new ArrayList<>();
                MatOfPoint mop = new MatOfPoint();
                mop.fromList(pair1);
                System.out.println(Arrays.toString(mop.toArray()));
                lol.add(mop);
                Imgproc.polylines(m, lol, false, new Scalar(255,0,0), 2);
            }
        }

        //System.out.println(Arrays.toString(ed.getArcComb().get(0).toArray()));
        
        /*
        Imgproc.polylines(m, ed.getArcComb(), false, new Scalar(0,255,0), 2);
        
        Imgproc.polylines(m, qs.getArcI(), false, new Scalar(0,255,0), 2);
        Imgproc.polylines(m, qs.getArcII(), false, new Scalar(0,255,0), 2);
        Imgproc.polylines(m, qs.getArcIII(), false, new Scalar(0,255,0), 2);
        Imgproc.polylines(m, qs.getArcIV(), false, new Scalar(0,255,0), 2);
        */
        ImShow im = new ImShow("Test");
        im.showImage(m);
    }
    
    private static double distance(double p1X, double p1Y, double p2X, double p2Y) {
        return Math.sqrt(Math.pow(p1X - p2X, 2) + Math.pow(p1Y - p2Y, 2));
    }
}
