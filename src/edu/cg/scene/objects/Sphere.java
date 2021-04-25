package edu.cg.scene.objects;

import edu.cg.Logger;
import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.*;

// TODO Implement this class which represents a sphere
public class Sphere extends Shape {
	private Point center;
	private double radius;

	public Sphere(Point center, double radius) {
		this.center = center;
		this.radius = radius;
	}

	public Sphere() {
		this(new Point(0, -0.5, -6), 0.5);
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Sphere:" + endl +
				"Center: " + center + endl +
				"Radius: " + radius + endl;
	}


	public Sphere initCenter(Point center) {
		this.center = center;
		return this;
	}

	public Sphere initRadius(double radius) {
		this.radius = radius;
		return this;
	}

	@Override
	public Hit intersect(Ray ray) {
		// TODO Implement:
		Vec sourceToCenter = ray.source().sub(this.center);

		double b = ((ray.direction()).mult(2)).dot(sourceToCenter);
		double c = (ray.source()).distSqr(this.center) - (this.radius * this.radius);

		double discriminant = (b * b) - (4 * c);
		if (discriminant < 0) {
			return null;
		}
		double root = Math.sqrt(discriminant);
		double t;
		if (root < Ops.epsilon) {
			t = -b / 2;
			if (t < Ops.epsilon || t > Ops.infinity) {
				return null;
			}
			return new Hit(t, ((ray.add(t)).sub(this.center)).normalize());
		}
		double t1 = (-b - root) / 2;
		double t2 = (-b + root) / 2;
		if (t1 < Ops.epsilon && t2 < Ops.epsilon) {
			return null;
		}

		Point point1 = ray.add(t1);
		Point point2 = ray.add(t2);
		Vec normal1 = point1.sub(this.center).normalize();
		Vec normal2 = point2.sub(this.center).normalize().neg();

		Hit hit1 = new Hit(t1, normal1);
		Hit hit2 = new Hit(t2, normal2);

		return t1 > Ops.epsilon ? hit1 : hit2;
	}
}
