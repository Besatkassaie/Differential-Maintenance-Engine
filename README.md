# Differential-Maintenance-Engine
DME identifies updates that can be autonomously applied to extracted relations.  
## LICENSE
(C) Copyright 2022 Besat Kassaie <bkassaie@uwaterloo.ca>, All Rights Reserved.
The bundle is released for academic purposes only, all other rights are reserved.
This bundle is provided "as is" with no warranties, and the author in not liable for any damages from its use.
*remarks
1. A part of code belongs to ? and their own licensing
2. We have adopted a Java class from Gate code repository. The related license can be found here: https://gate.ac.uk/gate/licence.html

## Functionalities
1. Scala program to convert regular formulas "x.prg" to their corresponding  eVset-Automaton representation  "x.csp".
2. Scala program to convert core AQL "x.aqls" to their corresponding  eVset-Automaton representation  "x.csp".
3. Scala program to verify an update formula w.r.t input extractors and to determine if the update is pseudo irrelevant.
4. Scala program to apply the shift function
5. Java program to evaluate the Jape rule on documents
6. Java program to split DBLP.xml into bibliographic documents


## Provided data Files
1.  Primitive and complex extractors used in experiments along with their ".csp" files [Extractors](data/extractPrograms/finalExtractors)
2.  Update formulas "x.prg" along with their specifications "x.sp" see [Updates](data/extractPrograms/Updates)
3.  Only Jape rule ["x.jape"](gaterelated/Data/Grammar/Article.jape) along with its Gate application file: [extraction.gapp](gaterelated/extraction.gapp)
4.  Some example of the generate DBLP documents see [benchmark](data/DBLP/benchmark)
5.  eVset-Automaton representations for Allen Intervals see [Allen Intervals](data/Allen_Interval)


### Java program to evaluate the Jape rule on documents
Steps:
1. We used gate developer interface to create the gate application file: [extraction.gapp](gaterelated/extraction.gapp) follow the steps below:
  1.1 creat a "corpus pipeline application" in [Gate Developer 9.0.1](https://gate.ac.uk/download/)
  1.2 add Annie Tokenizer to the pipeline as well as our Jape rule  
  1.3 save application state as "extraction.gapp" to load from the Java class

  *NOTE: steps can be done by coding.*

2. run class  [BatchProcessApp](gaterelated/Code/src/cs/uwaterloo/BatchProcessApp.java) with following parameters:
  * -g "gate application file path"
  * -c "corpus path"
  * -o "output path to write results"
  * -l "log file path"

*NOTE: the time spent for extraction will be written in the log file.*

### Java program to split DBLP.xml into bibliographic documents
