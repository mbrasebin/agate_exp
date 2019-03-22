package fr.agate;

import java.util.List;
import java.util.stream.Collectors;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class Intersection {


	public static void main(String[] args) {

		// Fichier dans lequel la grille de point est exporté
		String exportPoint = "C:/Users/m.brasebin/EspaceTravail/Donnees/tmp/exportPoint/export.shp";
		

		//Shape v
		String shapeIn =  "C:/Users/m.brasebin/EspaceTravail/Donnees/tmp/exportPoint/distance_sup_5000.shp";

		
		//Shape v
		String shapeOut =  "C:/Users/m.brasebin/EspaceTravail/Donnees/tmp/exportPoint/selected_points.shp";
		
		IFeatureCollection<IFeature> featShapeIn = ShapefileReader.read(shapeIn);
		featShapeIn.initSpatialIndex(Tiling.class, false);
		
		
		
		IFeatureCollection<IFeature> featShapePoint = ShapefileReader.read(exportPoint);
		
		
		
		List<IFeature> featList = featShapePoint.getElements().parallelStream().filter(x -> intersects(x, featShapePoint)).collect(Collectors.toList());
		
		
		IFeatureCollection<IFeature> featCollOut = new FT_FeatureCollection<IFeature>();
		featCollOut.addAll(featList);
		ShapefileWriter.write(featCollOut, shapeOut);
	}
	
	
	public static boolean intersects(IFeature feat, IFeatureCollection<IFeature> featShapePoint) {
		
		
	
		
						return 	! (featShapePoint.select(feat.getGeom()).isEmpty());	
		
		
	}
	
	
}
