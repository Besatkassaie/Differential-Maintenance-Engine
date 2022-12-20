package cs.uwaterloo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
// with this class I extract pure run time from the log printed in the console and saved as a text file
// we are worried that the time at the end does not reflect actual runtime
public class runTimeExtraction {


    public static void main(String args[]) {

        String fileName = "consoleLog.txt";
        List<String> list = new ArrayList<>();
        // in msec
      //  float totalTokenizerTime=0;
    //    float totalRuleTime=0;
        float totalnetTime=0;
        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {



            // if it has the tokenizer time get the number and look for the jape then get the numbers add and get the total

            list = stream
                    .filter(line -> (line.contains("Net time:")||(line.contains("done"))&& !line.contains(".xml")))
                    .collect(Collectors.toList());

       }catch (IOException e) {
            e.printStackTrace();
        }

        for (int i=0; i<list.size(); i++){
            String ln=list.get(i);
            if (ln.contains("done")){
               // System.out.println(i);
                ln=list.get(i-1);
                String netTime=ln.substring(ln.lastIndexOf("Net time:")+9,ln.lastIndexOf(";")-1) ;
                netTime=netTime.trim();
                totalnetTime=totalnetTime+Float.parseFloat(netTime);

            }
//            if(ln.contains("Time taken so far by ANNIE English Tokeniser")){
//                String tkTime=ln.substring(ln.lastIndexOf(":")+1) ;
//                tkTime=tkTime.trim();
//                totalTokenizerTime=totalTokenizerTime+Float.parseFloat(tkTime);
//            }
//            else if(ln.contains("Time taken so far by JapeRule:")){
//                String ruleTime=ln.substring(ln.lastIndexOf(":")+1) ;
//                ruleTime=ruleTime.trim();
//                totalRuleTime=totalRuleTime+Float.parseFloat(ruleTime);
//            }

        }

     //   double hours_token=( totalTokenizerTime/1000)/3600;
    //    double hour_rule=(totalRuleTime/1000)/3600;
        double hours_net= (totalnetTime)/3600;
        //System.out.println("Tokenization Time hours: "+hours_token);
     //  System.out.println("JapeRule Time hours: "+hour_rule);
        System.out.println("Total net time in H"+hours_net);




    }
}
