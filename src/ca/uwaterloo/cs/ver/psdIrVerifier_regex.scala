package ca.uwaterloo.cs.ver

import be.ac.ulb.arc.core.{CoreSpannerFileReader, Range, RegXTransition, State, VSetAutomaton}
import dk.brics.automaton.RegExp

import scala.{Int => SVar}
import scala.collection.immutable.{HashSet => SVars}
import scala.collection.immutable.{HashMap => AllenSet}
import java.io.{BufferedWriter, File}
import java.util.regex.Pattern
import scala.be.ac.ulb.arc.core
import scala.be.ac.ulb.arc.core.RegularFormula_context.project
import scala.util.control.Breaks.breakable
import scala.be.ac.ulb.arc.core.{ExpressionTree, Node, RegularFormula, RegularFormula_context}
import scala.collection.mutable.ArrayBuffer
/**
 * this calss is verifying  Pseudo-Irrelevance of an update w.r.t  an extractor
 * Both of its input is represented as regular formulas
 *
 * */
class psdIrVerifier_regex (val us:Update_Spec) extends verifier_regex  {


  /***
   * returns list of files in a folder
   * @param dir
   * @return
   */
  def getListOfFiles(dir: String):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  /***
   * loads Allen Intervals
   * @return
   */
  def load_allen_relationships(X:SVar,Y:SVar):AllenSet[String,VSetAutomaton]= {
   //val base_path="/u5/bkassaie/verifier/Verifier/data/Allen_Interval"
    val localDir: String = System.getProperty("user.home")
     println("path: "+localDir)
   val base_path="/Users/besat/ThesisFiles/SystemTExperimentPapers/Verifier/data/Allen_Interval"
    val files = new File(base_path).listFiles()
    var allen_rel_list=new AllenSet[String,VSetAutomaton]
    for (fl<-files)
    {
      //loads csp file into spanner and changes the names of X and Y
      // for 0meet1 and 0equal1 we have 2 disjunctions so we load each disjunct and the union them

      if (fl.getName.startsWith("0"))
        allen_rel_list+=(fl.getName-> CoreSpannerFileReader.getCoreSpanner(fl.getAbsolutePath).get.rename(0,X).get.rename(1,Y).get.automaton)
      else allen_rel_list+= (fl.getName-> CoreSpannerFileReader.getCoreSpanner(fl.getAbsolutePath).get.rename(1,X).get.rename(0,Y).get.automaton)
      //allen_rel_list+= (fl.getName+"_Y", CoreSpannerFileReader.getCoreSpanner(fl.getPath+"/"+fl.getName).get.rename(0,Y).get.rename(1,X))

    }
    allen_rel_list
  }




  /***
   * Conflict
   * Given an update spanner defined by g  If conflicts(g) =∅,
   * then g satisfies the first condition i.e. if the spans for UVar(g) are not equal, they must not overlap
   * @param g
   * @return
   */

  private def conflict(g: VSetAutomaton):Option[VSetAutomaton]={
    System.out.print("\n \n")
    println("conflict function starts ")
    val Max_var= g.V.max

    val X= Max_var+1   //SVar
    val Y= Max_var+2 //SVar

    val g_renamed_x=g.rename(us.updateVar.toInt,X)

    val g_renamed_x_projected=g_renamed_x.π(SVars[SVar](X))

    val g_renamed_y=g.rename(us.updateVar.toInt,Y)
    val g_renamed_y_projected=g_renamed_y.π(SVars[SVar](Y))

    val alen_rel_list=load_allen_relationships(X,Y)
   // var Γ_X_Y:VSetAutomaton=alen_rel_list.get("0Overlaps1.csp").get // is the disjunction of the fifth through the twelfth basic relationships
    var result:Option[VSetAutomaton]=None
    for ((k,v) <- alen_rel_list)
    {
      k match {
        case a if !List("0meets1.csp","0precedes1.csp","1metby0.csp","1precedeby0.csp","0equal1_1.csp",
          "0equal1_2.csp").contains(a)
        => {
          result=       g_renamed_x_projected.⋈(alen_rel_list.get(a).get).get.⋈(g_renamed_y_projected)
          if (result!=None) {
            println("Found overlap  in update conflict detection function!")
            return result
          }
        }
        case _=>
      }
      //println("Verifying continues in update conflict detection function!")
    }

    result

  }





  /***
   * an update automaton g is well defined if it has two properties:
   * 1- is not ambiguous 2- doesn't have any conflict
   * @return
   */

    def isWellDefined():Boolean={
    val g=this.us.update_regularFormula.regFormulaToVSetAutomaton();

    val result_conflict=this.conflict(g)


    if (result_conflict==None )
    {
      val result_ambig=this.ambig(g)
      if (result_ambig==None)

        return true
      else
        return  false
    }
    else
    {
      return false
    }

  }



  private def ambig(g: VSetAutomaton):Option[VSetAutomaton]= {

    var res:Option[VSetAutomaton]= None
    val Z_list=g.V- us.updateVar.toInt

    val Max_var = g.V.max

    val X = Max_var + 1 //SVar
    val Y = Max_var + 2 //SVar

    val alen_rel_list = load_allen_relationships(X, Y)
   // var Γ_X_Y: VSetAutomaton = alen_rel_list.get("0Overlaps1.csp").get // is the disjunction of the fifth through the twelfth basic relationships

    for (z <- Z_list ) {

      val g_renamed_x = g.rename(z, X)

      val g_renamed_x_projected = g_renamed_x.π(SVars[SVar](X, us.updateVar.toInt))

      val g_renamed_y = g.rename(z, Y)
      val g_renamed_y_projected = g_renamed_y.π(SVars[SVar](Y, us.updateVar.toInt))

      for ((k, v) <- alen_rel_list) {
        k match {
          case a if !List("0equal1_1.csp","0equal1_2.csp").contains(a) =>
          {
            var param1=g_renamed_x_projected.⋈(alen_rel_list.get(a).get)
            if(param1!=None)
               res= param1.get.⋈(g_renamed_y_projected)
            if (res!=None) {
              println("Found overlap  in update ambiguity detection function!")
              return res
            }
          } //if k is not amongst the first 4 allen relation
          case _ =>
        }
      }

      //println("Verifying ambiguity continues!")

    }
    res
  }

/**
 * @note A  well-defined update spanner specified by g, is durable if spans
 *      marked by the update variable and spans marked by
 *      context variables by its peer spanner C_UVar(g)(g) are disjoint.
 * */
 def durable():Boolean= {
   val rf= this.us.update_regularFormula

   val updatevar=this.us.updateVar
   var expression_tree = rf.regFormulaToExpressionTree();
   var prj = project(expression_tree.root, updatevar.toInt)
   var prj_formula = new RegularFormula(ExpressionTree.toExpression(prj))
   // convert g to C(g) means mark context
   val rf_contexted=RegularFormula_context.RegularFormulaToRegularFormulaWithContext(prj_formula,updatevar.toInt)
   println(rf_contexted.rf.regularFormula_str)
   //val g=rf.regFormulaToVSetAutomaton();
   val c_g=rf_contexted.rf.regFormulaToVSetAutomaton()
   var result: Option[VSetAutomaton] = None
   println("number of variables : "+c_g.V.size)
    println("variables : "+c_g.V.toString())
  //get context variables from c_g
   // we know that the formula is update formula which contains an update variable + context variables

  //keep only context variables
   var cvariables=c_g.V
   cvariables-=updatevar.toInt

  val Z_list = cvariables
  //if context variables are empty set then return none as being durable by definition
  if (Z_list.size==0) return true

  val Max_var = c_g.V.max

  val X = Max_var + 1 //SVar
  val Y = Max_var + 2 //SVar

  val c_g_renamed_x = c_g.rename(us.updateVar.toInt, X)

  val c_g_renamed_x_projected = c_g_renamed_x.π(SVars[SVar](X))

   val alen_rel_list=load_allen_relationships(X,Y)
  // is the disjunction of the fifth through the 13th basic relationships
  var allen_list_to_check=List("0Overlaps1.csp","0during1.csp","0finishes1.csp","0starts1.csp","1finishedby0.csp","1Overlapby0.csp",
     "1startby0.csp","1contans0.csp","0equal1_1.csp","0equal1_2.csp")


   var res:Option[VSetAutomaton]= None

   for (z <- Z_list ) {


     val c_g_renamed_y = c_g.rename(z, Y)
     val c_g_renamed_y_projected = c_g_renamed_y.π(SVars[SVar](Y))
     //Γ(XtY)
     for (a <-allen_list_to_check) {
       println("Checking allen: "+a)
       println("variable: "+z)
       var param1=c_g_renamed_x_projected.⋈(alen_rel_list.get(a).get)
       if(param1!=None)
         res = param1.get.⋈(c_g_renamed_y_projected)
       if (res != None) {
         println("not durable!")
         return false
       }

     }

   }
  true
}


/**
 * @note This fuction verify dependency of E on the current update formula and its  nabla
 * I did not implement this in a genral form to be able to optimize knowing that the update varibale
 * encloses all other vars and we do not need to go through all the vars in update formula
 * @return true if E depends on update formula or nabla, false otherwise
 */
def  depends(E:RegularFormula, E_contexted:RegularFormula_context, fw:BufferedWriter):Boolean={
  fw.write("starting depends function \n")
  val rf_update= this.us.update_regularFormula
  val updatevar=this.us.updateVar
  val update_vset=rf_update.regFormulaToVSetAutomaton();
  fw.write("Build nabla .... \n")
  var start = java.lang.System.currentTimeMillis
  val nabla_vset= this.us.nabla()
  var stop = java.lang.System.currentTimeMillis

  fw.write("Time elapsed for constructing nabla and getting coresponding vset " + formatTime(start, stop) + ".\n")
  fw.flush()
  // convert E to C(E) means mark context
 // val E_contexted=RegularFormula_context.RegularFormulaToRegularFormulaWithContext(E)
  val E_contexted_vset=E_contexted.rf.regFormulaToVSetAutomaton()
 // println("the contexted extractor" +E_contexted.rf.regularFormula_str)
 // println("number of variables in extractor: "+ E_contexted_vset.V.size)
 // println(E_contexted_vset.V.toString())
  var result: Option[VSetAutomaton] = None

  //get context variables from E_contexted_vset
  val Z_list=E_contexted_vset.V

  //val X = (E_contexted_vset.V.max).max(updatevar.toInt) + 1
  val X = (E_contexted_vset.V.max).max(update_vset.V.max) + 1
  val Y = X + 1

  val update_vset_renamed_y = update_vset.rename(us.updateVar.toInt, Y)
  val update_vset_renamed_y_projected = update_vset_renamed_y.π(SVars[SVar](Y))

  val nabla_vset_renamed_y = nabla_vset.rename(us.updateVar.toInt, Y)
  val nabla_vset_renamed_y_projected = nabla_vset_renamed_y.π(SVars[SVar](Y))


  val alen_rel_list=load_allen_relationships(X,Y)
  // is the disjunction of the fifth through the 13th basic relationships
  var allen_list_to_check=List("0Overlaps1.csp","0during1.csp","0finishes1.csp","0starts1.csp","1finishedby0.csp","1Overlapby0.csp",
    "1startby0.csp","1contans0.csp","0equal1_1.csp","0equal1_2.csp")
  var diff_updtae :Long =0
  var diff_nabla :Long =0


  var res:Option[VSetAutomaton]= None

  for (z <- Z_list ) {


    val E_contexted_vset_rename_x = E_contexted_vset.rename(z, X)
    val E_contexted_vset_rename_x_projected = E_contexted_vset_rename_x.π(SVars[SVar](X))

    for (a <-allen_list_to_check) {
       println("Checking allen:"+a)
       println("variable: "+z)
      var param1=E_contexted_vset_rename_x_projected.⋈(alen_rel_list.get(a).get)
      if(param1!=None) {
        var start_update = java.lang.System.currentTimeMillis
        res = param1.get.⋈(update_vset_renamed_y_projected)
        var stop_update = java.lang.System.currentTimeMillis
        diff_updtae=diff_updtae+(stop_update-start_update)

      if (res != None) {
        fw.write("dependent! resulted from the update formula\n")
        fw.write("Time elapsed for testing dependency of update " + formatTime(diff_updtae) + ".\n")
        fw.write("Time elapsed for testing dependency of nabla " + formatTime(diff_nabla) + ".\n")
        fw.flush()
        return true
      }
        // verify the nabla
      else {
        var start_nabla = java.lang.System.currentTimeMillis
        res = param1.get.⋈(nabla_vset_renamed_y_projected)
        var stop_nabla = java.lang.System.currentTimeMillis
        diff_nabla=diff_nabla+(stop_nabla-start_nabla)

        if (res != None) {
          fw.write("dependent! resulted from nabla\n")
          fw.write("Time elapsed for testing dependency of update " + formatTime(diff_updtae) + ".\n")
          fw.write("Time elapsed for testing dependency of nabla " + formatTime(diff_nabla) + ".\n")
          fw.flush()
          return true
        }

      }
      }
      else {
         println("joining E with allen "+a+" is none ")
      }
    }

  }
  fw.write("not dependent!\n")
  fw.write("Time elapsed for testing dependency of update " + formatTime(diff_updtae) + ".\n")
  fw.write("Time elapsed for testing dependency of nabla " + formatTime(diff_nabla) + ".\n")
  fw.flush()
  false
}

  /***
   * Input:extraction automatonE,update expressionRepl(g,U)Output:Boolean Value
   * Precondition:g is well-defined,
   *              Repl(g,A) disjoint from E,
   *              SVars(E) ∩ SVars(g) =∅
   * @param E
   * @param g
   * @return
   */

  def verifyPseudoIrrelevantUpdates(E:RegularFormula,fw:BufferedWriter):Boolean=
  {
    var stop = java.lang.System.currentTimeMillis
    fw.write("checking precondition: if update is WellDefined \n")
    var start = java.lang.System.currentTimeMillis
    if (!this.isWellDefined())
    {
      //raise proper exception
      stop = java.lang.System.currentTimeMillis
      fw.write("g is not well defined \n")
      fw.write("Time elapsed for well defined function: " + formatTime(start, stop) + ".\n")
      fw.write("........................................................................\n")
      fw.flush()
      return  false;
    }
    // check durability for the update spanner
    stop = java.lang.System.currentTimeMillis

    fw.write("g is  well defined \n")
    fw.write("Time elapsed for well-defined function: " + formatTime(start, stop) + ".")
    fw.write("\n")
    fw.write("checking precondition: Whether update is durable\n")

    start = java.lang.System.currentTimeMillis
    if(!this.durable())
    {
      stop = java.lang.System.currentTimeMillis
      fw.write("update is not durable\n")
      fw.write("Time elapsed for durable function: " + formatTime(start, stop) + "."+"\n")
      fw.write(".......................................................................\n")
      fw.flush()
      //check pseudo relevancy
      return false
    }
    stop = java.lang.System.currentTimeMillis
    fw.write("update is  durable\n")
    fw.write("Time elapsed for durable  function: " + formatTime(start, stop) + ".\n")
    fw.write("................................................................\n")

    //check pseudo relevanc

   // println("check dependency of extractor on update and nabla.....")
    fw.write("get contextual formula...\n")
  //  fw.flush()
    start = java.lang.System.currentTimeMillis
    // convert E to C(E) means mark context
    val E_contexted=RegularFormula_context.RegularFormulaToRegularFormulaWithContext(E)
    stop = java.lang.System.currentTimeMillis
    fw.write("Time elapsed for contextualization: " + formatTime(start, stop) + ".\n")
    fw.write("Verify Pseudo-irrelevancy of Update Spanner\n")
    start = java.lang.System.currentTimeMillis
    if(depends(E, E_contexted,fw))
    {stop = java.lang.System.currentTimeMillis
      fw.write("\n")
      fw.write("................................................................\n")
      fw.write("update is not pseudo-irrelevant \n")
      fw.write("Time elapsed for the dependency test of psedu-irrelavancy check is: " + formatTime(start, stop) + ".\n")
      fw.flush()
     false
    }
    else {
      stop = java.lang.System.currentTimeMillis
      fw.write("................................................................\n")
      fw.write("Time elapsed for the dependency test of psedu-irrelavancy check is: " + formatTime(start, stop) + ".\n")
      fw.write("First test was successful: continue Verify Pseudo-irrelevancy of Update Spanner\n")
      fw.write("with check conflicting characters.....\n")


      start = java.lang.System.currentTimeMillis
      /**check conflicting characters*/
      // get all expressions enclosed by update_var so this function return set of update_var{Regular expression}
      val rf_update= this.us.update_regularFormula
      val update_var=this.us.updateVar
      var upvar_expression_set=getSetOFExpressionInUpdateVar(rf_update)
      val replacmentText=this.us.replacementSpecification
      // get variables that are used in replacment text we can have zero or more
      var referencedVars=psdIrVerifier_regex.getVars(replacmentText)
      var replacmentConstantParts=psdIrVerifier_regex.getConstantPart(replacmentText)
     // get set of all sigma^ from the expression tree
      var sigmaHat_set=getSigmaHat(E_contexted)
      for (s_hat<-sigmaHat_set)
      {
        //if(s_hat!=".")
        {
          var badChars = getComplementCharacterSet(new RegularFormula(s_hat))
          // if there is a badchar in the constant part of replacment we call it not psudu irrelevant
          var constantRange = getRange(replacmentConstantParts)
          for (c <- constantRange) {
            for (b <- badChars) {
              if (overlap(c, b)) {
                stop = java.lang.System.currentTimeMillis

                fw.write("\n")
                fw.write("replacement text has bad characters range is: " + b._1.toChar + "," + b._2.toChar + ". The update is not pseudo relevant!\n")
                fw.write("Time elapsed for the the second test of psedu-irrelavancy  is: " + formatTime(start, stop) + ".\n")
                fw.flush()
                return false


              }
            }

          }
          if (referencedVars.size == 0) {
            // there must not be any bad character inside upvar_expression
            for (e <- upvar_expression_set) {
              if (forbidenCharExist(e, badChars)) {
                stop = java.lang.System.currentTimeMillis
                fw.write("bad character inside upvar_expression there is no reference variable \n")
                fw.write(" The update is not pseudo relevant!\n")
                fw.write("Time elapsed for the the second test of psedu-irrelavancy is: " + formatTime(start, stop) + ".\n")
                fw.flush()
                // not psedu irrelevant
                // println("The update is not pseudo relevant! ")

                return false
              }
            }

          }


          // there is varibale refrenced in replacemnt text
          else {

            // as we traverese tree if we see a bad char we need to make sure that a variable that is bringing it is referneced
            // there must not be any bad character inside upvar_expression
            for (e <- upvar_expression_set) {
              if (forbidenCharExistNoRefVar(e, badChars, referencedVars, update_var.toInt)) {
                // not psedu irrelevant
                stop = java.lang.System.currentTimeMillis
                fw.write("bad character inside upvar_expression although there is  referenced variables\n")
                fw.write(" The update is not pseudo relevant!\n")
                fw.write("Time elapsed for the the second test of psedu-irrelavancy is: " + formatTime(start, stop) + ".\n")
                // not psedu irrelevant
                //              println("The update is not pseudo relevant! ")
                //              System.out.print("****************************************************************************\n")
                //              System.out.println("Time elapsed for check conflicting characters check is: " + formatTime(start, stop) + ".")
                fw.flush()
                return false
              }
            }


          }
        }
      }

     // println("The update is pseudo relevant! Horaay")
      stop = java.lang.System.currentTimeMillis
      fw.write("................................................................\n")
      fw.write("The update is pseudo relevant!\n")
      fw.write("Time elapsed for the second test of psedu-irrelavancy is: " + formatTime(start, stop) + ".\n")
      fw.write("---------------------------------------------------------------------------------\n")
      fw.flush()
      true
    }

  }


  private def forbidenCharExist(regularFormula: RegularFormula, badChars:scala.collection.mutable.HashSet[(Int,Int)]):Boolean={

    traverseInsideUpdateVar(regularFormula.regFormulaToExpressionTree().root,badChars)

  }


  private def forbidenCharExistNoRefVar(regularFormula: RegularFormula,
                                        badChars:scala.collection.mutable.HashSet[(Int,Int)],
                                        refVars: scala.collection.mutable.HashSet[Int],
                                        update_var: Int):Boolean={
    var seenVars= scala.collection.mutable.HashSet[Int]()
    seenVars+=update_var
    traverseInsideUpdateVarNoRefVar(regularFormula.regFormulaToExpressionTree().root,badChars,refVars,seenVars )

  }
/**
 * return true if found a badcharacter
 * */
private def traverseInsideUpdateVar(rt: Node,  badChars:scala.collection.mutable.HashSet[(Int,Int)]):Boolean={

  if(rt.value=="~" ) {
    var excluded = ArrayBuffer[Int]()
    // get leaves
    var leaves= getLeaves(rt)
    // remove sigma
    leaves-="."
    for(l <-leaves){
      if(l(0)=='\\'){
        // if we have something like \\. to show period
        var ch=RegularFormula.complexCharEncoding(l)
        excluded +:=ch
      }
      else{
        excluded +:=l.toInt
      }

    }
    var existingChars=RegularFormula.toRange(excluded)

    // investigate overlap with bad characters
    for(existingchar <- existingChars)
      for(badchar <- badChars)
      {
        if( overlap((existingchar._1.toInt, existingchar._2.toInt),badchar))
        { return true}
      }
  }
  else if(rt.value.indexOf("⊢{")> -1){
    if(rt.right!= null)
      return traverseInsideUpdateVar(rt.right,badChars)
    else if(rt.left!= null)
      return traverseInsideUpdateVar(rt.left,badChars)
  }
  else if (rt.value=="()") {
    if(rt.right!= null)
      return traverseInsideUpdateVar(rt.right,badChars)
    else if(rt.left!= null)
      return traverseInsideUpdateVar(rt.left,badChars)
  }
  else if(rt.value=="$") {
    var result_l=traverseInsideUpdateVar(rt.left,badChars)
    var result_r=traverseInsideUpdateVar(rt.right,badChars)
    if(result_l || result_r )
      return true
    else return false
  }
  else if(rt.value=="|"){

    var result_l=traverseInsideUpdateVar(rt.left,badChars)
    var result_r=traverseInsideUpdateVar(rt.right,badChars)
    if(result_l || result_r )
      return true
    else return false


  }
  else if(rt.value=="*"){
    if(rt.right!= null)
      return traverseInsideUpdateVar(rt.right,badChars)
    else if(rt.left!= null)
      return traverseInsideUpdateVar(rt.left,badChars)
  }
  else{
// we got to a leaf
var existingChars= getRange( rt.value)
    // investigate overlap with bad characters
    for(existingchar <- existingChars)
      for(badchar <- badChars) {
        if( overlap(existingchar,badchar ))
        { return true}
      }
    return false
  }
  throw new RuntimeException("\" error expression tree traverse ... ")

}

  /**
   * return true if found a badcharacter with no reference varibale
   * */
  private def traverseInsideUpdateVarNoRefVar(rt: Node, badChars:scala.collection.mutable.HashSet[(Int,Int)],
                                              refVars: scala.collection.mutable.HashSet[Int],
                                              seenVars: scala.collection.mutable.HashSet[Int]):Boolean={



    if(rt.value=="~" ) {
      var excluded = ArrayBuffer[Int]()
      // get leaves
      var leaves= getLeaves(rt)
      // remove sigma
      leaves-="."
      for(l <-leaves){
        if(l(0)=='\\'){
          // if we have something like \\. to show period
          var ch=RegularFormula.complexCharEncoding(l)
          excluded +:=ch
        }
        else{
          excluded +:=l.toCharArray.head.toInt
        }

      }
      var existingChars=RegularFormula.toRange(excluded)

      // investigate overlap with bad characters
      for(existingchar <- existingChars)
        for(badchar <- badChars)
        {
          if( overlap((existingchar._1.toInt, existingchar._2.toInt),badchar))
          {
            // there is no variable that contains this character and also referenced
            if(seenVars.intersect(refVars).size==0)
            return true // badcharacter with no reference var exist
          }
        }
    }
    else if (rt.value=="()") {
      if(rt.right!= null)
        return traverseInsideUpdateVarNoRefVar(rt.right,badChars,refVars,seenVars)
      else if(rt.left!= null)
        return traverseInsideUpdateVarNoRefVar(rt.left,badChars,refVars,seenVars)
    }
    else if(rt.value=="$") {
      var result_l=traverseInsideUpdateVarNoRefVar(rt.left,badChars,refVars,seenVars)
      var result_r=traverseInsideUpdateVarNoRefVar(rt.right,badChars,refVars,seenVars)
      if(result_l || result_r )
        return true
      else return false
    }
    else if(rt.value=="|"){

      var result_l=traverseInsideUpdateVarNoRefVar(rt.left,badChars,refVars,seenVars)
      var result_r=traverseInsideUpdateVarNoRefVar(rt.right,badChars,refVars,seenVars)
      if(result_l || result_r )
        return true
      else return false


    }
    else if(rt.value=="*"){
      if(rt.right!= null)
        return traverseInsideUpdateVarNoRefVar(rt.right,badChars,refVars,seenVars)
      else if(rt.left!= null)
        return traverseInsideUpdateVarNoRefVar(rt.left,badChars,refVars,seenVars)
    }
    else if(rt.value.indexOf("⊢{")> -1){
//  example ⊢{34}
      seenVars+= rt.value.substring(rt.value.indexOf("{")+1, rt.value.indexOf("}")).toInt
      if(rt.right!= null)
        return traverseInsideUpdateVarNoRefVar(rt.right,badChars,refVars,seenVars)
      else if(rt.left!= null)
        return traverseInsideUpdateVarNoRefVar(rt.left,badChars,refVars,seenVars)
    }
    else{
      // we got to a leaf

      var existingChars= getRange( rt.value)
      // investigate overlap with bad characters
      for(existingchar <- existingChars)
        for(badchar <- badChars) {
          if( overlap(existingchar,badchar ) && seenVars.intersect(refVars).size==0)
          { return true}
        }


    }
    return false

    throw new RuntimeException("\" error expression tree traverse ... ")

  }


  private def getComplementCharacterSet(sigmahat_regularFormula:RegularFormula):scala.collection.mutable.HashSet[(Int,Int)]={
  var tree= sigmahat_regularFormula.regFormulaToExpressionTree()

      traverseToGetComplement(tree.root)


  }


  /**
   * This function returns complement of input as a set of ranges of encodings
   * For instance complement of b will be [minvalue,a.toint] and [c.toint, maxvalue]
   * range is inclusive means (a, l) both a and l are in the range
   * */
  private def traverseToGetComplement(rt:Node):scala.collection.mutable.HashSet[(Int,Int)]={
   var complement_char_set=scala.collection.mutable.HashSet[(Int,Int)]()

   //if sigmaHat is an exclusion expression like Sigma~a~b~g do following
   if(rt.value=="~" ) {

      // get leaves
      var leaves= getLeaves(rt)
      // remove sigma
         leaves-="."
        for(l <-leaves){
          if(l(0)=='\\'){
            // if we have something like \\. to show period
            var ch=RegularFormula.complexCharEncoding(l)
            complement_char_set+=((ch,ch))
          }
          else{
            // create a range like [a.toInt,a.toInt]
            complement_char_set+=((l.toCharArray.head.toInt,l.toCharArray.head.toInt))

          }

        }



 }

   else if(rt.value=="|") {
// demorgan law complemnt(A U B)=complemt(A) cap complemt(B)
     var complement_char_set_left= traverseToGetComplement(rt.left)
     var complement_char_set_right=traverseToGetComplement(rt.right)
     // find the intersection of to set of ranges
     for(left_range <- complement_char_set_left)
       for (right_range <- complement_char_set_right)
         {
           // first check if the overlap if not there is no intersection
           if(overlap(left_range,right_range))
             {
               // they overlap so find the intersection
               complement_char_set+=rangeIntersection(left_range,right_range)

             }
         }
   }

   else if(rt.value=="()") {
     if (rt.left!= null) complement_char_set= traverseToGetComplement(rt.left)
     if (rt.right!= null)  complement_char_set= traverseToGetComplement(rt.right)
   }
     // there is no star or $ inside simaHat
   else{
     // we are definitly in a leaf if not raise run tie exception
     if(rt.left!= null || rt.right !=null )
       throw new RuntimeException("\" we should be in a leaf here and we are not ... ")

     //process the leaves now
     complement_char_set= getComplemtRange(rt.value)



   }


    complement_char_set
 }

  private def getComplemtRange(value: String):scala.collection.mutable.HashSet[(Int,Int)]={
    var complement_char_set=scala.collection.mutable.HashSet[(Int,Int)]()

    if(value=="."){
      // complement set is empty
      complement_char_set
    }
   else if(value.matches("\\\\p\\{Lower\\}")){

    // complement of ('a', 'z') has two ranges
      complement_char_set+=((Character.MIN_VALUE,'a'.toInt-1 ))
       complement_char_set+=(('z'.toInt+1, Character.MAX_VALUE ))
    }
    else  if (value.matches("\\\\p\\{Upper\\}")){
      // complement of ('A', 'Z') has two ranges

      complement_char_set+=((Character.MIN_VALUE,'A'.toInt-1 ))
      complement_char_set+=(('Z'.toInt+1, Character.MAX_VALUE ))

    }
    else if (value.matches("\\\\p\\{Punct\\}")){
      //!:;`.,
      complement_char_set+=((Character.MIN_VALUE,'!'.toInt-1 ))
      complement_char_set+=(('!'.toInt+1,','.toInt-1 ))
      complement_char_set+=((','.toInt+1,'.'.toInt-1 ))
      complement_char_set+=(('.'.toInt+1,':'.toInt-1 ))
      complement_char_set+=((';'.toInt+1,'`'.toInt-1 ))
      complement_char_set+=(('`'.toInt+1,Character.MAX_VALUE))
    }
    else if (value.matches("\\\\d"))
    {

      complement_char_set+=((Character.MIN_VALUE,'0'.toInt-1 ))
      complement_char_set+=(('9'.toInt+1,Character.MAX_VALUE))
    }
//    else if (value.matches("\\\\e"))
//    { we do not have epsilon as input in Java so we
    //    }
    else if(value.matches("\\\\n")){
      //new line
      complement_char_set+=((Character.MIN_VALUE,'\n'.toInt-1 ))
      complement_char_set+=(('\n'.toInt+1,Character.MAX_VALUE))
    }
    else if(value.matches("\\\\\\.")){
      complement_char_set+=((Character.MIN_VALUE,'.'.toInt-1 ))
      complement_char_set+=(('.'.toInt+1,Character.MAX_VALUE))

    }
    else if(value.matches("\\\\\\(")){

      complement_char_set+=((Character.MIN_VALUE,'('.toInt-1 ))
      complement_char_set+=(('('.toInt+1,Character.MAX_VALUE))

    }
    else if(value.matches("\\\\\\)")){

      complement_char_set+=((Character.MIN_VALUE,')'.toInt-1 ))
      complement_char_set+=((')'.toInt+1,Character.MAX_VALUE))
    }
    else if(value.matches("\\[\\d-\\d\\]")){


      val rangePatternS = "\\[(\\d)-(\\d)\\]"
      val rangePattern = rangePatternS.r

      val rangePattern(min, max) = value
      val minA = min.toCharArray
      val minC = minA(0)
      val maxA = max.toCharArray
      val maxC = maxA(0)

      complement_char_set+=((Character.MIN_VALUE,minC.toInt-1 ))
      complement_char_set+=((maxC.toInt+1,Character.MAX_VALUE))

    }
    else if(value.matches("\\\\\\[(\\\\m)-(.)\\]")){

      val rangePatternS = "\\\\\\[(\\\\m)-(.)\\]"
      val rangePattern = rangePatternS.r

      val rangePattern(min, max) = value
      val maxA = max.toCharArray
      val maxC = maxA(0)
      complement_char_set+=((maxC.toInt+1, Character.MAX_VALUE ))

    }

    else if(value.matches("\\\\\\[(.)-(\\\\M)\\]")){
      val rangePatternS = "\\\\\\[(.)-(\\\\M)\\]"
      val rangePattern = rangePatternS.r
      val rangePattern(min, max) = value
      val minA = min.toCharArray
      val minC = minA(0)
      complement_char_set+=((Character.MIN_VALUE,minC.toInt-1 ))
    }


    else {
      // it should be a normal character like b
      if(value.length>1){throw new RuntimeException("\" unknown character ... ")}
      else {

        complement_char_set+=((Character.MIN_VALUE,value.charAt(0).toInt-1 ))
        complement_char_set+=((value.charAt(0).toInt+1,Character.MAX_VALUE))
      }

    }

    }


// return range
private def getRange(values: scala.collection.mutable.HashSet[Char]):scala.collection.mutable.HashSet[(Int,Int)]={
  var char_range=scala.collection.mutable.HashSet[(Int,Int)]()
  for(v <- values){
    char_range+=((v.toInt, v.toInt))

  }
  char_range


}
// return range for a character
  private def getRange(value: String):scala.collection.mutable.HashSet[(Int,Int)]={
    var char_range=scala.collection.mutable.HashSet[(Int,Int)]()
    if(value.matches("\\\\p\\{Lower\\}")){

      //  ('a', 'z')
      char_range+=(('a'.toInt,'z'.toInt ))
    }
    else if (value.matches("\\\\p\\{Upper\\}")){
      // ('A', 'Z')

      char_range+=(('A'.toInt,'Z'.toInt))

    }
    else if (value.matches("\\\\p\\{Punct\\}")){
      //!:;`.,
      char_range+=(('!'.toInt,'!'.toInt ))
      char_range+=((':'.toInt,':'.toInt ))
      char_range+=((';'.toInt,';'.toInt ))
      char_range+=(('`'.toInt,'`'.toInt ))
      char_range+=(('.'.toInt,'.'.toInt ))
      char_range+=((','.toInt,','.toInt ))
    }
    else if (value.matches("\\\\d"))
    {
      char_range+=(('0'.toInt,'9'.toInt))
    }
    //    else if (value.matches("\\\\e"))
    //    { we do not have epsilon as input in Java so we
    //    }
    else if(value.matches("\\\\n")){
      //new line
      char_range+=(('\n'.toInt,'\n'.toInt))

    }
    else if(value.matches("\\\\\\.")){
      char_range+=(('.'.toInt,'.'.toInt))


    }
    else if(value.matches("\\\\\\(")){
      char_range+=(('('.toInt,'('.toInt))


    }
    else if(value.matches("\\\\\\)")){

      char_range+=(('('.toInt,'('.toInt))
    }
    else if(value.matches("\\[\\d-\\d\\]")){

      val rangePatternS = "\\[(\\d)-(\\d)\\]"
      val rangePattern = rangePatternS.r

      val rangePattern(min, max) = value
      val minA = min.toCharArray
      val minC = minA(0)
      val maxA = max.toCharArray
      val maxC = maxA(0)
      char_range+=((minC.toInt,maxC.toInt))
    }
    else if(value.matches("\\\\\\[(\\\\m)-(.)\\]")){

      val rangePatternS = "\\\\\\[(\\\\m)-(.)\\]"
      val rangePattern = rangePatternS.r
      val rangePattern(min, max) = value
      val maxA = max.toCharArray
      val maxC = maxA(0)
      char_range+=((Character.MIN_VALUE, maxC))
    }

    else if(value.matches("\\\\\\[(.)-(\\\\M)\\]")){
      val rangePatternS = "\\\\\\[(.)-(\\\\M)\\]"
      val rangePattern = rangePatternS.r
      val rangePattern(min, max) = value
      val minA = min.toCharArray
      val minC = minA(0)
      char_range+=((minC,Character.MAX_VALUE ))
    }
    // it should be a normal character like b

    else {
      if (value.length > 1) {
        throw new RuntimeException("\" unknown character ... ")
      }
      else {

        char_range += ((value.charAt(0).toInt, value.charAt(0).toInt))
      }
    }
  }



  /**
   * the overlap */
  private def overlap(f:(Int, Int),s:(Int, Int)):Boolean={
    if((s._1 <= f._1 && f._1<= s._2) || (f._1 <= s._1 && s._1<= f._2)) {
    return true

    }
    false
  }



  private def rangeIntersection(f:(Int, Int),s:(Int, Int)):(Int, Int)={

    if(s._1 <= f._1 && f._1<= s._2)  {
      return (f._1, Math.min(f._2,s._2))

    }

   if (f._1 <= s._1 && s._1<= f._2)
     {  return (s._1, Math.min(f._2,s._2))}

    throw new RuntimeException("\" cannot compute range intersection... ")
  }
  private def getLeaves(rt:Node):scala.collection.mutable.HashSet[String]={
    var leave_set=scala.collection.mutable.HashSet[String]()

    if(rt.left==null && rt.right==null)
    {
      leave_set+=rt.value

    }
    if(rt.left != null )
      {
        leave_set=leave_set.union(getLeaves(rt.left))

      }
     if( rt.right != null)
      {
        leave_set=leave_set.union(getLeaves(rt.right))

      }

    leave_set

  }

 private def getSetOFExpressionInUpdateVar(rf_update_formula:RegularFormula):scala.collection.mutable.HashSet[RegularFormula]={
   var tree=rf_update_formula.regFormulaToExpressionTree()
   traverseTreeForSetOFExpressionInUpdateVar(tree.root)

 }


  private def traverseTreeForSetOFExpressionInUpdateVar(rt:Node):scala.collection.mutable.HashSet[RegularFormula]={
    var mySet=scala.collection.mutable.HashSet[RegularFormula]()
    //since update variable contain all other variables so the first one is update variable
    if(rt.value.indexOf("⊢{") > -1)
    {
      if(rt.left!= null )
      mySet+= new RegularFormula( ExpressionTree.toExpression(rt.left))
      else if(rt.right!= null) mySet+= new RegularFormula( ExpressionTree.toExpression(rt.right))
      else throw new RuntimeException("\" error update expression tree .. ")
      mySet
    }
    else
    {
      if(rt.left!= null)
      mySet=mySet.union(traverseTreeForSetOFExpressionInUpdateVar(rt.left))

      if(rt.right != null)
      mySet=mySet.union(traverseTreeForSetOFExpressionInUpdateVar(rt.right))
      mySet
    }

  }

private def getSigmaHat(E_context: RegularFormula_context):scala.collection.mutable.HashSet[String]={

  val tree=E_context.rf.regFormulaToExpressionTree();
  traverseForSigmaHat(tree.root)

}

  private def traverseForSigmaHat(rt:Node):scala.collection.mutable.HashSet[String]={
    var mylist=scala.collection.mutable.HashSet[String]()

    if(rt.value=="*")
     {
       if(rt.left != null)
         {

           mylist+= ExpressionTree.toExpression(rt.left)
         }
       if(rt.right != null)
       {

         mylist+= ExpressionTree.toExpression(rt.right)
       }
       mylist
     }

   else if(rt.value.indexOf("{") > -1)
    {
      return mylist
    }
    else if (rt.value=="$")
    {
      mylist=mylist.union(traverseForSigmaHat(rt.left))
      mylist=mylist.union(traverseForSigmaHat(rt.right))
      mylist
    }
    else {
      throw new RuntimeException("\" error in finding sigma hat... ")
    }

  }


  /**
 * @Note read the file containg regular formulas and verify each one
 * regular formula arein a line
 * */
/**
 * program are save in .prg files
 * program file format is similar to aqls format:
 * E1= expression 1
 * E2= expression2
 * -
 * E3=E1 ⋈ E2
 * */
//def verifyProgram(file:String):Boolean={
//
//  import scala.io.Source
//
//  val source = Source.fromFile(file, "UTF-8")
//  val lineIterator = source.getLines
//  var start = java.lang.System.currentTimeMillis
//  var stop = java.lang.System.currentTimeMillis
//
//
//  //check preconditions
//  System.out.print("\n")
//  println("checking precondition: Whether update is WellDefined ")
//  if (!this.isWellDefined())
//  {
//    //raise proper exception
//    println("g is not well defined")
//    stop = java.lang.System.currentTimeMillis
//    System.out.print("****************************************************************************\n")
//    System.out.println("Time elapsed for well defined function: " + formatTime(start, stop) + ".")
//    System.out.print("\n")
//    return  false;
//  }
//  // check durability for the update spanner
//
//  println("update is well defined!")
//   stop = java.lang.System.currentTimeMillis
//  System.out.print("****************************************************************************\n")
//  System.out.println("Time elapsed for well defined function: " + formatTime(start, stop) + ".")
//  System.out.print("\n")
//  println("checking precondition: Whether update is durable ")
//  start = java.lang.System.currentTimeMillis
//  if(!this.durable())
//  {
//    println("update is not durable")
//    stop = java.lang.System.currentTimeMillis
//    System.out.print("****************************************************************************\n")
//    System.out.println("Time elapsed for durable defined function: " + formatTime(start, stop) + ".")
//
//    System.out.print("****************************************************************************\n")
//    //check pseudo relevancy
//    System.out.print("\n")
//    return false
//  }
//  println("update is  durable")
//  stop = java.lang.System.currentTimeMillis
//  System.out.print("****************************************************************************\n")
//  System.out.println("Time elapsed for durable defined function: " + formatTime(start, stop) + ".")
//
//  System.out.print("****************************************************************************\n")
//  //check pseudo relevancy
//  System.out.print("\n")
//
//
//  for (line <- lineIterator){
//
//      if(!verifyPseudoIrrelevantUpdates(new RegularFormula(line)))
//        {
//          println("The update is NOT  pseudo relevant! ")
//          stop = java.lang.System.currentTimeMillis
//          System.out.print("****************************************************************************\n")
//          System.out.println("Time elapsed for the whole psedu irrelavancy check of the program is: " + formatTime(start, stop) + ".")
//
//          return false}
//
//}
//  println("The update is pseudo relevant! ")
//  stop = java.lang.System.currentTimeMillis
//  System.out.print("****************************************************************************\n")
//  System.out.println("Time elapsed for the whole psedu irrelavancy check of the program is: " + formatTime(start, stop) + ".")
// true
//
//}


  def formatTime(start:Long, stop:Long):String = {

    val diff = stop - start
    var x = diff / 1000
    val seconds = x % 60
    x /= 60
    val minutes = x % 60
    x /= 60
    val hours = x % 24

    val res = "" + hours + "h" + minutes + "m" + seconds + "s"

    res
  }



  def formatTime(diff:Long):String = {


    var x = diff / 1000
    val seconds = x % 60
    x /= 60
    val minutes = x % 60
    x /= 60
    val hours = x % 24

    val res = "" + hours + "h" + minutes + "m" + seconds + "s"

    res
  }

}

//object main {
//
//
//
//  def main(args: Array[String]):Unit = {
//
//
//
//
//    var str="er$2$tr$4$ff"
//    var v=psdIrVerifier_regex.getVars(str)
//    var c=psdIrVerifier_regex.getConstantPart(str)
//    System.out.println("")
//  }
//}


object  psdIrVerifier_regex{
  def getVars(str: String):scala.collection.mutable.HashSet[Int]={
    var var_ref_set=scala.collection.mutable.HashSet[Int]()
    var i=0
    while(i<str.length){
      if (str(i)=='$')
      {
        var j=i+1
        var var_name=""
        while(str(j)!='$')
        {
          var_name=var_name+str(j)
          j=j+1
        }
        var_ref_set+= var_name.toInt
        i=j+1
      }
      else {
        i=i+1
      }
    }
    return var_ref_set



  }

  def getConstantPart(str: String):scala.collection.mutable.HashSet[Char]={
    var constPart_set=scala.collection.mutable.HashSet[Char]()
    var i=0
    while(i<str.length){
      if (str(i)=='$')
      {
        var j=i+1
        while(str(j)!='$') {
          j=j+1

        }
        i=j+1
      }
      else {
        constPart_set +=str(i)
        i=i+1
      }
    }
    return constPart_set

  }





}
