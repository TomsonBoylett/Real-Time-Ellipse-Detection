/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boylett.t.coincounter;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

/**
 * Provides static methods for getting the key points of contours
 */
public class KeyPoint {
    public static final int LENGTH = 3;
    
    /**
     * Returns a list of contours where each contour only contains the first, last and
     * middle points of its corresponding original.
     * 
     * @param contours List of contours
     * @return List of key points
     */
    public static List<MatOfPoint> createKeyPointList(List<MatOfPoint> contours) {
        List<MatOfPoint> keyPointsList = new ArrayList<>();
        
        for (MatOfPoint contour : contours) {
            keyPointsList.add(fromMatOfPoint(contour));
        }
        
        return keyPointsList;
    }
    
    /**
     * Returns a contour containing only the first, last and middle points of the
     * original contour.
     *
     * @param contour
     * @return firstMidLastContour
     */
    public static MatOfPoint fromMatOfPoint(MatOfPoint contour) {
        if (contour.rows() < LENGTH) {
            throw new InvalidParameterException(
                    "Contour must have atleast " + LENGTH + " points.");
        }

        Point start = new Point();
        Point mid = new Point();
        Point end = new Point();
        int lastRow = contour.rows() - 1;
        
        // Not sure why you need to divide by 2 and 4 here to get the mid and
        // end points.
        start.set(contour.get(0, 0));
        mid.set(contour.get(lastRow / 4, 0));
        end.set(contour.get(lastRow / 2, 0));
        
        return new MatOfPoint(start, mid, end);
    }
}
