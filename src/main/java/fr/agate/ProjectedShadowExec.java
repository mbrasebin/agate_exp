package fr.agate;

import java.awt.Color;
import java.util.List;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IEnvelope;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableSurface;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.geometrie.Vecteur;
import fr.ign.cogit.geoxygene.convert.FromGeomToSurface;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.analysis.roof.RoofDetection;
import fr.ign.cogit.geoxygene.sig3d.calculation.Util;
import fr.ign.cogit.geoxygene.sig3d.calculation.raycasting.InverseProjection;
import fr.ign.cogit.geoxygene.sig3d.calculation.raycasting.ProjectedShadow;
import fr.ign.cogit.geoxygene.sig3d.calculation.raycasting.RayCasting;
import fr.ign.cogit.geoxygene.sig3d.convert.transform.Extrusion2DObject;
import fr.ign.cogit.geoxygene.sig3d.equation.PlanEquation;
import fr.ign.cogit.geoxygene.sig3d.gui.MainWindow;
import fr.ign.cogit.geoxygene.sig3d.representation.ConstantRepresentation;
import fr.ign.cogit.geoxygene.sig3d.representation.texture.TextureManager;
import fr.ign.cogit.geoxygene.sig3d.representation.texture.TexturedSurface;
import fr.ign.cogit.geoxygene.sig3d.semantic.VectorLayer;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_Polygon;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;

/**
 * Classe permettant de calculer les ombres projetées sur une surface à partir
 * d'un angle
 * 
 * 
 * @author MBrasebin
 * 
 */
public class ProjectedShadowExec {

	public static void main(String[] args) throws Exception {

		// Fichier de bâtiments en entrée
		String shapeFileBuildings = "C:/Users/m.brasebin/EspaceTravail/Donnees/tmp/ombrage/buildings.shp";

		// Fichier de zone
		String shapeFileZone = "C:/Users/m.brasebin/EspaceTravail/Donnees/tmp/ombrage/zone.shp";

		// Shapefile de projet
		String projetShp = "C:/Users/m.brasebin/EspaceTravail/Donnees/tmp/ombrage/project.shp";

		RayCasting.EPSILON = 0.01;
		RayCasting.CHECK_IS_ON_EDGE = true;

		PlanEquation.EPSILON = 0.0000000000001;

		ConstantRepresentation.backGroundColor = new Color(156, 180, 193);

		IFeatureCollection<IFeature> buildings = ShapefileReader.read(shapeFileBuildings);

		IFeatureCollection<IFeature> zone = ShapefileReader.read(shapeFileZone);

		IFeatureCollection<IFeature> proj = ShapefileReader.read(projetShp);

		for (IFeature feat : proj) {

			feat.setGeom(Extrusion2DObject.convertFromGeometry(feat.getGeom(), 0, 12));
		}

		for (IFeature feat : buildings) {

			double hauteur = Double.parseDouble(feat.getAttribute("HAUTEUR").toString());

			if (Double.isNaN(hauteur) || Double.isInfinite(hauteur) || hauteur < 1.0) {
				hauteur = 3.0;
			}

			feat.setGeom(Extrusion2DObject.convertFromGeometry(feat.getGeom(), 0, hauteur));
		}

		MainWindow mW = new MainWindow();


		
		
		IFeatureCollection<IFeature> roofBuilding = RoofDetection.detectRoof(buildings, 0.2, false);
		IFeatureCollection<IFeature> roofProject = RoofDetection.detectRoof(proj, 0.2, false);
		
		
		
		
		IFeatureCollection<IFeature> wallBuilding = new FT_FeatureCollection<IFeature>();
		
		for(IFeature feat :  buildings) {
			IMultiSurface<IOrientableSurface> ims = Util.detectVertical(FromGeomToSurface.convertGeom(feat.getGeom()), 0.2);
		
			if(! (ims == null) && ! ims.isEmpty()) {
				wallBuilding.add(new DefaultFeature(ims));
			}
		}
		
		
		
		IFeatureCollection<IFeature> wallProject = new FT_FeatureCollection<IFeature>();
		
		for(IFeature feat :  proj) {
			IMultiSurface<IOrientableSurface> ims = Util.detectVertical(FromGeomToSurface.convertGeom(feat.getGeom()), 0.2);
		
			if(! (ims == null) && ! ims.isEmpty()) {
				wallProject.add(new DefaultFeature(ims));
			}
		}
		
		

		
		VectorLayer vl = new VectorLayer(roofBuilding, "Toits Batiment", Color.green);
		VectorLayer vl2 = new VectorLayer(roofProject, "Toits Projet", Color.red);
		
		VectorLayer vl3 = new VectorLayer(wallBuilding, "Murs Batiment", Color.LIGHT_GRAY);
		VectorLayer vl4 = new VectorLayer(wallProject, "Murs Projet", Color.white);
		

		
		
		
		IPolygon emprise = (IPolygon) FromGeomToSurface.convertGeom(zone.get(0).getGeom()).get(0);

		if (emprise.isEmpty()) {
			System.out.println("null");
		}

		// Calculation3D.translate(emprise, -Environnement.dpTranslate.getX(),
		// -Environnement.dpTranslate.getY(), 0);

		IFeatureCollection<IFeature> fc = new FT_FeatureCollection<IFeature>();

		IFeature feat = new DefaultFeature(emprise);
		
		
		IEnvelope env = feat.getGeom().getEnvelope();

		fc.add(feat);
		
		
		
		mW.getInterfaceMap3D().getCurrent3DMap().addLayer(vl);
		mW.getInterfaceMap3D().getCurrent3DMap().addLayer(vl2);
		
		
		mW.getInterfaceMap3D().getCurrent3DMap().addLayer(vl3);
		mW.getInterfaceMap3D().getCurrent3DMap().addLayer(vl4);
		//mW.getInterfaceMap3D().getCurrent3DMap().addLayer(vlZone);
		
		
		// if(true) return;

		// feat.setRepresentation(new Object2d(feat, Color.pink));

		feat.setRepresentation(new TexturedSurface(feat, TextureManager.textureLoading("C:/Users/m.brasebin/EspaceTravail/Donnees/tmp/ombrage/background.png"), 
				env.maxX() - env.minX(), env.maxY() - env.minY()));

		mW.getInterfaceMap3D().getCurrent3DMap().addLayer(new VectorLayer(fc, "Cool"));
		
		//Source  : http://www.solartopo.com/orbite-solaire.htm
		//16h16 28/03/2019
		double zenithPrintemps = 24.83;
		double azimuthPrintemps =244.46;

		
	
		//16h16 21/21/2019
		double zenithHiver = 4.79;
		double azimuthHiver = 229.35;

		
		
		long t = System.currentTimeMillis();

	
		System.out.println("Temps écoulé " + (System.currentTimeMillis() - t));

		IFeatureCollection<IFeature> lfeatC2 = new FT_FeatureCollection<IFeature>();
		IFeatureCollection<IFeature> lfeatC = new FT_FeatureCollection<IFeature>();

		// List<IOrientableCurve> lC =
		// CalculNormales.getNormal(env.getBatiments(),
		// 5);
		// lGeom.addAll(lC);
		List<IGeometry> lGeom = generateGeometries(azimuthPrintemps, zenithPrintemps, proj, zone, env);

		List<IGeometry> lGeom2 = generateGeometries(azimuthHiver, zenithHiver, proj, zone, env);

		for (IGeometry geom : lGeom2) {
			lfeatC2.add(new DefaultFeature(geom));
		}


		for (IGeometry geom : lGeom) {
			lfeatC.add(new DefaultFeature(geom));
		}

		
		mW.getInterfaceMap3D().getCurrent3DMap().addLayer(new VectorLayer(lfeatC, "Printemps", Color.pink));

		mW.getInterfaceMap3D().getCurrent3DMap().addLayer(new VectorLayer(lfeatC2, "Hiver", Color.orange));
	}
	
	
	public static List<IGeometry> generateGeometries(double azimuth, double zenith, IFeatureCollection<IFeature> proj, IFeatureCollection<IFeature> zone, IEnvelope env){

		double x = Math.sin(azimuth) * Math.cos(90 - zenith) * 100 ;
		double y = Math.cos(azimuth) * Math.cos(90 - zenith) * 100 ;
		double z =  Math.sin(90 - zenith) * 100 ;
		
		//
		System.out.println("x " + x + "  y  " + y + "  z  "+ z );
		

		List<IGeometry> lGeom2 = ProjectedShadow.process(proj, zone,
				(IPolygon) env.getGeom(), new Vecteur(-x,-y,-z), 0, 15,
				ProjectedShadow.POSSIBLE_RESULT.PROJECTED_VOLUME, true);

		
		return lGeom2;
		
	}

}
