package edu.cg.scene.objects;

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
		double a = ray.direction().dot(ray.direction());
		double b = 2 * ray.direction().dot(sourceToCenter);
		double c = sourceToCenter.dot(sourceToCenter) - (this.radius * this.radius);

		double[] ts;
		try {
			ts = quadratic(a, b, c);
			if (ts[0] < Ops.epsilon && ts[1] < Ops.epsilon) {
				return null;
			}

			Plain[] hitPlains = new Plain[2];
			for (int i = 0; i < 2; i++) {
				Point point = ray.add(ts[i]);
				Plain plain = new Plain(point.sub(this.center), point);
				hitPlains[i] = plain;
			}
			Hit hit0 = new Hit(ts[0], hitPlains[0].normal());
			Hit hit1 = new Hit(ts[1], hitPlains[1].normal());
			return hit0.compareTo(hit1) < 0 ? hit0 : hit1;

		} catch (ArithmeticException e) {
			return null;
		}
	}

	private double[] quadratic (double a, double b, double c) throws ArithmeticException {
		double[] results = new double[2];
		double denominator = 2 * a;
		double discriminant = Math.sqrt((b * b) - 4 * a * c);
		discriminant = discriminant < Ops.epsilon ? 0 : discriminant;

		int index = 0;
		for (double factor : new double[]{1, -1}) {
			double numerator = -b + factor * discriminant;
			results[index++] = numerator / denominator;
		}
		return results;
	}
}
