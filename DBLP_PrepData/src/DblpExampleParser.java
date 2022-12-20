//
// Copyright (c)2015, dblp Team (University of Trier and
// Schloss Dagstuhl - Leibniz-Zentrum fuer Informatik GmbH)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// (1) Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//
// (2) Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
//
// (3) Neither the name of the dblp team nor the names of its contributors
// may be used to endorse or promote products derived from this software
// without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL DBLP TEAM BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

import java.io.*;
import java.util.*;

import org.dblp.mmdb.Field;
import org.dblp.mmdb.Person;
import org.dblp.mmdb.PersonName;
import org.dblp.mmdb.Publication;
import org.dblp.mmdb.RecordDb;
import org.dblp.mmdb.RecordDbInterface;
import org.dblp.mmdb.TableOfContents;
import org.xml.sax.SAXException;


@SuppressWarnings("javadoc")
class DblpExampleParser {

    public static void main(String[] args) throws FileNotFoundException {

        // we need to raise entityExpansionLimit because the dblp.xml has millions of entities
        System.setProperty("entityExpansionLimit", "10000000");

      //  args[0]="dblp-2019-04-01.xml";
     //   args[1]="dblp-2017-08-29.dtd";
      /*  if (args.length != 2) {
            System.err.format("Usage: java %s <dblp-xml-file> <dblp-dtd-file>\n", DblpExampleParser.class.getName());
            System.exit(0);
        }*/

// TODO:  2022-09-16 these file should change to most recent ones
        String dblpXmlFilename = "/Users/besat/Desktop/SystemTExperimentPapers/DBLP_PrepData/src/dblp-2015-03-02.xml";
        String dblpDtdFilename = "/Users/besat/Desktop/SystemTExperimentPapers/DBLP_PrepData/src/dblp-2012-01-03.dtd";

        System.out.println("building the dblp main memory DB ...");
        RecordDbInterface dblp;
        try {
            dblp = new RecordDb(dblpXmlFilename, dblpDtdFilename, false);

        }
        catch (final IOException ex) {
            System.err.println("cannot read dblp XML: " + ex.getMessage());
            return;
        }
        catch (final SAXException ex) {
            System.err.println("cannot parse XML: " + ex.getMessage());
            return;
        }
        System.out.format("MMDB ready: %d publs, %d pers\n\n", dblp.numberOfPublications(), dblp.numberOfPersons());




         System.out.println("finding longest person name in dblp ...");
        String longestName = null;
        int longestNameLength = 0;
        for (PersonName name : dblp.getPersonNames()) {
            if (name.name().length() > longestNameLength) {
                longestName = name.name();
                longestNameLength = longestName.length();
            }
        }
        System.out.format("%s (%d chars)\n\n", longestName, longestNameLength);

        System.out.println("finding most prolific author in dblp ...");
        String prolificAuthorName = null;
        int prolificAuthorCount = 0;
        for (Person pers : dblp.getPersons()) {
            int publsCount = pers.numberOfPublications();
            if (publsCount > prolificAuthorCount) {
                prolificAuthorCount = publsCount;
                prolificAuthorName = pers.getPrimaryName().name();
            }
        }
        System.out.format("%s, %d records\n\n", prolificAuthorName, prolificAuthorCount);

        System.out.println("finding author with most coauthors in dblp ...");
        String connectedAuthorName = null;
        int connectedAuthorCount = 0;
        for (Person pers : dblp.getPersons()) {
            int coauthorCount = dblp.numberOfCoauthors(pers);
            if (coauthorCount > connectedAuthorCount) {
                connectedAuthorCount = coauthorCount;
                connectedAuthorName = pers.getPrimaryName().name();
            }
        }
        System.out.format("%s, %d coauthors\n\n", connectedAuthorName, connectedAuthorCount);

        System.out.println("finding coauthors of Jim Gray 0001 ...");
        Person jim = dblp.getPersonByName("Jim Gray 0001");
        for (int i = 0; i < dblp.numberOfCoauthorCommunities(jim); i++) {
            Collection<Person> coauthors = dblp.getCoauthorCommunity(jim, i);
            System.out.format("Group %d:\n", i);
            for (Person coauthor : coauthors) {
                System.out.format("  %s\n", coauthor.getPrimaryName().name());
            }
        }
        System.out.println();

        System.out.println("finding authors of FOCS 2010 ...");
        Comparator<Person> cmp = (Person o1,
                Person o2) -> o1.getPrimaryName().name().compareTo(o2.getPrimaryName().name());
        Map<Person, Integer> authors = new TreeMap<>(cmp);
        TableOfContents focs2010Toc = dblp.getToc("db/conf/focs/focs2010.bht");
        for (Publication publ : focs2010Toc.getPublications()) {
            for (PersonName name : publ.getNames()) {
                Person pers = name.getPerson();
                if (authors.containsKey(pers)) authors.put(pers, authors.get(pers) + 1);
                else authors.put(pers, 1);
            }
        }
        for (Person author : authors.keySet())
            System.out.format("  %dx %s\n", authors.get(author), author.getPrimaryName().name());
        System.out.println();

        System.out.println("finding URLs of FOCS 2010 publications ...");
        for (Publication publ : focs2010Toc.getPublications()) {
            for (Field fld : publ.getFields("ee")) {
                System.out.format("  %s\n", fld.value());
            }
        }

        System.out.println("done.");
    }
}
