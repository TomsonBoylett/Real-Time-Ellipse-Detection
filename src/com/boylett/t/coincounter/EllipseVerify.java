/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boylett.t.coincounter;

import java.security.InvalidParameterException;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author tomson
 */
public class EllipseVerify {
    private static final int POINTS = KeyPoint.LENGTH * MatchArcs.ARCS_PER_SET;
    
    private int minCount;
    private double perimeterRatioMin;
    private double ellipseThresh;

    private EllipseVerify(int minCount, double perimeterRatioMin, double ellipseThresh) {
        this.minCount = minCount;
        this.perimeterRatioMin = perimeterRatioMin;
        this.ellipseThresh = ellipseThresh;
    }
    
    public static class Builder {
        private static final int MIN_COUNT = 9;
        private static final double PERIMETER_RATIO_MIN = 0.2;
        private static final double ELLIPSE_THRESH = 0.09;
    
        private int minCount;
        private double perimeterRatioMin;
        private double ellipseThresh;
        
        public Builder() {
            setToDefault();
        }
        
        public final void setToDefault() {
            this.minCount = MIN_COUNT;
            this.perimeterRatioMin = PERIMETER_RATIO_MIN;
            this.ellipseThresh = ELLIPSE_THRESH;
        }
        
        public Builder setMinCount(int minCount) {
            // Each candidate arc has 3 points and each candidate set has 3 arcs
            if (minCount < 0 || minCount > POINTS) {
                throw new InvalidParameterException("minCount must be between 0 and " + POINTS + " inclusive");
            }
            this.minCount = minCount;
            return this;
        }
        
        public Builder setPerimeterRatioMin(double perimeterRatioMin) {
            if (perimeterRatioMin < 0.0 || perimeterRatioMin > 1.0) {
                throw new InvalidParameterException("Perimeter ratio must be between 0 and 1 inclusive");
            }
            this.perimeterRatioMin = perimeterRatioMin;
            return this;
        }
        
        public Builder setEllipseThresh(double ellipseThresh) {
            if (ellipseThresh < 0.0) {
                throw new InvalidParameterException("Ellipse threshold must be positive");
            }
            this.ellipseThresh = ellipseThresh;
            return this;
        }
        
        public EllipseVerify build() {
            return new EllipseVerify(minCount, perimeterRatioMin, ellipseThresh);
        }
    }
    
    public boolean verify(MatOfPoint2f mop, RotatedRect e) {
        if (mop.rows() != POINTS) {
            throw new InvalidParameterException("Must be 9 points in candidate set");
        }
        return ellipseMatchesPoints(mop, e) && stableEllipse(mop, e);
    }
    
    private boolean ellipseMatchesPoints(MatOfPoint2f mop, RotatedRect ellipse) {
        Point[] points = mop.toArray();
        double count = 0;
        for (Point p : points) {
            if (pointOnEllipse(p, ellipse)) {
                count++;
            }
        }
        return count >= minCount;
    }
    
    public static double dispFromEllipse(Point p, RotatedRect e) {
        double h = e.center.x;
        double k = e.center.y;
        double A = (e.angle * Math.PI) / 180.0;
        double a = e.size.width / 2.0;
        double b = e.size.height / 2.0;
        
        return (Math.pow((p.x - h) * Math.cos(A) + (p.y - k) * Math.sin(A), 2.0) / Math.pow(a, 2.0)) +
               (Math.pow((p.x - h) * Math.sin(A) - (p.y - k) * Math.cos(A), 2.0) / Math.pow(b, 2.0));
        
    }
    
    private boolean pointOnEllipse(Point p, RotatedRect e) {
        return Math.abs(dispFromEllipse(p, e) - 1) < ellipseThresh;
    }
    
    private boolean stableEllipse(MatOfPoint2f mop, RotatedRect ellipse) {
        double totalLength = Imgproc.arcLength(new MatOfPoint2f(mop.submat(0, 3, 0, 1)), false) +
                             Imgproc.arcLength(new MatOfPoint2f(mop.submat(3, 6, 0, 1)), false) +
                             Imgproc.arcLength(new MatOfPoint2f(mop.submat(6, 9, 0, 1)), false);
        
        return totalLength / ellipsePerimeter(ellipse) >= perimeterRatioMin;
    }
    
    private double ellipsePerimeter(RotatedRect e) {
        return 2.0 * Math.PI * Math.sqrt(
                                (Math.pow(e.size.width, 2.0) + Math.pow(e.size.height, 2.0))
                                / 2.0
        );
    }
}
