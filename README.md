## Differential-Maintenance-Engine
 We have developed a system in Scala, DME, that verifies an extraction program expressed as a core AQL query.  If the program passes the test, the extracted view content is updated by running a shift algorithm. If it does not pass the test the extractor needs to be  executed from scratch. Through experimentation,  we show the run-time overhead imposed by our verifier in practice. Also, we compare the run-time of differential maintenance of the extracted views and re-executing the extraction program. DME is developed on top of the engine proposed and implemented by Marciano[^1].
### LICENSE
(C) Copyright 2022 Besat Kassaie <bkassaie@uwaterloo.ca>, All Rights Reserved.
The bundle is released for academic purposes only, all other rights are reserved.
This bundle is provided "as is" with no warranties, and the author in not liable for any damages from its use.

##### Remarks
1. We are working on getting the permission to release some parts of the code[^1], until then code related to [functionalities](https://github.com/Besatkassaie/Differential-Maintenance-Engine#functionalities) 3, 4, 5, and 6 cannot be fully functional .
2. We have adopted a Java class from Gate code repository. The related license can be found here: https://gate.ac.uk/gate/licence.html
3. We have modified XML parser provided by [DBLP team](https://dblp.org/faq/How+to+parse+dblp+xml.html). The related license is in the body of the class.
[^1]: Morciano, A. (2017). Engineering a runtime system for AQL
### Requirements
1. Java SE 11
2. Scala 2.11.2  

### Functionalities
1. [Java program to split DBLP.xml into bibliographic documents](https://github.com/Besatkassaie/Differential-Maintenance-Engine#java-program-to-split-dblpxml-into-bibliographic-documents)
2. [Java program to evaluate the Jape rule on documents](https://github.com/Besatkassaie/Differential-Maintenance-Engine#java-program-to-evaluate-the-jape-rule-on-documents)
3. [Scala program to convert regular formulas ["x.prg"](data/extractPrograms/finalExtractors/proc_less/proc_less.prg) to their corresponding  eVset-Automaton representation  ["x.csp"](data/extractPrograms/finalExtractors/proc_less/proc_less_QnRfM.csp)](https://github.com/Besatkassaie/Differential-Maintenance-Engine#scala-program-to-convert-regular-formulas-to-evset-automaton-representation)
4. Scala program to convert core AQL ["x.aqls"](data/extractPrograms/finalExtractors/proc_less_article_less_moreThanOneAuthor/moreThOneAuthorProc_lessArcl_less.aqls) to their corresponding  eVset-Automaton representation  ["x.csp"](data/extractPrograms/finalExtractors/proc_less_article_less_moreThanOneAuthor/moreThOneAuthorProc_lessArcl_less.csp).
5. [Scala program to verify an update formula w.r.t input extractors and to determine if the update is pseudo irrelevant](https://github.com/Besatkassaie/Differential-Maintenance-Engine#scala-program-to-verify-an-update-formula-wrt-input-extractors)
6. [Scala program to apply the shift function](https://github.com/Besatkassaie/Differential-Maintenance-Engine#scala-program-to-apply-the-shift-function)


### Provided Data Files
1.  [Extractors](data/extractPrograms/finalExtractors) primitive and complex extractors used in experiments along with their ".csp" files.
2.  [Updates](data/extractPrograms/Updates) update formulas "x.prg" along with their specifications "x.sp".
3.  [Gate application](gaterelated/extraction.gapp) along with the Jape rule ["x.jape"](gaterelated/Data/Grammar/Article.jape).
4.  [Benchmark](data/DBLP/benchmark) samples of generated DBLP documents.
5.  [Allen Intervals](data/Allen_Interval) eVset-Automaton representations for Allen Intervals.

#### Java program to split DBLP.xml into bibliographic documents

##### Requirements
   1. Download DBLP file [DBLP dblp-2022-10-02.xml.gz]( https://dblp.org/xml/release/).
   2. Download library used in [DBLP Source](https://dblp.org/src/mmdb-2019-04-29-sources.jar)

##### create database
  run class [createDocumentDB](DBLP_PrepData/src/createDocumentDB.java) with three parameters with no flag:
   - *dblp xml file path*
   - *dblp dtd file path*
   - *output path to store documents*

#### Java program to evaluate the Jape rule on documents
##### Requirements
 * We used gate developer interface to create the gate application file: [extraction.gapp](gaterelated/extraction.gapp)
   - create a "corpus pipeline application" in [Gate Developer 9.0.1](https://gate.ac.uk/download/)
   - add Annie Tokenizer to the pipeline as well as [the rule]((gaterelated/Data/Grammar/Article.jape))  
   - save application state as "extraction.gapp" to later load from the Java class[^2]

  [^2]:  the steps can be done by coding and without using Gate Developer Interface.
##### Run Gate Extractor
* run class [BatchProcessApp](gaterelated/Code/src/cs/uwaterloo/BatchProcessApp.java) with following parameters:
  * *-g gate application file path*
  * *-c corpus path*
  * *-o output path to write results*
  * *-l log file path*

#### Scala program to convert regular formulas to eVset-Automaton representation.
* run class [ProgramManagement.scala](src/ca/uwaterloo/cs/util/ProgramManagement.scala) with two parameters with no flag:
 - *program file x.prg path*
 - *output directory path to store the eVset-Automaton representation*
####  Scala program to verify an update formula w.r.t input extractors
* run class [_psedu_regexpr.scala](src/_psedu_regexpr.scala) with 4 parameters
- *update's .prg file path *
- *update's specification's file path .sp*
- *extractor .prg file path *
- *X.txt file path*

####  Scala program to apply the shift function
* run class [ReadOutputFile.scala](src/ca/uwaterloo/cs/psupdate/ReadOutputFile.scala) with 4 parameters
-  args(0) file path of update relation[sample file](data/extractPrograms/Updates/DateFormat/output_dateFormat_4l2Fp.txt)
-  args(1) file path of extracted relation [sample file](data/extractPrograms/finalExtractors/article_less/output_article_less_At43o.txt)
-  args(2) file path shifted extracted relation
-  args(3) update's specification's file path [sample file](data/extractPrograms/Updates/DateFormat/dateFormat.sp)

*NOTE: the time spent for the extraction is written in the log file.*
