package dao;

import model.Note;
import exceptions.CsvException;
import exceptions.FileCorruptedException;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;


public class CsvManager {
    private String csvFilePath;
    private Path csvPath;
    private static final String CSV_SEPARATOR = ";";
    private static final String CSV_HEADER = "ID;TITLE;CREATION_DATE;MODIFICATION_DATE;TAGS";


    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CsvManager(String csvFilePath) throws CsvException {
        if (csvFilePath == null || csvFilePath.isBlank()) {
            throw new CsvException("Le chemin du fichier CSV ne peut pas être vide");
        } //pour verifier que le chemin n est pas vide et le transformer en Path

        this.csvFilePath = csvFilePath;
        this.csvPath = Paths.get(csvFilePath).toAbsolutePath();

        initializeCsvFile();
    }


    private void initializeCsvFile() throws CsvException {
        try {
            if (!Files.exists(csvPath)) //verifier si le fichier existe
            {
                Path parentDir = csvPath.getParent();//recuperer le dossier parent
                //si le dossier n existe pas, on le cree automatiquement
                if (parentDir != null && !Files.exists(parentDir)) {
                    Files.createDirectories(parentDir);
                    System.out.println("Dossier créé: " + parentDir);
                }

                // creer le fichier csv s il n existe pas, sinn erreur
                try (BufferedWriter writer = Files.newBufferedWriter(csvPath, StandardOpenOption.CREATE_NEW)) {
                    //ecriture de l en tete
                    writer.write(CSV_HEADER);
                    writer.newLine();
                }
                //confirmation
                System.out.println("Fichier CSV créé: " + csvPath);
            } else { //si le fichier existe deja, on l utilise tel quel
                System.out.println("Fichier CSV déjà existant: " + csvPath);
            }
        } catch (IOException e) {
            throw new CsvException("Impossible de créer le fichier CSV: " + csvPath, e);
        }
    }

    public List<Note> readAllNotes() throws CsvException {
        //une liste vide qui va contenir toutes les notes
        List<Note> notes = new ArrayList<>();

        //ouvre le fichier dont le chemin est csvPath et bufferedReader pour lire le fichier ligne par ligne
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String line; //contient une ligne du fichier a chq iteration
            boolean isFirstLine = true; //pour ignorer l en tete du CSV

            while ((line = reader.readLine()) != null) { //boucle jusqu a 0 lignes
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; //sauter la 1er ligne pour ne pas l analyser comme une note
                }

                if (!line.trim().isEmpty()) { //line.trim supprime les espaces au debut et a la fin, si ligne=vide on ne fait rien
                    try {
                        Note note = parseCsvLine(line); //transformer la ligne en objet note
                        if (note != null) {
                            notes.add(note); //si la ligne est correctement parsee -> add
                        }
                    } catch (DateTimeParseException e) { //si une date est mal formatee dans la ligne
                        System.err.println("Ligne ignorée (format de date invalide): " + line);
                        continue;
                    }
                }
            }

        } catch (IOException e) {
            throw new CsvException("lecture", csvFilePath, e);
        }

        return notes; //retourner la liste des notes valides
    }


    private Note parseCsvLine(String line) throws CsvException {
        try {
            //divise la ligne en tab de chaines avec ; comme separateur
            //Le -1 permet de conserver les champs vides a la fin de la ligne
            String[] parts = line.split(CSV_SEPARATOR, -1);

            //verification du nbr de colonnes: on doit avoir au moins 5col sinn erreur
            if (parts.length < 5) {
                throw new FileCorruptedException(csvFilePath,
                        new IllegalArgumentException("Ligne CSV incomplète: " + line));
            }

            //si id est vide, on mets null, sinon on convertit la chaine en Long
            Long id = parts[0].isEmpty() ? null : Long.parseLong(parts[0]);//parseLong transforme string en un nbre long
            String title = parts[1];

            //si la date est vide, on utilise la date actuelle, sinon on la converti
            LocalDateTime creationDate = parts[2].isEmpty() ?
                    LocalDateTime.now() :
                    LocalDateTime.parse(parts[2], DATE_FORMATTER);

            //si vide on met la mm valeur que creationdate
            LocalDateTime modificationDate = parts[3].isEmpty() ?
                    creationDate :
                    LocalDateTime.parse(parts[3], DATE_FORMATTER);

            //creer note avec content vide
            Note note = new Note(id, title, "", creationDate, modificationDate);

            //si le champ tags n est pas vide, on appelle settags.., pour transformer la chaine en liste des tags
            if (!parts[4].isEmpty()) {
                note.setTagsFromString(parts[4]);
            }

            return note;

        } catch (NumberFormatException e) {
            throw new FileCorruptedException(csvFilePath, e);
        } catch (DateTimeParseException e) {
            throw new FileCorruptedException(csvFilePath, e);
        }
    }


    public void writeAllNotes(List<Note> notes) throws CsvException {
        try {
            //on cree un fichier temporaire pour nouvelles notes, au cas ou une erreur survient
            Path tempFile = Files.createTempFile("notes", ".csv.tmp");

            //ouvriture du fichier tempor
            try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
                //en tete
                writer.write(CSV_HEADER);
                //saut d une ligne apres
                writer.newLine();

                for (Note note : notes) {
                    //convert la note en ligne CSV
                    String csvLine = convertNoteToCsvLine(note);
                    //chq ligne est ecrite suivie d un saut de ligne
                    writer.write(csvLine);
                    writer.newLine();
                }
            }
            //une fois tt les lignes ecrites correctement on remplace notre fichier par le temporaire
            Files.move(tempFile, csvPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new CsvException("écriture", csvFilePath, e);
        }
    }


    private String convertNoteToCsvLine(Note note) {
        //un tab de 5cases pou les 5colonnes du CSV
        String[] parts = new String[5];

        //si l id est null, chaine vide dans csv; sinn convertit en chaine
        parts[0] = note.getId() != null ? note.getId().toString() : "";
        //recuperer titre avec methode escapeCsv pour gerer les pointsvirg, guillemets ...(securite)
        parts[1] = escapeCsvField(note.getTitle());
        //recupere dates sous forme de chaine
        parts[2] = note.getFormattedCreationDate();
        parts[3] = note.getFormattedModificationDate();
        //transforme la liste des tags en chaine (String.join) separee par ,
        parts[4] = escapeCsvField(String.join(",", note.getTags()));

        //relier les colonnes avec ;
        return String.join(CSV_SEPARATOR, parts);
    }


    private String escapeCsvField(String field) {
        //si le champ est null, return chaine vide
        if (field == null) return "";

        //verifier si le champ contient ; "" retour a la ligne
        if (field.contains(CSV_SEPARATOR) || field.contains("\"") || field.contains("\n")) {
            //si le champ contient un guillemet, on le double
            field = field.replace("\"", "\"\"");
            return "\"" + field + "\"";
            //expl: je suis "adam", ca devient "je suis ""adam"""
            //ou bonjour;adam devient "bonjour;adam"
        }
        return field;
    }


    public void addNote(Note note) throws CsvException {
        try {
            //liste de notes qu on a dans le csv
            List<Note> notes = readAllNotes();
            notes.add(note); //ajout de la note
            writeAllNotes(notes); //on reecrit toutes les notes dans le CSV
        } catch (Exception e) {
            throw new CsvException("Impossible d'ajouter la note", e);
        }
    }



    public List<Note> importFromCsv(String sourceFilePath) throws CsvException {
        try {
            //transforme le chemin en path pour pouvoir utiliser les methodes files, s il n existe pas exception
            //Files: classe utilitaire de Java avec plusieurs methodes:lire ecrire creer copier..
            Path sourcePath = Paths.get(sourceFilePath);
            if (!Files.exists(sourcePath)) {
                throw new CsvException("Fichier source introuvable: " + sourceFilePath);
            }

            //liste vide qui va contenir les notes importees
            List<Note> importedNotes = new ArrayList<>();
            //pour lire ligne par ligne
            try (BufferedReader reader = Files.newBufferedReader(sourcePath)) {
                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    //ignirer l en tete
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    //si la ligne n est pas vide
                    if (!line.trim().isEmpty()) {
                        try {
                            //on transforme la ligne csv en obj note
                            Note note = parseCsvLine(line);
                            //si ca fonctionne on l ajoute a la liste
                            if (note != null) {
                                importedNotes.add(note);
                            }
                        } catch (DateTimeParseException e) { //si la date est mal formatee
                            System.err.println("Ligne ignorée lors de l'import: " + line);
                            continue;
                        }
                    }
                }
            }

            return importedNotes;

        } catch (IOException e) {
            throw new CsvException("Impossible d'importer depuis le fichier", e);
        }
    }


    public void exportToCsv(List<Note> notes, String destinationFilePath) throws CsvException {
        try {
            //transforme le chemin en path pour pouvoir utiliser les methodes files
            Path destPath = Paths.get(destinationFilePath);
            //s assurer que les dossiers parents existent sinn on les creer auto
            Files.createDirectories(destPath.getParent());

            //ecrire ligne par ligne si le fichier n existe pas on le cree
            try (BufferedWriter writer = Files.newBufferedWriter(destPath)) {
                //ecrire l en tete
                writer.write(CSV_HEADER);
                writer.newLine();

                //ecriture des notes
                for (Note note : notes) {
                    //convertir la note en ligne csv
                    String csvLine = convertNoteToCsvLine(note);
                    // l ecrire dans le fichier
                    writer.write(csvLine);
                    //saut de ligne
                    writer.newLine();
                }
            }
            //confirmation
            System.out.println("Export terminé: " + destinationFilePath);

            //gestion d erreurs
        } catch (IOException e) {
            throw new CsvException("Impossible d'exporter vers le fichier", e);
        }
    }

}