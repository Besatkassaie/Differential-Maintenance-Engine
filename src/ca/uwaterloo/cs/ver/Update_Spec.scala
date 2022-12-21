package ca.uwaterloo.cs.ver

import be.ac.ulb.arc.core.{State, Transition, VSetAutomaton}

import scala.be.ac.ulb.arc.core.RegularFormula
import scala.collection.mutable
import be.ac.ulb.arc.core.{OperationsTransition, OrdinaryTransition, Range, RangeTransition, SVOp, State, Transition, VSetAutomaton, ⊢, ⊣}
import be.ac.ulb.arc.vsetold.VSetAFileReader

import scala.collection.immutable.{HashSet => SVOps}
import scala.collection.immutable.{HashSet => StateSet}
import scala.collection.immutable.{HashSet => SVars}
import scala.collection.immutable.{HashSet => TransitionFunction}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.{Int => SVar}


/**
 @note This class has everything we need to update a document:
 a regularformula to match documents for update and is well-defined
 a replacement specification such as "test$2$sdfd$3$sdf$4$sfs" where numbers are variables in the update regular formula, confined to $ on both sides,
 a function to build diamond(U)
 a function to build nabla(g, U)
 */
class Update_Spec (val update_regularFormula : RegularFormula,val updateVar:String, val replacementSpecification:String){





//Input: update expression $Repl(g,U)$, update variable $x$ which are class members
  def nabla( ):VSetAutomaton={
    println("creating post update expression.....")
    var δ = new TransitionFunction[Transition[State]]

    var Q = new StateSet[State]
    var V = new SVars[SVar]
    //intial state
    val q0 = new State
    Q = Q + q0

    //final state
    val qf = new State
    Q = Q + qf

  //  create empty vset
 // var nabla_vset=new VSetAutomaton(Q, q0, qf, V, δ)
  var nabla_vset=new VSetAutomaton(Q, q0, qf, V, δ)
  var disjunct_list=this.update_regularFormula.getDisjuntiveFormList(updateVar)
var loop=0
    for (g_i<-disjunct_list)
      {
      var g_str=g_i.regularFormula_str
/* get the subexpressions  on the left/right of the update variable from corresponding expression tree  */

      var expressiontree=g_i.regFormulaToExpressionTree()

        var (left_expresion, right_expresion) =expressiontree.getLeftRightExpressionTree(expressiontree.root,updateVar)
       var l_vset= new RegularFormula(left_expresion).regFormulaToVSetAutomaton()
       var r_vset=new RegularFormula(right_expresion).regFormulaToVSetAutomaton()
        var dimnd_vset=diamond(g_i).regFormulaToVSetAutomaton()
        var temp= g_i.concatination(l_vset, dimnd_vset).get
        var temp2=g_i.concatination(temp, r_vset).get
          // manage the first round
        if(loop==0)
           nabla_vset=temp2
        else
            nabla_vset=nabla_vset.∪(temp2).get
        loop=loop+1
}
    return nabla_vset

}
//todo make this private method
private def diamond(update_reg:RegularFormula):RegularFormula={
  println("creating diamond ....")
//get string representation
// val  regularFormula_str=this.update_regularFormula.regularFormula_str
// retrieve all back-references and their positions from U
var bkrefs = scala.collection.mutable.Map[String, List[Int]]().withDefaultValue(List())

bkrefs= getBackRefs()
//retrieve all sub-strings associated with  all backrefrences
var subexps = scala.collection.mutable.Map[String, List[String]]().withDefaultValue(List())
subexps=getSubExps(update_reg,bkrefs)

//append all sub-string, with a valid disjunction symbol  between every two sub-string
var subexps_appended = scala.collection.mutable.Map[String, String]()

subexps_appended=append_subexprs(subexps)

//substitute each bkref in replacementSpecification with corresponding string and convert to regular expression

toRegularExpression(update_reg,subexps_appended);


}

private def toRegularExpression(update_reg:RegularFormula,subexps_appended: scala.collection.mutable.Map[String, String]):RegularFormula={

var diamond_str=""
var i=0
while(i<replacementSpecification.length){


var current_char=replacementSpecification.charAt(i).toString

if(current_char!="$")
{
//check whether we have special character at hand if so add \ to it
  if(update_reg.specialChars(current_char))
    current_char="\\"+current_char
  //concat
  diamond_str=diamond_str+"$"+current_char
  i=i+1
}
else if(replacementSpecification.charAt(i).toString=="$"){
 var varname=""
 i=i+1
while (replacementSpecification.charAt(i).toString!="$" & i<replacementSpecification.length){
  varname=varname+replacementSpecification.charAt(i).toString
  i=i+1
}
if(i<replacementSpecification.length)
i=i+1
//get subexpression and concat
diamond_str=diamond_str+"$"+ subexps_appended(varname)
}


}

if(diamond_str.charAt(0)=='$') {
diamond_str=diamond_str.substring(1)
 //wrap with update variable
  diamond_str="⊢{"+this.updateVar +"}"+diamond_str+"⊣"
println("diamond_str "+diamond_str)
}
  new RegularFormula(diamond_str)
}


private def append_subexprs(subexps:scala.collection.mutable.Map[String, List[String]]):scala.collection.mutable.Map[String, String]={


var subexps_appended = scala.collection.mutable.Map[String, String]()
for((k,v)<-subexps){
var varname=k
var subexpressions=v
var appended_express=""
for(item<-v){
appended_express=appended_express+"|"+item
}
//is there is extra disjunction symbol in the begining remove it
if(appended_express.charAt(0)=='|')
appended_express=appended_express.substring(1)
// enclose by parenthesis and add to the list
appended_express="("+appended_express+")"
subexps_appended(varname)=appended_express

}
return subexps_appended
}

private  def getBackRefs():scala.collection.mutable.Map[String, List[Int]]={

var bkrefs = scala.collection.mutable.Map[String, List[Int]]().withDefaultValue(List())
//scan U and find back references
var i=0
while(i< replacementSpecification.length){
var chr=replacementSpecification.charAt(i)
if(chr!='$'){
i=i+1
}
else{
i=i+1
chr=replacementSpecification.charAt(i)
var varname=""

while (chr!='$' & i<replacementSpecification.length){
  varname=varname+chr.toString
  i=i+1
  chr=replacementSpecification.charAt(i)
}
if(chr!='$' & i==replacementSpecification.length){

  // there is a syntax error a varname has not closed by $
  throw new Exception("there is a syntax error in U: a varname has not closed by $")
}
else{
  bkrefs(varname)::=i-varname.length
  i=i+1}
}

}

return bkrefs
}

private def getSubExps(update_reg:RegularFormula,bkrefs:scala.collection.mutable.Map[String, List[Int]]):scala.collection.mutable.Map[String, List[String]]={


var subexps = scala.collection.mutable.Map[String, List[String]]().withDefaultValue(List())
val regularFormula_str=update_reg.regularFormula_str
//scan g for each variable start to end
for ((k,v) <- bkrefs){

var variables_operations = new mutable.Stack[String]
var expression= new mutable.Stack[Char]
var varname_U=k
var varname_g=""
var j=0
//show whether we are inside a backref variable inside=0 means outside
var inside=0
var current_char='''
while(j<regularFormula_str.length)
{
current_char=regularFormula_str.charAt(j)
"⊢{1"
if(current_char=='⊢' & inside==0)
{
//open variable
var (varname, end_pos)=getVarname(regularFormula_str,j)
varname_g=varname
j=end_pos+1


// if its a back references we need to keep the subexpression
if(varname_U==varname_g){
  //push into variables_operations if we care means we are inside a bakref variable
  variables_operations= variables_operations.push(varname_g)
  inside=1}

}
else if(current_char=='⊢' & inside==1)
{

// since the regularformula is function
// it cannot have the same var inside so
// in case a var is open we just need to push it and skip

var (varname, end_pos)=getVarname(regularFormula_str,j)
varname_g=varname
j=end_pos+1
//push into variables_operations
variables_operations= variables_operations.push(varname_g)
}
else if(current_char=='⊣' & inside==1)
{

if( variables_operations.head==varname_U)
{
  inside=0
  //empty the expression stack into subexps and keep

  var expr=getExpression(expression)
  subexps(k)::=expr
  variables_operations.pop()
  j=j+1
}
else {
  variables_operations.pop()
  j = j + 1
}
}
else  if(current_char=='⊣' & inside==0)
{
j=j+1

}
else if(current_char!='⊢' & current_char!='⊣' & inside==0){
j=j+1
}
else  if(current_char!='⊢' & current_char!='⊣' & inside==1){

expression=expression.push(current_char)
j=j+1
}



}






}
return subexps
}
private def getExpression(expressionstack:mutable.Stack[Char]):String={
var expr=""
while (!expressionstack.isEmpty){

expr=expressionstack.pop().toString+expr
}
return expr

}

private def getVarname(regformula:String ,position :Int ):(String,Int)={
var j=position
var varname_g=""
j = j + 1
var current_char = regformula.charAt(j)
if(current_char!='{') {
throw new Exception("there is a syntax error in g: a varname has not opened properly")
}
j = j + 1
//get the variable name
while(j<regformula.length & regformula.charAt(j)!='}') {
current_char=regformula.charAt(j)
varname_g = varname_g + current_char.toString
j=j+1

}

if(current_char!='}' & j==regformula.length) {
throw new Exception("there is a syntax error in g: a varname has not opened properly")
}
return (varname_g,j)
}



}


object Update_Spec{



  def getUpdateSpec(file:String):(String, String)={

    import scala.io.Source

    val source = Source.fromFile(file, "UTF-8")
    val lineIterator = source.getLines


    var line = lineIterator.next()
    val updatevariable=line
    line = lineIterator.next() // has -
    val replacment=lineIterator.next()
    (updatevariable,replacment)
  }
  def getFormulaFromFile(file:String):RegularFormula={
    import scala.io.Source

    val source = Source.fromFile(file, "UTF-8")
    val lineIterator = source.getLines
    var line = lineIterator.next()
    // we assume that there is one line continues without any break
    new RegularFormula(line)}


}