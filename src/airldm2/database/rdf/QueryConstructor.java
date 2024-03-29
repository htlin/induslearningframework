package airldm2.database.rdf;

import static airldm2.util.StringUtil.angleBracket;
import static airldm2.util.StringUtil.makeContextPart;
import static airldm2.util.StringUtil.triple;

import org.openrdf.model.URI;

import airldm2.core.rl.DiscreteType;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RbcAttribute;

public class QueryConstructor {

   protected RDFDataDescriptor mDesc;
   protected String mContextPart;
   
   public QueryConstructor(RDFDataDescriptor desc, String context) {
      mDesc = desc;
      mContextPart = makeContextPart(context);
   }
   
   protected String createInstanceType() {
      return triple(mDesc.getInstanceVar(), "a", angleBracket(mDesc.getTargetType()));
   }
   
   protected String createInstanceFilter(URI instance) {
      return "FILTER(" + mDesc.getInstanceVar() + "=" + angleBracket(instance) + ")";
   }
   
   protected String createAttributeGraph(RbcAttribute att) {
      return "FILTER(" + mDesc.getInstanceVar() + "=" + att.getGraphPattern().getInstanceVar() + ") "
         + att.getGraphPattern().getPattern();
   }

   protected String createValueFilter(RbcAttribute att, int valueIndex, String var) {
      DiscreteType valueType = (DiscreteType) att.getValueType();
      return valueType.makeFilter(var, valueIndex);
   }
   
   protected String createValueFilter(RbcAttribute att, int valueIndex) {
      return createValueFilter(att, valueIndex, att.getGraphPattern().getValueVar());
   }
   
   protected String createValueFilter(RbcAttribute att, String value, String var) {
      DiscreteType valueType = (DiscreteType) att.getValueType();
      return valueType.makeFilter(var, value);
   }
   
   protected String createValueFilter(RbcAttribute att, String value) {
      return createValueFilter(att, value, att.getGraphPattern().getValueVar());
   }

}
