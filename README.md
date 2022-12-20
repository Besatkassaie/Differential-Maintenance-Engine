# Differential-Maintenance-Engine
 We have developed a system in Scala, DME, that verifies an extraction program expressed as a core AQL query.  If the program passes the test, the extracted view content is updated by running a shift algorithm. If it does not pass the test the extractor needs to be  executed from scratch. Through experimentation,  we show the run-time overhead imposed by our verifier in practice. Also, we compare the run-time of differential maintenance of the extracted views and re-executing the extraction program. Our system is developed on top of the engine proposed and implemented by Marciano[^1].
## LICENSE
(C) Copyright 2022 Besat Kassaie <bkassaie@uwaterloo.ca>, All Rights Reserved.
The bundle is released for academic purposes only, all other rights are reserved.
This bundle is provided "as is" with no warranties, and the author in not liable for any damages from its use.
*remarks
1. We are working on getting the permission to release part of the code until then code related to [functionalities](https://github.com/Besatkassaie/Differential-Maintenance-Engine#functionalities) 3,4,5, and 6 cannot be released.
2. We have adopted a Java class from Gate code repository. The related license can be found here: https://gate.ac.uk/gate/licence.html
[^1]: Morciano, A. (2017). Engineering a runtime system for AQL
## Functionalities
1. [Java program to evaluate the Jape rule on documents](https://github.com/Besatkassaie/Differential-Maintenance-Engine#java-program-to-evaluate-the-jape-rule-on-documents)
2. [Java program to split DBLP.xml into bibliographic documents](https://github.com/Besatkassaie/Differential-Maintenance-Engine#java-program-to-split-dblpxml-into-bibliographic-documents)
3. Scala program to convert regular formulas "x.prg" to their corresponding  eVset-Automaton representation  "x.csp".
4. Scala program to convert core AQL "x.aqls" to their corresponding  eVset-Automaton representation  "x.csp".
5. Scala program to verify an update formula w.r.t input extractors and to determine if the update is pseudo irrelevant.
6. Scala program to apply the shift function


## Provided Data Files
1.  Primitive and complex extractors used in experiments along with their ".csp" files [Extractors](data/extractPrograms/finalExtractors)
2.  Update formulas "x.prg" along with their specifications "x.sp" see [Updates](data/extractPrograms/Updates)
3.  Only Jape rule ["x.jape"](gaterelated/Data/Grammar/Article.jape) along with its Gate application file: [extraction.gapp](gaterelated/extraction.gapp)
4.  Some example of the generate DBLP documents see [benchmark](data/DBLP/benchmark)
5.  eVset-Automaton representations for Allen Intervals see [Allen Intervals](data/Allen_Interval)


### Java program to evaluate the Jape rule on documents
Steps:
1. We used gate developer interface to create the gate application file: [extraction.gapp](gaterelated/extraction.gapp)
   - create a "corpus pipeline application" in [Gate Developer 9.0.1](https://gate.ac.uk/download/)
   - add Annie Tokenizer to the pipeline as well as our Jape rule  
   - save application state as "extraction.gapp" to load from the Java class[^2]

  [^2]:  the steps can be done by coding and without using Gate Developer Interface.

2. run class  [BatchProcessApp](gaterelated/Code/src/cs/uwaterloo/BatchProcessApp.java) with following parameters:
  * -g "gate application file path"
  * -c "corpus path"
  * -o "output path to write results"
  * -l "log file path"

*NOTE: the time spent for extraction will be written in the log file.*

### Java program to split DBLP.xml into bibliographic documents

#### prepration
   1. To create a document database we split [DBLP dblp-2022-10-02.xml.gz]( https://dblp.org/xml/release/).
   2. we wrote a program using source code provided by [DBLP team](https://dblp.org/src/mmdb-2019-04-29-sources.jar)

#### create database
  run class [createDocumentDB](DBLP_PrepData/src/createDocumentDB.java) with three parameters with no flag:
   - dblp xml file path
   - dblp dtd file path
   - output path to store documents
