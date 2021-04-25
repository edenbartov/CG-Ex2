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
		double b = ray.direction().mult(2).dot(sourceToCenter);
		double c = sourceToCenter.dot(sourceToCenter) - (this.radius * this.radius);

		double denominator = 2 * a;
		try {
			double discriminant = Math.sqrt((b * b) - 4 * a * c);
			double t;
			if (discriminant < Ops.epsilon) {
				t = -b / denominator;
				if (t < Ops.epsilon || t > Ops.infinity) {
					return null;
				}
				return new Hit(t, ray.add(t).sub(this.center).normalize());
			}

			double t1 = (-b + discriminant) / denominator;
			double t2 = (-b - discriminant) / denominator;
			if (t1 < Ops.epsilon && t2 < Ops.epsilon) {
				return null;
			}

			Point point1 = ray.add(t1);
			Plain plain1 = new Plain(point1.sub(this.center).normalize(), point1);

			Point point2 = ray.add(t2);
			Plain plain2 = new Plain(point2.sub(this.center).normalize(), point2);

			Hit hit1 = new Hit(t1, plain1.normal());
			Hit hit2 = new Hit(t2, plain2.normal());
			return hit1.compareTo(hit1) < 0 ? hit2 : hit1;

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
			results[index] = numerator / denominator;
			if (results[index] > Ops.infinity) {
				return null;
			}
			index++;
		}
		return results;
	}
}
