package fr.agate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IEnvelope;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

/**
 * Script to generate point on a regular grid inside the geometry envelope
 * 
 * 
 * @author m.brasebin
 *
 */
public class GenerateRegularGrid {

	public static void main(String[] args) {

		// Couche contenant 1 shape dans lequel la grille est générée
		String shapeIn = "C:\\Users\\m.brasebin\\EspaceTravail\\Donnees\\Departement\\Savoie.shp";

		// Fichier dans lequel la grille de point est exporté
		String exportPoint = "C:/Users/m.brasebin/EspaceTravail/Donnees/tmp/exportPoint/export.shp";

		// Pas en m
		long pas = 50;

		// We get the entity
		IFeature feat = ShapefileReader.read(shapeIn).get(0);

		IEnvelope env = feat.getGeom().envelope();

		long xMin = Math.round(env.getLowerCorner().getX());
		long xMax = Math.round(env.getUpperCorner().getX());

		long yMin = Math.round(env.getLowerCorner().getY());
		long yMax = Math.round(env.getUpperCorner().getY());
		
		
		System.out.println("Xmin : " + xMin + "  xMax " + xMax + "  yMin  " + yMin + "   yMax " + yMax);
		
		if(true)return;
		
		System.out.println("Preparing stream");

		Stream<Long> str = LongStream.iterate(xMin, i -> i + pas).limit(1 + (xMax - xMin)/pas ).boxed().parallel();

		System.out.println("Computin lines");


		List<IDirectPosition> listResult = str.map(x -> getListForALine(x, yMin, yMax, pas, feat.getGeom()))
				.flatMap(List::stream).collect(Collectors.toList());

		IFeatureCollection<IFeature> collectOut = new FT_FeatureCollection<IFeature>();
		
		
		System.out.println("Collecting points");

		collectOut.getElements().addAll(
				listResult.parallelStream().map(x -> new DefaultFeature(new GM_Point(x))).collect(Collectors.toList()));

		System.out.println("Exporting shape");

		
		ShapefileWriter.write(collectOut, exportPoint);
	}

	public static List<IDirectPosition> getListForALine(long x, long yMin, long yMax, long pas, IGeometry geom) {
		List<IDirectPosition> list = new ArrayList<>();

		for (long i = yMin; i < yMax; i = i + pas) {
			IDirectPosition dp = new DirectPosition(x, i);
			if (geom.intersects(new GM_Point(dp))) {
				list.add(dp);
			}
		}
		
		
		System.out.println("Line finished : " + x);

		return list;
	}

}