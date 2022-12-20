///*
// *  BatchProcessApp.java
// *
// *
// * Copyright (c) 2006, The University of Sheffield.
// *
// * This file is part of GATE (see http://gate.ac.uk/), and is free
// * software, licenced under the GNU Library General Public License,
// * Version 2, June1991.
// *
// * A copy of this licence is included in the distribution in the file
// * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
// *
// *  Ian Roberts, March 2006
// *
// *  $Id: BatchProcessApp.java,v 1.5 2006/06/11 19:17:57 ian Exp $
// */
//
//import gate.Document;
//import gate.Corpus;
//import gate.CorpusController;
//import gate.AnnotationSet;
//import gate.Gate;
//import gate.Factory;
//import gate.util.*;
//import gate.util.persistence.PersistenceManager;
//
//import java.io.*;
//import java.nio.file.DirectoryStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.*;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
///**
// * This class ilustrates how to do simple batch processing with GATE.  It loads
// * an application from a .gapp file (created using "Save application state" in
// * the GATE GUI), and runs the contained application over one or more files.
// * The results are written out to XML files, either in GateXML format (all
// * annotation sets preserved, as in "save as XML" in the GUI), or with inline
// * XML tags taken from the default annotation set (as in "save preserving
// * format").  In this example, the output file names are simply the input file
// * names with ".out.xml" appended.
// *
// * To keep the example simple, we do not do any exception handling - any error
// * will cause the process to abort.
// */
//public class BatchProcessApp {
//
//    /**
//     * The main entry point.  First we parse the command line options (see
//     * usage() method for details), then we take all remaining command line
//     * parameters to be file names to process.  Each file is loaded, processed
//     * using the application and the results written to the output file
//     * (inputFile.out.xml).
//     */
//    public static void main(String[] args) throws Exception {
//        parseCommandLine(args);
//
//        // initialise GATE - this must be done before calling any GATE APIs
//        Gate.init();
//
//        // load the saved application
//        CorpusController application =
//                (CorpusController) PersistenceManager.loadObjectFromFile(gappFile);
//
//        // Create a Corpus to use.  We recycle the same Corpus object for each
//        // iteration.  The string parameter to newCorpus() is simply the
//        // GATE-internal name to use for the corpus.  It has no particular
//        // significance.
//        Corpus corpus = Factory.newCorpus("BatchProcessApp Corpus");
//
//        application.setCorpus(corpus);
//
//
//// Reading only files in the directory
//        List<File> fileList = new ArrayList<>();
//
//       String logString="";
//// Reading only files in the directory
//        try {
//            fileList = java.nio.file.Files.list(Paths.get(corpusPath))
//                    .map(Path::toFile)
//                    .filter(File::isFile)
//                    .collect(Collectors.toList());
//
//            fileList.forEach(System.out::println);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        HashMap <File,String> nameTocontent = new HashMap();
//
//        // process the files one by one
//        for (int i = 0; i < fileList.size(); i++) {
//            // load the document (using the specified encoding if one was given)
//            String filename = fileList.get(i).getName();
//            Path filePath = fileList.get(i).toPath();
//
//            if (filename.indexOf(".DS_Store")<0) {
//                List<String> lines = Files.readAllLines(filePath);
//                String content =String.join("",lines);
//               // File docFile = new File(filename);
//
//                nameTocontent.put(fileList.get(i),content);
//            }
//
//
//        }
//        ArrayList<File> listOfKeys = new ArrayList(nameTocontent.keySet());
//        Long start = java.lang.System.currentTimeMillis();
//
//        for (int i = 0; i < listOfKeys.size(); i++) {
//            logString="";
//            File file=listOfKeys.get(i);
//            String fileName=file.getName();
//            String content=nameTocontent.get(file);
//            Document doc = Factory.newDocument(content);
//            // put the document in the corpus
//            corpus.add(doc);
//            // run the application
//            application.execute();
//            int numberofannotation=doc.getAnnotations().getAllTypes().size();
//            if(numberofannotation>2){
//                logString=logString+"\n";
//                logString=logString+"**********"+fileName+"********"+"\n";
//                logString=logString+doc.getAnnotations().getAllTypes().toString()+"\n";
//            }
//            try
//            {
//
//                FileWriter fw = new FileWriter(logFile,true); //the true will append the new data
//                fw.write(logString);//appends the string to the file
//                fw.close();
//            }
//            catch(IOException ioe)
//            {
//                System.err.println("IOException: " + ioe.getMessage());
//            }
//            // remove the document from the corpus again
//            corpus.clear();
//            String docXMLString = null;
//            // if we want to just write out specific annotation types, we must
//            // extract the annotations into a Set
//            if (annotTypesToWrite != null) {
//                // Create a temporary Set to hold the annotations we wish to write out
//                Set annotationsToWrite = new HashSet();
//
//                // we only extract annotations from the default (unnamed) AnnotationSet
//                // in this example
//                AnnotationSet defaultAnnots = doc.getAnnotations();
//
//                Iterator annotTypesIt = annotTypesToWrite.iterator();
//                while (annotTypesIt.hasNext()) {
//                    // extract all the annotations of each requested type and add them to
//                    // the temporary set
//                    AnnotationSet annotsOfThisType =
//                            defaultAnnots.get((String) annotTypesIt.next());
//                    if (annotsOfThisType != null) {
//                        annotationsToWrite.addAll(annotsOfThisType);
//                    }
//                }
//
//                // create the XML string using these annotations
//                docXMLString = doc.toXml(annotationsToWrite);
//            }
//            // otherwise, just write out the whole document as GateXML
//            else {
//                docXMLString = doc.toXml();
//            }
//
//            // Release the document, as it is no longer needed
//            Factory.deleteResource(doc);
//
//            // output the XML to <inputFile>.out.xml
//            String outputFileName = fileName + ".out.xml";
//            File outputFile = new File(outputDirectory, outputFileName);
//            // Write output files using the same encoding as the original
//            FileOutputStream fos = new FileOutputStream(outputFile);
//            BufferedOutputStream bos = new BufferedOutputStream(fos);
//            OutputStreamWriter out;
//            if (encoding == null) {
//                out = new OutputStreamWriter(bos);
//            } else {
//                out = new OutputStreamWriter(bos, encoding);
//            }
//
//            out.write(docXMLString);
//
//            out.close();
//
//            System.out.println("done");
//        } // for each file
//        Long stop = java.lang.System.currentTimeMillis();
//
//        try
//        {
//
//            FileWriter fw = new FileWriter(logFile,true); //the true will append the new data
//            fw.write("\n time elapsed to extract: "+formatTime(start,stop));//appends the string to the file
//            fw.close();
//        }
//        catch(IOException ioe)
//        {
//            System.err.println("IOException: " + ioe.getMessage());
//        }
//        System.out.println("All done");
//    } // void main(String[] args)
//
//
//    /**
//     * Parse command line options.
//     */
//    private static void parseCommandLine(String[] args) throws Exception {
//        int i;
//        // iterate over all options (arguments starting with '-')
//        for(i = 0; i < args.length && args[i].charAt(0) == '-'; i++) {
//            switch(args[i].charAt(1)) {
//                // -a type = write out annotations of type a.
//                case 'a':
//                    if(annotTypesToWrite == null) annotTypesToWrite = new ArrayList();
//                    annotTypesToWrite.add(args[++i]);
//                    break;
//
//                // -g gappFile = path to the saved application
//                case 'g':
//                    gappFile = new File(args[++i]);
//                    break;
//
//                // -e encoding = character encoding for documents
//                case 'e':
//                    encoding = args[++i];
//                    break;
//                // -e encoding = character encoding for documents
//                case 'c':
//                    corpusPath = args[++i];
//                    break;
//                case 'o':
//                    outputDirectory = args[++i];
//                    break;
//                case 'l':
//                    logFile = args[++i];
//                    break;
//                default:
//                    System.err.println("Unrecognised option " + args[i]);
//                    usage();
//            }
//        }
//
//        // set index of the first non-option argument, which we take as the first
//        // file to process
//        firstFile = i;
//
//        // sanity check other arguments
//        if(gappFile == null) {
//            System.err.println("No .gapp file specified");
//            usage();
//        }
//    }
//
//    private static String formatTime(Long start, Long stop){
//
//        Long diff = stop - start;
//        double x = diff / 1000;
//
//        String res = ""+ x + " s";
//
//        return res;
//    }
//
//
//    /**
//     * Print a usage message and exit.
//     */
//    private static final void usage() {
//        System.err.println(
//                "Usage:\n" +
//                        "   java sheffield.examples.BatchProcessApp -g <gappFile> [-e encoding]\n" +
//                        "            [-a annotType] [-a annotType] file1 file2 ... fileN\n" +
//                        "\n" +
//                        "-g gappFile : (required) the path to the saved application state we are\n" +
//                        "              to run over the given documents.  This application must be\n" +
//                        "              a \"corpus pipeline\" or a \"conditional corpus pipeline\".\n" +
//                        "\n" +
//                        "-e encoding : (optional) the character encoding of the source documents.\n" +
//                        "              If not specified, the platform default encoding (currently\n" +
//                        "              \"" + System.getProperty("file.encoding") + "\") is assumed.\n" +
//                        "\n" +
//                        "-a type     : (optional) write out just the annotations of this type as\n" +
//                        "              inline XML tags.  Multiple -a options are allowed, and\n" +
//                        "              annotations of all the specified types will be output.\n" +
//                        "              This is the equivalent of \"save preserving format\" in the\n" +
//                        "              GATE GUI.  If no -a option is given the whole of each\n" +
//                        "              processed document will be output as GateXML (the equivalent\n" +
//                        "              of \"save as XML\")."+
//                "\n" +
//                        "-c curpusPath :path of corpus containing .txt files"+
//                        "\n" +
//                        "-o outputPath :path of output direcory"+
//                "\n" +
//                        "-l filename of log file"
//        );
//
//        System.exit(1);
//    }
//
//    /** Index of the first non-option argument on the command line. */
//    private static int firstFile = 0;
//
//    /** Path to the saved application file. */
//    private static File gappFile = null;
//
//    /**
//     * List of annotation types to write out.  If null, write everything as
//     * GateXML.
//     */
//    private static List annotTypesToWrite = null;
//
//    /**
//     * The character encoding to use when loading the docments.  If null, the
//     * platform default encoding is used.
//     */
//    private static String encoding = null;
//    /**
//     * @Note path of the corpus
//     */
//    private static String corpusPath="";
//    /**
//     * @Note path of the corpus
//     */
//    private static String outputDirectory="";
//    /**
//     * @Note path of the log file
//     */
//    private static String logFile="";
//}