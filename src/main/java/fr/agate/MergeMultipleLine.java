package fr.agate;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.convert.FromGeomToLineString;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiCurve;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class MergeMultipleLine {

	public static void main(String[] args) {

		String fileIn = "C:\\Users\\m.brasebin\\Desktop\\rama\\couche-regroupee.shp";
		String fileOut = "C:/Users/m.brasebin/Desktop/rama/couche-out.shp";
		IFeatureCollection<IFeature> featC = ShapefileReader.read(fileIn);

		IMultiCurve<ILineString> imc = new GM_MultiCurve<ILineString>();

		for (IFeature feat : featC) {
			imc.addAll(FromGeomToLineString.convertLineString(feat.getGeom()));
		}
		
		System.out.println(imc);

		ILineString ls = Operateurs.union(imc.getList());

		IFeature feat = new DefaultFeature(ls);
		AttributeManager.addAttribute(feat, "id", 1, "Integer");
		IFeatureCollection<IFeature> featColl = new FT_FeatureCollection<IFeature>();
		featColl.add(feat);
		ShapefileWriter.write(featColl, fileOut);

	}

}
