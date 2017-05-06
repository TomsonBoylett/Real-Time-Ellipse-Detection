/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boylett.t.coincounter;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;

/**
 * Decides if two arcs come from the same ellipse or not.
 * Arcs must only be made up of 2 end points and a mid point.
 * 
 * Related:
 * http://i.imgur.com/7rf0Vrp.png
 * 
 * @author tomson
 */
public class MatchArcs {
    public static final int ARCS_PER_SET = 3;
    private static final double CNC_THRESH = 0.5;
    private static final double NO_MATCH = -10.0; // Value returned when 2 of the lines a parallel
    
    private double threshold; // How close to 1 does the cnc value need to be?
    private Point[] p; // Points where the lines in q intersect
    private Line[] q; // Lines formed from 2 arcs
    private double[][] a; // Coefficients used to calculate cnc
    private double[][] b; // Coefficients used to calculate cnc
    private Mat m = new Mat(2, 1, CvType.CV_64FC1); // Used to solve simultaneous equation
    private Mat k = new Mat(2, 2, CvType.CV_64FC1); // Used to solve simultaneous equation
    
    public MatchArcs() {
        threshold = CNC_THRESH;
    }
    
    /**
     * 
     * @param threshold 
     */
    public MatchArcs(double threshold) {
        if (threshold < 0) {
            throw new InvalidParameterException("Threshold must be positive!");
        }
        this.threshold = threshold;
    }
    
    /**
     * Returns true if two arcs are likely to come from the same ellipse.
     * 
     * @param arc1
     * @param arc2
     * @return True if both arcs come from the same ellipse
     */
    public boolean isMatch(Point[] arc1, Point[] arc2) {
        double cnc = cnc(arc1, arc2);
        
        /* 
        Let X be one possible cnc value of 2 arcs.
        
        Depending on the ordering of the arc inputs you can get the value X
        or 1/X.
        
        This means that as the threshold value gets larger the results
        become more and more inconsistent.
        
        This if statement helps resolve this issue.
        */
        if (cnc > 1.0) {
            cnc = 1.0 / cnc;
        }
        return Math.abs(1 - cnc) < threshold;
    }
    
    public boolean isMatch(MatOfPoint arc1, MatOfPoint arc2) {
        return isMatch(arc1.toArray(), arc2.toArray());
    }
    
    public double cnc(Point[] arc1, Point[] arc2) {
        if (arc1.length != KeyPoint.LENGTH || arc2.length != KeyPoint.LENGTH) {
            throw new InvalidParameterException("Arcs must contain exactly 3 points");
        }
        
        calcQ(arc1, arc2);
        if (!calcP()) {
            return NO_MATCH;
        }
        
        return calculateCNC();
    }
    
    private Line[] calcQ(Point[] arc1, Point[] arc2) {
        q = new Line[3];
        int[] close = findClosestPointsBetweenArcs(arc1, arc2);
        int[] far = {close[0] == 0 ? 2 : 0, close[1] == 0 ? 2 : 0};
        
        q[0] = new Line(arc1[far[0]], arc1[1]);
        
        q[1] = new Line(arc1[close[0]], arc2[close[1]]);
        
        q[2] = new Line(arc2[1], arc2[far[1]]);
        
        return q;
    }
    
    private boolean calcP() {
        p = new Point[3];
        for (int i = 0; i < 3; i++) {
            // j is index of the next line going clockwise around the triangle
            int j = (i + 2) % 3;
            p[i] = q[i].intersect(q[j]);
            if (p[i] == null) {
                return false;
            }
        }
        return true;
    }
    
    private double calculateCNC() {
        calculateAllCoeffs();
        double product = 1.0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                product *= (b[i][j] / a[i][j]);
            }
        }
        return product;
    }
    
    private void calculateAllCoeffs() {
        a = new double[3][2];
        b = new double[3][2];
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 2; j++) {
                calculateCoeff(i, j);
            }
        }
    }
    
    private void calculateCoeff(int i, int j) {
        k.put(0, 0, p[i].x);
        k.put(0, 1, p[(i+1)%3].x);
        k.put(1, 0, p[i].y);
        k.put(1, 1, p[(i+1)%3].y);
        
        m.put(0, 0, q[i].get(j).x);
        m.put(1, 0, q[i].get(j).y);
        
        Core.invert(k, k);
        
        Core.gemm(k, m, 1, new Mat(), 1, m);
        
        a[i][j] = m.get(0, 0)[0];
        b[i][j] = m.get(1, 0)[0];
    }

    /**
     * @param arc1
     * @param arc2
     * @return 
     */
    private int[] findClosestPointsBetweenArcs(Point[] arc1, Point[] arc2) {
        double minDist = Double.MAX_VALUE;
        int[] minIndex = {-1, -1};
        
        for (int i = 0; i <= 2; i += 2) {
            for (int j = 0; j <= 2; j += 2) {
                double dist = distance(arc1[i], arc2[j]);
                if (dist < minDist) {
                    minDist = dist;
                    minIndex[0] = i;
                    minIndex[1] = j;
                }
            }
        }
        
        return minIndex;
    }

    /**
     * @param p1
     * @param p2
     * @return Distance between points.
     */
    private double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }
}
