package fr.agate;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableCurve;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.convert.FromGeomToLineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class CarteTopoExec {

	public static void main(String[] args) {

		String path = "C:/Users/m.brasebin/Desktop/tmp/routes.shp";
		IFeatureCollection<IFeature> collection = ShapefileReader.read(path);

		List<ILineString> lls = new ArrayList<>();

		for (IFeature feat : collection) {

			for (IOrientableCurve oc : FromGeomToLineString.convert(feat.getGeom())) {
				lls.addAll(convertPolToLineStrings((ILineString) oc));
			}

		}

		CarteTopo cT = newCarteTopo("test", lls, 0);

		System.out.println("Nombre de faces : " + cT.getPopFaces().size());

		for (Arc a : cT.getPopArcs()) {

			AttributeManager.addAttribute(a, "FD", a.getFaceDroite(), "String");
			AttributeManager.addAttribute(a, "FG", a.getFaceGauche(), "String");
		}

		ShapefileWriter.write(cT.getPopFaces(), "C:/Users/m.brasebin/Desktop/tmp/face.shp");
		ShapefileWriter.write(cT.getPopArcs(), "C:/Users/m.brasebin/Desktop/tmp/arcs.shp");
	}

	public static List<ILineString> convertPolToLineStrings(ILineString pol) {
		List<ILineString> lLS = new ArrayList<>();

		IDirectPositionList dpl = pol.coord();

		for (int i = 0; i < dpl.size() - 1; i++) {

			IDirectPositionList dplTemp = new DirectPositionList();
			dplTemp.add(dpl.get(i));
			dplTemp.add(dpl.get(i + 1));

			lLS.add(new GM_LineString(dplTemp));

		}

		return lLS;

	}

	public static CarteTopo newCarteTopo(String name, List<ILineString> lLLS, double threshold) {

		try {
			// Initialisation d'une nouvelle CarteTopo
			CarteTopo carteTopo = new CarteTopo(name);
			carteTopo.setBuildInfiniteFace(false);
			// Récupération des arcs de la carteTopo
			IPopulation<Arc> arcs = carteTopo.getPopArcs();
			// Import des arcs de la collection dans la carteTopo

			for (ILineString ls : lLLS) {
				// création d'un nouvel élément
				Arc arc = arcs.nouvelElement();
				// affectation de la géométrie de l'objet issu de la collection
				// à l'arc de la carteTopo
				arc.setGeometrie(ls);
				// instanciation de la relation entre l'arc créé et l'objet
				// issu de la collection
				// arc.addCorrespondant(feature);

			}

			carteTopo.creeTopologieArcsNoeuds(threshold);

			carteTopo.creeNoeudsManquants(0);

			carteTopo.fusionNoeuds(0);

			carteTopo.decoupeArcs(0);
			carteTopo.splitEdgesWithPoints(0);

			carteTopo.filtreArcsDoublons();

			// Création de la topologie Arcs Noeuds

			carteTopo.creeTopologieArcsNoeuds(threshold);
			// La carteTopo est rendue planaire

			carteTopo.rendPlanaire(threshold);

			carteTopo.filtreArcsDoublons();

			// DEBUG2.addAll(carteTopo.getListeArcs());

			// carteTopo.creeTopologieFaces();
			carteTopo.creeTopologieArcsNoeuds(threshold);

			// Création des faces de la carteTopo
			carteTopo.creeTopologieFaces();

			return carteTopo;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
