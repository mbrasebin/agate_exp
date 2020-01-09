package fr.agate;

import java.util.Collection;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class Difference {
	
	
	public static void main(String[] args) {
		 String shapeIn = "C:/Users/m.brasebin/Desktop/difference/SCoT_DUL_2015_zones_U.shp";
		 
		 String eu = "C:/Users/m.brasebin/Desktop/difference/eu_2014.shp";
		 
		 
		 IFeatureCollection<IFeature> featCollShapeIn = ShapefileReader.read(shapeIn);
		 
		 IFeatureCollection<IFeature> featCollShapeEU = ShapefileReader.read(eu);
		 featCollShapeEU.initSpatialIndex(Tiling.class, false);
		 
		 
		 IFeatureCollection<IFeature> results = new FT_FeatureCollection<IFeature>();

		 
		 for(IFeature feat: featCollShapeIn)
		 {
			 
			 IGeometry geom = validateGeom(feat.getGeom());
	
			 
			 if(! geom.isValid()) {
				 System.out.println("Not valid");
				 System.out.println(geom);
			 }
				 
			 Collection<IFeature> coll = featCollShapeEU.select(feat.getGeom());
			 
			
			 
			 for(IFeature featInColl :coll) {
				 
				 IGeometry geom2 = validateGeom(featInColl.getGeom());
				 if(geom2 == null) {
					 continue;
				 }
				 
				 geom = geom.difference(geom2);
				 
				 if(geom == null || geom.isEmpty() || geom.area() < 0.001) {
					 break;
				 }
				 
				 geom = validateGeom(geom);
				 
			 }
			 
			 
			 if(geom != null) {
				 feat.setGeom(geom);
				 results.add(feat);
			 }
		 }
		 
		 ShapefileWriter.write(results, "C:/Users/m.brasebin/Desktop/difference/result/out_U_2014.shp");
	}
	
	
	public static IGeometry validateGeom(IGeometry geom) {
		
		
		if(geom ==null || geom.isEmpty()) {
			return null;
		}
		 
		 
		 if(! geom.isValid()) {
			// System.out.println("Not valid");
			 geom = geom.buffer(0.0);
		 }
		 
		 
		 if(! geom.isValid()) {
			 // System.out.println("Not valid");
			 geom = geom.buffer(0.001);
		 }
		 
		return geom;
	}

}
