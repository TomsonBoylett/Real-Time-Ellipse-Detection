/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boylett.t.coincounter;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.CvType;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;

/**
 *
 * @author tomson
 */
public class ArcPicking {
    MatchArcs ma;
    
    public ArcPicking(MatchArcs ma) {
        this.ma = ma;
    }
    
    public List<MatOfPoint2f> pickArcs(QuadrantSet qs) {
        List<MatOfPoint2f> arcComb = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            arcPicking(qs, arcComb, i);
        }
        return arcComb;
    }
    
    private void arcPicking(QuadrantSet qs, List<MatOfPoint2f> arcComb, int i) {
        List<MatOfPoint> arcs1 = qs.get(i);
        List<MatOfPoint> arcs2 = qs.get(Math.floorMod(i - 1, 4));
        List<MatOfPoint> arcs3 = qs.get(Math.floorMod(i - 2, 4));
        
        for (int i2 = 0; i2 < arcs2.size(); i2++) {
            for (int i1 = 0; i1 < arcs1.size(); i1++) {
                if (!meetsCoordCon(arcs1.get(i1), arcs2.get(i2), i) ||
                    !ma.isMatch(arcs1.get(i1), arcs2.get(i2))) {
                    continue;
                }
                for (int i3 = 0; i3 < arcs3.size(); i3++) {
                    if (!meetsCoordCon(arcs2.get(i2), arcs3.get(i3), Math.floorMod(i - 1, 4))) {
                        continue;
                    }
                    MatOfPoint pointSet = new MatOfPoint();
                    pointSet.alloc(arcs1.get(i1).rows() + arcs2.get(i2).rows() + arcs3.get(i3).rows());
                    
                    int[] buffer = new int[2 * arcs1.get(i1).rows()];
                    arcs1.get(i1).get(0, 0, buffer);
                    pointSet.put(0, 0, buffer);
                    
                    buffer = new int[2 * arcs2.get(i2).rows()];
                    arcs2.get(i2).get(0, 0, buffer);
                    pointSet.put(arcs1.get(i1).rows(), 0, buffer);
                    
                    buffer = new int[2 * arcs3.get(i3).rows()];
                    arcs3.get(i3).get(0, 0, buffer);
                    pointSet.put(arcs1.get(i1).rows() + arcs2.get(i2).rows(), 0, buffer);
                    
                    MatOfPoint2f dst = new MatOfPoint2f();
                    pointSet.convertTo(dst, CvType.CV_32F);
                    arcComb.add(dst);
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
        
        throw new InvalidParameterException("inc must be between 0 and 3");
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
