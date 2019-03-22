package fr.agate;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableCurve;
import fr.ign.cogit.geoxygene.convert.FromGeomToLineString;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiCurve;
import fr.ign.cogit.geoxygene.util.algo.CommonAlgorithms;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class DistanceRoute {

	
	public static void main(String[] args) {
		String shapePointIn = "C:/Users/m.brasebin/EspaceTravail/Donnees/tmp/zone2/point_fin.shp";
		
		String shapeRoadNear = "C:/Users/m.brasebin/EspaceTravail/Donnees/tmp/zone2/routes_proches.shp";
		
		
		String shapePointOut = "C:/Users/m.brasebin/EspaceTravail/Donnees/tmp/zone2/point_fin_out.shp";
		
		
		
		
		IFeatureCollection<IFeature> points = ShapefileReader.read(shapePointIn);
		
		IFeatureCollection<IFeature> roads = ShapefileReader.read(shapeRoadNear);
		
		IMultiCurve<IOrientableCurve> ims = new GM_MultiCurve<IOrientableCurve>();
		
		for(IFeature feat : roads) {
			ims.addAll(FromGeomToLineString.convert(feat.getGeom()));
		}
	
	
	
		
		for(IFeature point : points)
		{
			IDirectPosition dp  = 	CommonAlgorithms.getNearestPoint(ims, point.getGeom());
			
			
			double distance = point.getGeom().coord().get(0).distance2D(dp);
			
			AttributeManager.addAttribute(point, "Distance", distance, "Double");
			AttributeManager.addAttribute(point, "X", dp.getX(), "Double");
			AttributeManager.addAttribute(point, "Y",  dp.getY(), "Double");
			
		}
		
		
		ShapefileWriter.write(points, shapePointOut);
		
		
	}
}
