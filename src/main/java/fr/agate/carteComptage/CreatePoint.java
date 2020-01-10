package fr.agate.carteComptage;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.contrib.geometrie.Vecteur;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.calculation.OrientedBoundingBox;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class CreatePoint {

	public static void main(String[] args) throws CloneNotSupportedException {

		int shift = 350;

		// On lit les entités du shapefile
		IFeatureCollection<IFeature> featColl = ShapefileReader
				.read("C:\\Users\\m.brasebin\\Desktop\\tmp\\llineaire_test.shp");

		// FeatCollOut : l'endroit où l'on stocke les résultats en sortie
		IFeatureCollection<IFeature> featCollOutMiddle = new FT_FeatureCollection<IFeature>();

		// Les points orientés vers le haut en sortie
		IFeatureCollection<IFeature> featCollOutTop = new FT_FeatureCollection<IFeature>();

		// Les points vers le bas en sortie
		IFeatureCollection<IFeature> featCollOutBot = new FT_FeatureCollection<IFeature>();

		IFeatureCollection<IFeature> featCollObb = new FT_FeatureCollection<IFeature>();

		// Pour chaque entité
		for (IFeature feat : featColl) {
			// On récupère la géométrie
			IGeometry geom = feat.getGeom();
			// On s'assure que c'est bien une multicurve
			if (!(geom instanceof IMultiCurve<?>)) {
				// Si ce n'est pas le cas on affiche un message et on ne traite pas l'entité
				System.out.println("Géométrie non reconnue : " + geom.getClass());
				continue;
			}

		


			IMultiCurve<ILineString> curve = (IMultiCurve<ILineString>) geom;


			// On fusionne en une seule ligne
			List<ILineString> lsTemp = new ArrayList<>();
			lsTemp.addAll(curve.getList());
			ILineString ls = Operateurs.union(lsTemp);

			// On calcule le milieu
			IDirectPosition dp = Operateurs.milieu(ls);

			// --------------Traiter le point au milieu

			// On met un point comme géométrie dans une c
			IFeature featOutMiddle = feat.cloneGeom();
			featOutMiddle.setGeom(new GM_Point(dp));

			// On ajoute l'entité en sortie
			featCollOutMiddle.add(featOutMiddle);

			IGeometry geomTemp = geom.intersection((new GM_Point(dp)).buffer(1000));

			if (geomTemp == null || geomTemp.isEmpty()) {
				geomTemp = geom;

			}

			OrientedBoundingBox oBB = new OrientedBoundingBox(geomTemp);
			
			if(oBB.getPoly() == null) {
				oBB = new OrientedBoundingBox(geom);
			}

			featCollObb.add(new DefaultFeature(oBB.getPoly()));

			// On récupère la valeur de l'azimuth
			// double azimuth =
			// Double.parseDouble(feat.getAttribute(nomAttributAzimuth).toString());

			// --------------Traiter le point du haut
			IFeature featOutTop = feat.cloneGeom();
			Vecteur v = getVecteur(oBB);
			if(v.getX() < 0 ) {
				v= v.multConstante(-1);
			}
			
			double offsetXTop = -shift * v.getY();
			double offsetYTop = shift * v.getX();
			IDirectPosition dpTop = (IDirectPosition) dp.clone();
			dpTop.move(offsetXTop, offsetYTop);
			featOutTop.setGeom(new GM_Point(dpTop));
			featCollOutTop.add(featOutTop);

			// --------------Traiter le point du bas
			IFeature featOutBottom = feat.cloneGeom();
			double offsetXBottom = -offsetXTop;
			double offsetYBottom = -offsetYTop;
			IDirectPosition dpBot = (IDirectPosition) dp.clone();
			dpBot.move(offsetXBottom, offsetYBottom);
			featOutBottom.setGeom(new GM_Point(dpBot));
			featCollOutBot.add(featOutBottom);

		}

		// On exporte le résultat
		ShapefileWriter.write(featCollOutMiddle, "C:/Users/m.brasebin/Desktop/tmp/point_out_mid.shp");
		ShapefileWriter.write(featCollOutBot, "C:/Users/m.brasebin/Desktop/tmp/point_out_bot.shp");
		ShapefileWriter.write(featCollOutTop, "C:/Users/m.brasebin/Desktop/tmp/point_out_top.shp");

		ShapefileWriter.write(featCollObb, "C:/Users/m.brasebin/Desktop/tmp/enveloppe.shp");

	}

	public static Vecteur getVecteur(OrientedBoundingBox oBB) {

		IPolygon poly = oBB.getPoly();

		IDirectPosition dp1 = poly.coord().get(0);
		IDirectPosition dp2 = poly.coord().get(1);
		IDirectPosition dp3 = poly.coord().get(3);

		if (dp1.distance2D(dp2) < dp1.distance2D(dp3)) {

			dp2 = dp3;
		}

		Vecteur v = new Vecteur(dp1, dp2);
		v.normalise();

		return v;

	}

}
