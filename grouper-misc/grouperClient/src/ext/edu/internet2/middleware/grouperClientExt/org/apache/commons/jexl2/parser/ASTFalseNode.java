/* Generated By:JJTree: Do not edit this line. ASTFalseNode.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl2.parser;

public
class ASTFalseNode extends JexlNode {
  public ASTFalseNode(int id) {
    super(id);
  }

  public ASTFalseNode(Parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=5c7ee1ac9277f16597926c1fed96d4bc (do not edit this line) */
