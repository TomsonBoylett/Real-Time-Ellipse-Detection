/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boylett.t.coincounter;

import java.security.InvalidParameterException;
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
 * @author tomson
 */
public class MatchArcs {
    private static final int LENGTH = 3;
    private static final double CNC_THRESH = 0.35;
    
    private static double[][] p = new double[3][2];
    private static double[][][] q = new double[3][2][2];
    private static double[][] a = new double[3][2];
    private static double[][] b = new double[3][2];
    
    public static boolean isMatch(MatOfPoint arc1, MatOfPoint arc2) {
        return isMatch(arc1.toArray(), arc2.toArray());
    }
    
    /**
     * Returns true if two arcs are likely to come from the same ellipse.
     * 
     * @param arc1
     * @param arc2
     * @return True if both arcs come from the same ellipse
     */
    public static boolean isMatch(Point[] arc1, Point[] arc2) {
        return Math.abs(1 - cnc(arc1, arc2)) < CNC_THRESH;
    }
    
    public static double cnc(Point[] arc1, Point[] arc2) {
        if (arc1.length != LENGTH || arc2.length != LENGTH) {
            throw new InvalidParameterException("Arcs must only be made up of 2 end points and a mid point");
        }
        setQ(arc1, arc2);
        return calculateCNC();
    }
    
    private static void setQ(Point[] arc1, Point[] arc2) {
        int[] closest = findClosestPointsBetweenArcs(arc1, arc2);
        int[] furthest = {closest[0] == 0 ? 2 : 0, closest[1] == 0 ? 2 : 0};
        
        // Set midpoints
        // 3 lines, 2 points per line, each point has x and y
        q[0][1][0] = arc1[1].x;
        q[0][1][1] = arc1[1].y;
        q[2][0][0] = arc2[1].x;
        q[2][0][1] = arc2[1].y;

        // Set closest endpoints
        q[1][0][0] = arc1[closest[0]].x;
        q[1][0][1] = arc1[closest[0]].y;
        q[1][1][0] = arc2[closest[1]].x;
        q[1][1][1] = arc2[closest[1]].y;

        // Set furthest endpoints
        q[0][0][0] = arc1[furthest[0]].x;
        q[0][0][1] = arc1[furthest[0]].y;
        q[2][1][0] = arc2[furthest[1]].x;
        q[2][1][1] = arc2[furthest[1]].y;
    }
    
    private static void calculateP() {
        p[0] = intersection(q[0][0], q[0][1], q[2][1], q[2][0]);
        p[1] = intersection(q[0][0], q[0][1], q[1][0], q[1][1]);
        p[2] = intersection(q[2][1], q[2][0], q[1][0], q[1][1]);
    }
    
    private static double calculateCNC() {
        calculateP();
        for (int i = 0; i < p.length; i++) {
            if (p[i] == null) {
                return -1.0;
            }
        }
        calculateAllCoeffs();
        double product = 1.0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                product *= (b[i][j] / a[i][j]);
            }
        }
        return product;
    }
    
    private static void calculateAllCoeffs() {
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 2; j++) {
                calculateCoeff(i, j);
            }
        }
    }
    
    private static void calculateCoeff(int i, int j) {
        Mat K = new Mat(2, 2, CvType.CV_64FC1);
        K.put(0, 0, p[i][0]);
        K.put(0, 1, p[(i+1)%3][0]);
        K.put(1, 0, p[i][1]);
        K.put(1, 1, p[(i+1)%3][1]);
        
        Mat M = new Mat(2, 1, CvType.CV_64FC1);
        M.put(0, 0, q[i][j][0]);
        M.put(1, 0, q[i][j][1]);
        
        Core.invert(K, K);
        
        Core.gemm(K, M, 1, new Mat(), 1, M);
        
        a[i][j] = M.get(0, 0)[0];
        b[i][j] = M.get(1, 0)[0];
    }
    
    /**
     * Computes the intersection between two lines.
     * (c) 2007 Alexander Hristov. Use Freely (LGPL
     * license). http://www.ahristov.com
     *
     * @param p1 Point 1 of line 1
     * @param p2 Point 2 of line 1
     * @param p3 Point 1 of line 2
     * @param p4 Point 2 of line 2
     *
     * @return Point where the segments intersect, or null if they don't
     */
    private static double[] intersection(
            double[] p1, double[] p2,
            double[] p3, double[] p4) {
        double d = (p1[0] - p2[0]) * (p3[1] - p4[1]) - (p1[1] - p2[1]) * (p3[0] - p4[0]);
        if (d == 0) {
            return null;
        }

        double xi = ((p3[0] - p4[0]) * (p1[0] * p2[1] - p1[1] * p2[0]) - (p1[0] - p2[0]) * (p3[0] * p4[1] - p3[1] * p4[0])) / d;
        double yi = ((p3[1] - p4[1]) * (p1[0] * p2[1] - p1[1] * p2[0]) - (p1[1] - p2[1]) * (p3[0] * p4[1] - p3[1] * p4[0])) / d;

        return new double[]{xi, yi};
    }

    private static int[] findClosestPointsBetweenArcs(Point[] arc1, Point[] arc2) {
        double minDist = Double.MAX_VALUE;
        int[] minIndex = {-1, -1};
        for (int i = 0; i <= 2; i += 2) {
            for (int j = 0; j <= 2; j += 2) {
                double dist = distance(arc1[i].x, arc1[i].y, arc2[j].x, arc2[j].y);
                if (dist < minDist) {
                    minDist = dist;
                    minIndex[0] = i;
                    minIndex[1] = j;
                }
            }
        }
        return minIndex;
    }

    public static double distance(double p1X, double p1Y, double p2X, double p2Y) {
        return Math.sqrt(Math.pow(p1X - p2X, 2) + Math.pow(p1Y - p2Y, 2));
    }
    
    public static double calcMinDistance(MatOfPoint arc1, MatOfPoint arc2) {
        return calcMinDistance(arc1.toArray(), arc2.toArray());
    }
    
    public static double calcMinDistance(Point[] arc1, Point[] arc2) {
        if (arc1.length != LENGTH || arc2.length != LENGTH) {
            throw new InvalidParameterException("Arcs must only be made up of 2 end points and a mid point");
        }
        int[] closest = findClosestPointsBetweenArcs(arc1, arc2);
        return distance(arc1[closest[0]].x, arc1[closest[0]].y,
                        arc2[closest[1]].x, arc2[closest[1]].y);
    }
}
