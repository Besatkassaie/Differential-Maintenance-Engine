# Differential-Maintenance-Engine
DME identifies updates that can be autonomously applied to extracted relations.  
# LICENSE
(C) Copyright 2022 Besat Kassaie <bkassaie@uwaterloo.ca>, All Rights Reserved.
The bundle is released for academic purposes only, all other rights are reserved.
This bundle is provided "as is" with no warranties, and the author in not liable for any damages from its use.
*remarks
1. A part of code belongs to ? and their own licensing
2. We have adopted a Java class from Gate code repository. The related license can be found here: https://gate.ac.uk/gate/licence.html

# Functionalities
1. Scala program to convert regular formulas "x.prg" to their corresponding  eVset-Automaton representation  "x.csp".
2. Scala program to convert core AQL "x.aqls" to their corresponding  eVset-Automaton representation  "x.csp".
3. Scala program to verify an update formula w.r.t input extractors and to determine if the update is pseudo irrelevant.
4. Scala program to apply the shift function
5. Java program to evaluate the Jape rule on documents
6. Java program to split DBLP.xml into bibliographic documents


# Provided Files
1.  Primitive and complex extractors used in experiments along with their ".csp" files [Extractors] (https://github.com/Besatkassaie/Differential-Maintenance-Engine/tree/main/data/extractPrograms/finalExtractors)
2.  Update formulas "x.prg" along with their specifications "x.sp" see [Updates] (https://github.com/Besatkassaie/Differential-Maintenance-Engine/tree/main/data/extractPrograms/Updates)
3.  Only Jape rule "x.jape" along with its Gate application file
4.  Some example of the generate DBLP documents see [benchmark](https://github.com/Besatkassaie/Differential-Maintenance-Engine/tree/main/data/DBLP/benchmark)
5.  eVset-Automaton representations for Allen Intervals see [Allen Intervals](https://github.com/Besatkassaie/Differential-Maintenance-Engine/tree/main/data/Allen_Interval)
