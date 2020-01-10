package fr.agate.grandChyTourisme;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;

public class AnalyseTroncons {

	private static String attNature = "NATURE";
	private static String attClasse = "Classe";
	private static String attGR = "GR";

	public static void main(String[] args) throws IOException {

		// Le chemin vers le référentiel
		String folder = "C:/Users/m.brasebin/Desktop/tmp/gdchy/";
		String pathToReferentiel = folder + "referentiel.shp";

		// Le chemin vers le dossier contenant les 75 sous-dossiers
		String pathToOutFolder = folder + "/out/";
		//Chemine d'écriture du CSV
		String pathToCSV = folder + "stats.csv";

		// On calcule les statistiques sur l'ensemble du fichier référentiel
		IFeatureCollection<IFeature> referentiel = ShapefileReader.read(pathToReferentiel);

		// On intialise la liste de classes
		List<String> listClasses = initListClasses(referentiel);

		// On initialise la liste de nature
		List<String> listNatures = initListNatures(referentiel);

		// On garde les attributs nom, nature, GR les classes d'occupation des sols,
		// longueur totale
		String headline = "nom;total;GR;Non-GR";

		for (String classe : listClasses) {
			headline = headline + ";" + classe;
		}

		for (String nature : listNatures) {
			headline = headline + ";" + nature;
		}
		
		String total = generateContent(referentiel,"Total", listClasses, listNatures);
		
		
		//On écrit le début du fichier :
		BufferedWriter in = new BufferedWriter(new FileWriter(pathToCSV));
		in.write(headline + "\n");
		in.write(total + "\n");

		
		File f = new File(pathToOutFolder);
		File[] folders = f.listFiles();

		for (File currentFolder : folders) {
			String folderName = currentFolder.getName();

			File finside = new File(currentFolder, folderName + ".shp");

			// On vérifie que tous les fichiers existent bien
			if (!finside.exists()) {
				System.out.println(finside);
			}
			IFeatureCollection<IFeature> featColl = ShapefileReader.read(finside.getAbsolutePath());
			String currentLine = generateContent(featColl,folderName,listClasses, listNatures);
			in.write(currentLine + "\n");
		}

		in.flush();
		in.close();
	}

	private static String generateContent(IFeatureCollection<IFeature> sentiers, String name, List<String> classes,
			List<String> natures) {
		StringBuilder sB = new StringBuilder();
		//On ajoute le nom
		sB.append(name+";");
		
		//On ajoute la longueur totale
		Double totalLenght = sentiers.getElements().parallelStream().mapToDouble(feat -> feat.getGeom().length()).sum();
		sB.append(totalLenght+";");
		
		//On ajoute les longueurs de GR
		Double grLength = sentiers.getElements().parallelStream().filter(feat -> (! feat.getAttribute(attGR).toString().isEmpty())).mapToDouble(feat -> feat.getGeom().length()).sum();
		sB.append(grLength+";");

		//On ajoute les longueurs hors GR
		Double nogrLength = sentiers.getElements().parallelStream().filter(feat -> (feat.getAttribute(attGR).toString().isEmpty())).mapToDouble(feat -> feat.getGeom().length()).sum();
		sB.append(nogrLength+";");

		//On ajoute la liste des classes d'occupation des sols
		for(String classe: classes) {
			Double currentClassLenght = sentiers.getElements().parallelStream().filter(feat -> (feat.getAttribute(attClasse)).toString().equalsIgnoreCase(classe)).mapToDouble(feat -> feat.getGeom().length()).sum();
			sB.append(currentClassLenght+";");
		}
		
		
		//On ajoute la liste des sentiers
		for(String nature: natures) {
			Double currentNatureLenght = sentiers.getElements().parallelStream().filter(feat -> (feat.getAttribute(attNature)).toString().equalsIgnoreCase(nature)).mapToDouble(feat -> feat.getGeom().length()).sum();
			sB.append(currentNatureLenght+";");
		}


		
		sentiers.getElements().parallelStream().filter(feat -> feat.getAttribute("").toString().equalsIgnoreCase("s")).mapToDouble(feat -> feat.getGeom().length());
			
		
		return sB.toString();
	}

	private static List<String> initListNatures(IFeatureCollection<IFeature> referentiel) {

		List<String> listNatures = new ArrayList<String>();

		for (IFeature feat : referentiel) {

			String nature = feat.getAttribute(attNature).toString();

			if (!listNatures.contains(nature)) {
				listNatures.add(nature);
			}

		}

		listNatures.sort(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {

				return o1.compareToIgnoreCase(o2);
			}
		});

		return listNatures;
	}

	private static List<String> initListClasses(IFeatureCollection<IFeature> referentiel) {

		List<String> listClasses = new ArrayList<String>();

		for (IFeature feat : referentiel) {

			String classe = feat.getAttribute(attClasse).toString();

			if (!listClasses.contains(classe)) {
				listClasses.add(classe);
			}

		}

		listClasses.sort(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {

				return o1.compareToIgnoreCase(o2);
			}
		});

		return listClasses;
	}
}
