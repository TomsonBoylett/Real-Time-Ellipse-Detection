package com.boylett.t.coincounter;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

/**
 *
 * @author tomson
 */
public class EllipseDetection {
    
    List<MatOfPoint> arcComb;
    QuadrantSet qs;

    public EllipseDetection() {
        arcComb = new ArrayList<>();
    }

    /**
     * As part of the ellipse detection we first have to find 4 candidate arcs.
     * This function returns 4 images that contain edges corresponding to the 4
     * arcs that could make up a full ellipse.
     *
     * @param img image
     * @return 4 quadrant sets
     */
    public QuadrantSet findQuadrantSet(Mat img) {
        QuadrantSetDetection qsd = new QuadrantSetDetection();
        qs = qsd.findQuadrantSet(img);
        return qs;
    }
    
    public List<MatOfPoint> getArcComb() {
        return arcComb;
    }
    
    public List<List<MatOfPoint>> getCandidatePairs(int i) {
        List<List<MatOfPoint>> pairs = new ArrayList<>();
        List<MatOfPoint> arcs1 = qs.get(i);
        List<MatOfPoint> arcs2 = qs.get(Math.floorMod(i - 1, 4));
        
        ListIterator<MatOfPoint> it2 = arcs2.listIterator();
        while(it2.hasNext()) {
            MatOfPoint arc2 = it2.next();
            ListIterator<MatOfPoint> it1 = arcs1.listIterator();
            while(it1.hasNext()) {
                MatOfPoint arc1 = it1.next();
                if (!meetsCoordCon(arc1, arc2, i) ||
                        !MatchArcs.isMatch(arc1, arc2)) {
                    continue;
                }
                
                pairs.add(Arrays.asList(new MatOfPoint[]{arc1,arc2}));
            }
        }
        return pairs;
    }
    
    public void arcPicking(int i) {
        List<MatOfPoint> arcs1 = qs.get(i);
        List<MatOfPoint> arcs2 = qs.get(Math.floorMod(i - 1, 4));
        List<MatOfPoint> arcs3 = qs.get(Math.floorMod(i - 2, 4));
        
        ListIterator<MatOfPoint> it2 = arcs2.listIterator();
        while(it2.hasNext()) {
            MatOfPoint arc2 = it2.next();
            ListIterator<MatOfPoint> it1 = arcs1.listIterator();
            while(it1.hasNext()) {
                MatOfPoint arc1 = it1.next();
                
                if (!meetsCoordCon(arc1, arc2, i) ||
                        !MatchArcs.isMatch(arc1, arc2)) {
                    continue;
                }
                
                ListIterator<MatOfPoint> it3 = arcs3.listIterator();
                while(it3.hasNext()) {
                    MatOfPoint arc3 = it3.next();
                    
                    if (!meetsCoordCon(arc2, arc3, Math.floorMod(i - 1, 4)) ||
                            !MatchArcs.isMatch(arc2, arc3)) {
                        continue;
                    }
                    
                    MatOfPoint pointSet = new MatOfPoint();
                    pointSet.alloc(arc1.rows() + arc2.rows() + arc3.rows());
                    
                    int[] buffer = new int[2 * arc1.rows()];
                    arc1.get(0, 0, buffer);
                    pointSet.put(0, 0, buffer);
                    
                    buffer = new int[2 * arc2.rows()];
                    arc2.get(0, 0, buffer);
                    pointSet.put(arc1.rows(), 0, buffer);
                    
                    buffer = new int[2 * arc3.rows()];
                    arc3.get(0, 0, buffer);
                    pointSet.put(arc1.rows() + arc2.rows(), 0, buffer);
                    
                    arcComb.add(pointSet);
                }
            }
        }
    }
    
    /**
     * 
     * @param arc1
     * @param arc2 must be in the quadrant 90 degrees clockwise from arc1
     * @param inc the quadrant arc1 is in where 0 is top right going clockwise
     * @return 
     */
    private boolean meetsCoordCon(MatOfPoint arc1, MatOfPoint arc2, int inc) {
        if (inc < 0 || inc > 3) {
            throw new InvalidParameterException("inc must be between 0 and 3");
        }
        
        switch(inc) {
            case 0:
                return maxY(arc1) < minY(arc2);
                
            case 3:
                return minX(arc1) > maxX(arc2);
                
            case 2:
                return minY(arc1) > maxY(arc2);
                
            case 1:
                return maxX(arc1) < minX(arc2);
        }
        
        throw new RuntimeException("This should never be executed");
    }
    
    private double minX(MatOfPoint arc) {
        return arc.get(0, 0)[0] < arc.get(2, 0)[0] ? arc.get(0, 0)[0] : arc.get(2, 0)[0];
    }
    
    private double maxX(MatOfPoint arc) {
        return arc.get(0, 0)[0] > arc.get(2, 0)[0] ? arc.get(0, 0)[0] : arc.get(2, 0)[0];
    }
    
    private double minY(MatOfPoint arc) {
        return arc.get(0, 0)[1] < arc.get(2, 0)[1] ? arc.get(0, 0)[1] : arc.get(2, 0)[1];
    }
    
    private double maxY(MatOfPoint arc) {
        return arc.get(0, 0)[1] > arc.get(2, 0)[1] ? arc.get(0, 0)[1] : arc.get(2, 0)[1];
    }
}
