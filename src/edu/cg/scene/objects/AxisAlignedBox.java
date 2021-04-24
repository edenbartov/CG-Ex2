package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.*;

import java.util.HashSet;
import java.util.Set;


// TODO Implement this class which represents an axis aligned box
public class AxisAlignedBox extends Shape{
    private final static int NDIM=3; // Number of dimensions
    private Point a = null;
    private Point b = null;
    private double[] aAsArray;
    private double[] bAsArray;

    public AxisAlignedBox(Point a, Point b){
        this.a = a;
        this.b = b;
        // We store the points as Arrays - this could be helpful for more elegant implementation.
        aAsArray = a.asArray();
        bAsArray = b.asArray();
        assert (a.x <= b.x && a.y<=b.y && a.z<=b.z);
    }

    @Override
    public String toString() {
        String endl = System.lineSeparator();
        return "AxisAlignedBox:" + endl +
                "a: " + a + endl +
                "b: " + b + endl;
    }

    public AxisAlignedBox initA(Point a){
        this.a = a;
        aAsArray = a.asArray();
        return this;
    }

    public AxisAlignedBox initB(Point b){
        this.b = b;
        bAsArray = b.asArray();
        return this;
    }

    // get all 6 faces of the box with the axis normals and the 2 points
    private Plain[] getFaces() {
        Plain[] faces = new Plain[6];
        Vec[] unitVectors  = new Vec[] {
            new Vec(1, 0 ,0), new Vec(0, 1, 0), new Vec(0 ,0 ,1)
        };
        int index = 0;
        for (Vec normal : unitVectors) {
            for (Point point : new Point[] {a, b}) {
                faces[index++] = new Plain(normal, point);
            }
        }
        return faces;
    }

    /**
     * Determine if a point of intersection between a ray and the plain containing one of the
     * faces of the box is inside the box
     * @param ray The ray
     * @param hit The intersection
     * @return true iff the point of intersection is in the box
     */
    private boolean isPointInBox(Ray ray, Hit hit) {
        Point hitPoint = ray.getHittingPoint(hit);
        double xPoint = hitPoint.x - Ops.epsilon;
        double yPoint = hitPoint.y - Ops.epsilon;
        double zPoint = hitPoint.z - Ops.epsilon;
        boolean x = a.x <= xPoint && b.x >= xPoint;
        boolean y = a.y <= yPoint && b.y >= yPoint;
        boolean z = a.z <= zPoint && b.z >= zPoint;
        return x && y && z;
    }

    @Override
    public Hit intersect(Ray ray) {
        // DONE Implement:
        Plain[] faces = getFaces();
        Hit minHit = null;
        for (Plain face : faces) {
            Hit hit = face.intersect(ray);
            if (hit != null && isPointInBox(ray, hit)) {
                if (minHit == null) {
                    minHit = hit;
                } else if (hit.compareTo(minHit) < 0) {
                    minHit = hit;
                }
            }
        }
        return minHit;
    }
}

