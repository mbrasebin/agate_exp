package fr.agate;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableSurface;
import fr.ign.cogit.geoxygene.convert.FromGeomToSurface;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.convert.transform.Extrusion2DObject;
import fr.ign.cogit.geoxygene.sig3d.semantic.DTM;
import fr.ign.cogit.geoxygene.sig3d.util.ColorShade;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_Polygon;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class PrepareBatiment {

	public static void main(String[] args) {
		String sourceDTM = "C:/Users/m.brasebin/Desktop/bla/";

		DTM mnt = new DTM(sourceDTM + "export.asc", "Export", true, 1, ColorShade.BLUE_CYAN_GREEN_YELLOW_WHITE);

		IFeatureCollection<IFeature> featC = ShapefileReader.read(sourceDTM + "batiment.shp");
		IFeatureCollection<IFeature> featCOut = new FT_FeatureCollection<IFeature>();
		for (IFeature feat : featC) {

			for (IOrientableSurface os : FromGeomToSurface.convertGeom(feat.getGeom())) {
				IDirectPosition dp = mnt.cast(os.centroid());

				if (dp != null) {
					for (IDirectPosition dpIn : os.coord()) {
						dpIn.setZ(0);
					}

					double hauteur = Double.parseDouble(feat.getAttribute("HAUTEUR").toString());
					
					
					System.out.println(hauteur);
					
					IMultiSurface<IOrientableSurface> ios = FromGeomToSurface.convertMSGeom(Extrusion2DObject.convertFromPolygon((GM_Polygon) os, dp.getZ(),
							dp.getZ() + hauteur));

					featCOut.add(new DefaultFeature(ios));

				}
			}

		}

		ShapefileWriter.write(featCOut, "C:/Users/m.brasebin/Desktop/bla/buildingout.shp");

	}

}
