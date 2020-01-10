package fr.agate.grandChyTourisme;

import java.io.File;
import java.util.Collection;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class DecomposeParItineraires {

	public static void main(String[] args) throws NoSuchAuthorityCodeException, FactoryException {

		//fichier contenant les shapefiles d'itinéraire et le référentiel
		String folder = "C:/Users/m.brasebin/Desktop/tmp/gdchy/";
		String pathToReferentiel = folder + "referentiel.shp";
		String pathToItineraire = folder + "itineraires.shp";
		
		//Dossier dans lequel on stock les résultats
		String folderOut = folder + "/out/";

		//Attribut pour l'identifiant et le nom de l'itinéraire
		String itineraireNameAttribute = "ITIN�rAI";
		String idNameAttribute = "NOUVELIDEN";

		//Taille du buffer de recherche
		double bufferSize = 50;

		// On charge le référentiel et les itinéraires
		IFeatureCollection<IFeature> referentiel = ShapefileReader.read(pathToReferentiel);
		IFeatureCollection<IFeature> itineraires = ShapefileReader.read(pathToItineraire);

		// On initialise l'index spatiale
		referentiel.initSpatialIndex(Tiling.class, false);
		
		
		//S'il n'y a pas d'attibut d'identifiant, ça commence par 100
		int countNoIDName = 100;

		//Pour chaque itinéraire
		for (IFeature itineraire : itineraires) {

			////////////////
			// Création des sous dossiers dans folderOut au besoin
			///////////////
			
			//Le nom commence par l'identifiant d'après l'attribut identifiant
			Object attributIDObj = itineraire.getAttribute(idNameAttribute);
			
			String idName = "";
			//S'il n'y a pas d'attribut identifiant
			if (attributIDObj == null) {
				//On le génère à partir de 100
				idName = countNoIDName + "-";
				countNoIDName++;

			} else {
				//Sinon on utilise l'attribut identifiant converti en entier
				idName = ((int) Double.parseDouble(attributIDObj.toString())) +"-";
			}

	
			// Le nom est ensuite composé de l'attribut nom de l'itinéraire
			String newFolderName = itineraire.getAttribute(itineraireNameAttribute).toString();
			//On supprime les espaces			
			newFolderName = newFolderName.replace(" ", "");
			//On met en minuscule
			newFolderName = newFolderName.toLowerCase();
			//On retire les caractères spéciaux
			newFolderName = newFolderName.replaceAll("[^a-zA-Z0-9]", "");
			
			//On créer un nouveau dossier s'il n'existe pas
			File newFolder = new File(folderOut + "/" + idName + newFolderName + "/");
			if (!newFolder.exists()) {
				newFolder.mkdirs();
			}


	

			////////////////
			// Sélection des bonnes entités du référentiel
			////////////////
			
			//Un buffer autour de l'itinéraire permet de récupérer le référenciel
			Collection<IFeature> featColl = referentiel.select(itineraire.getGeom().buffer(bufferSize));

			//On met dans une liste pour exporter en shapefile
			IFeatureCollection<IFeature> featCollOut = new FT_FeatureCollection<IFeature>(featColl);

			//On exporte en shapefile
			ShapefileWriter.write(featCollOut, newFolder.getAbsolutePath() + "/" + idName + newFolderName + ".shp", CRS.decode("EPSG:2154"));

		}

	}

}
