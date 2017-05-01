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
 * A mat of points which can only store exactly 3 points.
 * 
 * This class also provides static methods for converting from a MatOfPoint
 */
public class KeyPoints extends MatOfPoint {

    public KeyPoints(Point... a) {
        super(a);
        if (a.length != 3) {
            throw new InvalidParameterException("Must provide exactly 3 points.");
        }
    }
    
    public double[] get(int row) {
        return super.get(row, 0);
    }
    
    public void put(int row, double[] point) {
        super.put(row, 0, point);
    }

    @Override
    public void fromList(List<Point> lp) {
        super.fromList(lp); //To change body of generated methods, choose Tools | Templates.
        if (lp.size() != 3) {
            throw new InvalidParameterException("List must contain exactly 3 points.");
        }
    }

    @Override
    public void fromArray(Point... a) {
        super.fromArray(a); //To change body of generated methods, choose Tools | Templates.
        if (a.length != 3) {
            throw new InvalidParameterException("Array must contain exactly 3 points.");
        }
    }
    
    /**
     * Returns a list of contours where each contour only contains the first, last and
     * middle points of its corresponding original.
     * 
     * @param contours List of contours
     * @return List of key points
     */
    public static List<KeyPoints> createKeyPointList(List<MatOfPoint> contours) {
        List<KeyPoints> keyPointsList = new ArrayList<>();
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
    public static KeyPoints fromMatOfPoint(MatOfPoint contour) {
        if (contour.rows() < 3) {
            throw new InvalidParameterException(
                    "Contour must have atleast 3 points.");
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
        
        return new KeyPoints(start, mid, end);
    }
}
