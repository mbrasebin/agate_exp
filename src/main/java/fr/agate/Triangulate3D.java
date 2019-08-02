package fr.agate;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ITriangle;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableSurface;
import fr.ign.cogit.geoxygene.convert.FromGeomToSurface;
import fr.ign.cogit.geoxygene.sig3d.convert.geom.FromPolygonToTriangle;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiSurface;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class Triangulate3D {
	public static void main(String[] args) {
		String fileIn = "Q:\\Production\\Communes\\Communes de Savoie\\SAINT FRANCOIS  LONGCHAMP\\2018 VIGIE\\SIG\\Projet\\simul\\result.shp";
		String fileOut = "C:/tmp/result.shp";
		
		IFeatureCollection<IFeature> featIn = ShapefileReader.read(fileIn);
		
		for(IFeature feat : featIn) {
			IMultiSurface<IOrientableSurface> iosOut = new GM_MultiSurface<IOrientableSurface>();
			for(IOrientableSurface os : FromGeomToSurface.convertGeom(feat.getGeom())) {
				
				for(ITriangle tri : FromPolygonToTriangle.convertAndTriangle(os)){
					tri.coord().remove(tri.coord().size()-1);
					iosOut.add(tri);
				}
				
				
			}
			
			feat.setGeom(iosOut);
			
			System.out.println(featIn.get(0).getGeom());
		}
		
		ShapefileWriter.write(featIn, fileOut);
	}
}
