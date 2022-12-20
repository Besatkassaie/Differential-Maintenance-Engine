
import java.io.*;



import org.dblp.mmdb.Person;

import org.dblp.mmdb.Publication;
import org.dblp.mmdb.RecordDb;
import org.dblp.mmdb.RecordDbInterface;

import org.xml.sax.SAXException;


/**
 * @Author Besat Kassaie
 * @Note this class split dblp-2015-03-02.xml to files where each file contain information abut one individual
 *  // we want to create a file for each person in dblp and save it seperately and name the file  as  person name.xml
 *  we wrap everything with <record>...</record>
 *  */
public class createDocumentDB {


    public static void main(String[] args) {

        // we need to raise entityExpansionLimit because the dblp.xml has millions of entities
        System.setProperty("entityExpansionLimit", "10000000");

        String dblpXmlFilename = args[0];
        String dblpDtdFilename = args[1];

        System.out.println("building the dblp main memory DB ...");
        RecordDbInterface dblp;
        try {
            dblp = new RecordDb(dblpXmlFilename, dblpDtdFilename, false);
        } catch (final IOException ex) {
            System.err.println("cannot read dblp XML: " + ex.getMessage());
            return;
        } catch (final SAXException ex) {
            System.err.println("cannot parse XML: " + ex.getMessage());
            return;
        }

       //  String path_folder="/dblp/data/benchmark/";
        String path_folder=args[3];

        for (Person pers : dblp.getPersons()) {
            String perrecord_content=pers.getXml();
            if(perrecord_content=="")
                System.out.println("Found an empty person tag: "+pers.getKey());
            String pub_content="";

            for(Publication pub:pers.getPublications()){

                pub_content=pub_content+pub.getXml();
            }
            String file_content= "<record>"+perrecord_content+pub_content+"</record>";

            String file_name=path_folder+pers.getPrimaryName().name()+".xml";
            // this is the encoding that has been used by Morciano
            String encoding="ISO-8859-1";
            Writer out = null;

            // Open output file
            File outputFile = new File(file_name);
            // Check if it already exists
            if(outputFile.exists()) try {
                throw new Exception("File " + file_name + " already exists.");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try (BufferedWriter fw = new BufferedWriter(new FileWriter(outputFile, true))) {
                System.out.println("writing out file: "+file_name);
             //   System.out.println("numebr of publications: "+pers.numberOfPublications());
                fw.write(file_content);
                fw.flush();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }
}
