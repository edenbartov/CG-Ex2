package edu.cg.scene;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.cg.Logger;
import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Ops;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;
import edu.cg.scene.camera.PinholeCamera;
import edu.cg.scene.lightSources.Light;
import edu.cg.scene.objects.Surface;

public class Scene {
	private String name = "scene";
	private int maxRecursionLevel = 1;
	private int antiAliasingFactor = 1; //gets the values of 1, 2 and 3
	private boolean renderRefractions = false;
	private boolean renderReflections = false;

	private PinholeCamera camera;
	private Vec ambient = new Vec(0.1, 0.1, 0.1); //white
	private Vec backgroundColor = new Vec(0, 0.5, 1); //blue sky
	private List<Light> lightSources = new LinkedList<>();
	private List<Surface> surfaces = new LinkedList<>();


	//MARK: initializers
	public Scene initCamera(Point eyePoistion, Vec towardsVec, Vec upVec,  double distanceToPlain) {
		this.camera = new PinholeCamera(eyePoistion, towardsVec, upVec,  distanceToPlain);
		return this;
	}

	public Scene initCamera(PinholeCamera pinholeCamera) {
		this.camera = pinholeCamera;
		return this;
	}

	public Scene initAmbient(Vec ambient) {
		this.ambient = ambient;
		return this;
	}

	public Scene initBackgroundColor(Vec backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public Scene addLightSource(Light lightSource) {
		lightSources.add(lightSource);
		return this;
	}

	public Scene addSurface(Surface surface) {
		surfaces.add(surface);
		return this;
	}

	public Scene initMaxRecursionLevel(int maxRecursionLevel) {
		this.maxRecursionLevel = maxRecursionLevel;
		return this;
	}

	public Scene initAntiAliasingFactor(int antiAliasingFactor) {
		this.antiAliasingFactor = antiAliasingFactor;
		return this;
	}

	public Scene initName(String name) {
		this.name = name;
		return this;
	}

	public Scene initRenderRefractions(boolean renderRefractions) {
		this.renderRefractions = renderRefractions;
		return this;
	}

	public Scene initRenderReflections(boolean renderReflections) {
		this.renderReflections = renderReflections;
		return this;
	}

	//MARK: getters
	public String getName() {
		return name;
	}

	public int getFactor() {
		return antiAliasingFactor;
	}

	public int getMaxRecursionLevel() {
		return maxRecursionLevel;
	}

	public boolean getRenderRefractions() {
		return renderRefractions;
	}

	public boolean getRenderReflections() {
		return renderReflections;
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Camera: " + camera + endl +
				"Ambient: " + ambient + endl +
				"Background Color: " + backgroundColor + endl +
				"Max recursion level: " + maxRecursionLevel + endl +
				"Anti aliasing factor: " + antiAliasingFactor + endl +
				"Light sources:" + endl + lightSources + endl +
				"Surfaces:" + endl + surfaces;
	}

	private transient ExecutorService executor = null;
	private transient Logger logger = null;

	// TODO: add your fields here with the transient keyword
	//  for example - private transient Object myField = null;

	private void initSomeFields(int imgWidth, int imgHeight, double planeWidth, Logger logger) {
		this.logger = logger;
		// TODO: initialize your fields that you added to this class here.
		//      Make sure your fields are declared with the transient keyword
	}


	public BufferedImage render(int imgWidth, int imgHeight, double planeWidth ,Logger logger)
			throws InterruptedException, ExecutionException, IllegalArgumentException {

		initSomeFields(imgWidth, imgHeight, planeWidth, logger);

		BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
		camera.initResolution(imgHeight, imgWidth, planeWidth);
		int nThreads = Runtime.getRuntime().availableProcessors();
		nThreads = nThreads < 2 ? 2 : nThreads;
		this.logger.log("Initialize executor. Using " + nThreads + " threads to render " + name);
		executor = Executors.newFixedThreadPool(nThreads);

		@SuppressWarnings("unchecked")
		Future<Color>[][] futures = (Future<Color>[][])(new Future[imgHeight][imgWidth]);

		this.logger.log("Starting to shoot " +
			(imgHeight*imgWidth*antiAliasingFactor*antiAliasingFactor) +
			" rays over " + name);

		for(int y = 0; y < imgHeight; ++y)
			for(int x = 0; x < imgWidth; ++x)
				futures[y][x] = calcColor(x, y);

		this.logger.log("Done shooting rays.");
		this.logger.log("Wating for results...");

		for(int y = 0; y < imgHeight; ++y)
			for(int x = 0; x < imgWidth; ++x) {
				Color color = futures[y][x].get();
				img.setRGB(x, y, color.getRGB());
			}

		executor.shutdown();

		this.logger.log("Ray tracing of " + name + " has been completed.");

		executor = null;
		this.logger = null;

		return img;
	}

	private Future<Color> calcColor(int x, int y) {
		return executor.submit(() -> {
			Point pointOnScreen = camera.transform(x, y);
			Vec color = new Vec(0.0);

			Ray ray = new Ray(camera.getCameraPosition(), pointOnScreen);
			color = color.add(calcColor(ray, 0));

			return color.toColor();
			// TODO: change this method for AntiAliasing bonus
			//		You need to shoot antiAliasingFactor-1 additional rays through the pixel return the average color of
			//      all rays.
		});
	}

	private Vec calcColor(Ray ray, int recursionLevel) {
		// TODO: implement this method to support ray tracing
		// 		This is the first call to ray ray-tracing
		// TODO: add reflection
		// calculate the nearest hit of the ray
		Vec color = new Vec();
		if (maxRecursionLevel <= recursionLevel) {
			return color;
		}

		Hit minHit = getFirstHit(ray);
		if (minHit == null) {
			return backgroundColor;
		}

		Point hitPoint = ray.getHittingPoint(minHit);
		Surface hitSurface = minHit.getSurface();
		color = hitSurface.Ka();
		color = color.mult(ambient); // KA* IA

		for (Light light : lightSources) {
			Ray rayToLight = light.rayToLight(hitPoint);
			if (!isOccluded(rayToLight, light)) {
				color = color.add(calcPhongModel(minHit, hitPoint, light, ray, rayToLight));
			}
		}

		if (renderReflections && hitSurface.isReflecting()) {
			Vec reflectedVec = Ops.reflect(ray.direction(), minHit.getNormalToSurface());
			Ray reflectedRay = new Ray (hitPoint, reflectedVec);
			Vec Kr = new Vec(hitSurface.Kr());
			Vec newColor = calcColor(reflectedRay, recursionLevel + 1);
			newColor = newColor.mult(Kr);
			color = color.add(newColor);
		}
		return color;

	}

	private Hit getFirstHit(Ray ray) {
		Hit minHit = null;
		for (Surface surface : surfaces) {
			Hit tempHit = surface.intersect(ray);
			if (tempHit != null) {
				if (minHit == null){
					minHit = tempHit;
				} else {
					minHit = tempHit.compareTo(minHit) < 0 ? tempHit : minHit;
				}
			}
		}
		return minHit;
	}

	private boolean isOccluded(Ray rayToLight, Light light){
		for (Surface surface : surfaces) {
			if (light.isOccludedBy(surface, rayToLight)) {
				return true;
			}
		}
		return false;
	}

	private Vec calcDiffuseColor(Hit hit, Ray rayToLight, Vec intensity) {
		Vec normal = hit.getNormalToSurface();
		Vec L = rayToLight.direction();
		Vec Kd = hit.getSurface().Kd();
		double cosTheta = Math.max(normal.dot(L), 0);
		return Kd.mult(intensity).mult(cosTheta);
	}

	private Vec calcSpecularColor(Hit hit, Ray originalRay, Ray rayToLight, Vec intensity){;
		Vec normal = hit.getNormalToSurface();
		Vec L = Ops.reflect(rayToLight.direction().neg(), normal);
		Vec V = originalRay.direction();
		Vec Ks = hit.getSurface().Ks();
		int shininess = hit.getSurface().shininess();
		double cosAlpha = Math.max(L.dot(V.neg()), 0);
		return Ks.mult(intensity).mult(Math.pow(cosAlpha, shininess));
	}

	private Vec calcPhongModel(Hit hit, Point hitPoint, Light light, Ray originalRay, Ray rayToLight) {
		Vec intensity = light.intensity(hitPoint, rayToLight); // IL
		Vec diffuseColor = calcDiffuseColor(hit, rayToLight, intensity);
		Vec specularColor = calcSpecularColor(hit, originalRay, rayToLight, intensity);
		return diffuseColor.add(specularColor);
	}
}
