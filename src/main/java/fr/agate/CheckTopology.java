package fr.agate;

import java.io.File;
import java.util.Collection;
import java.util.List;



import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.io.vector.PostgisManager;
import fr.ign.cogit.geoxygene.util.index.Tiling;
import fr.ign.cogit.simplu3d.util.SimpluParametersJSON;

public class CheckTopology {
	public static void main(String[] args) throws Exception {
		String file = "C:/Users/m.brasebin/EspaceTravail/connexion_donnees.json";
		SimpluParametersJSON paramters = new SimpluParametersJSON(new File(file));
		
		String user = paramters.getString("user");
		String host =  paramters.getString("host");
		String port =  paramters.getString("port");
		String password =  paramters.getString("pw");
		String database =  paramters.getString("database");
		
		System.out.println("---------Connexion---------");
		System.out.println("host : " + host);
		System.out.println("user : " + user);
		System.out.println("password : " + password);
		System.out.println("database : " + database);
		System.out.println("---------------------------");
		
		List<String> tables = PostgisManager.tableWithGeom(host, port, database, "zaef", user, password);
		
		for(String s:tables) {
			System.out.println(s);
		}
		
		
		IFeatureCollection<IFeature> featCollUrba = PostgisManager.loadGeometricTable(host, port, database,"env_urb", "eu2014", user, password);
		
		IFeatureCollection<IFeature> featCollZAEF = PostgisManager.loadGeometricTable(host, port, database,"zaef", "zaef_cc_46", user, password);
		
		
		featCollUrba.initSpatialIndex(Tiling.class,false);
		featCollZAEF.initSpatialIndex(Tiling.class,false);
		
		IFeatureCollection<IFeature> featCollOut = new FT_FeatureCollection<IFeature>();
		
		for(IFeature feat : featCollZAEF) {
			Collection<IFeature> collSelect = featCollUrba.select(feat.getGeom());
			
			for(IFeature featSel : collSelect) {
					IGeometry geom = feat.getGeom().intersection(featSel.getGeom());
					if(geom != null && !geom.isEmpty()) {
						featCollOut.add(new DefaultFeature(geom));
					}
			}
			
			
		}
		
		System.out.println(featCollOut.getElements().parallelStream().mapToDouble(x -> x.getGeom().area()).sum());
		
	}
}
