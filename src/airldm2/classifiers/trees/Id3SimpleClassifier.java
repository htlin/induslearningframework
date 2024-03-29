/**
 * Id3SimpleClassifier.java<br>
 * An implementation of Decision Tree in Indus Learning Framework
 * 
 * HISTORY
 * 
 * 1: Vikas Bahirwani (April 2008) : First Cut
 * 
 * 2: Neeraj Koul (January 2009) : Added Support For Handling Missing
 * Values
 * 
 * 3: Neeraj Koul (Feburary 2009): Bug Fix so that option for missing value
 * is read correctly
 * 
 */

package airldm2.classifiers.trees;

import java.util.HashMap;
import java.util.Vector;

import weka.core.Utils;
import airldm2.classifiers.Classifier;
import airldm2.core.ISufficentStatistic;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.SSDataSource;
import airldm2.core.datatypes.relational.ColumnDescriptor;
import airldm2.core.datatypes.relational.SingleRelationDataDescriptor;
import airldm2.util.AttribValuePair;

/**
 * TODO Write class description here.
 * 
 * @author Vikas Bahirwani (TODO Write email id here)
 * @since Apr 8, 2008
 * @version $Date: 2008/08/04 16:06:30 $
 */
public class Id3SimpleClassifier extends Classifier {

   /*
    * (non-Javadoc)
    * 
    * @see
    * airldm2.classifiers.Classifier#buildClassifier(airldm2.core.LDInstances
    * )
    */
   /** The node's successors */
   private Id3SimpleClassifier[] m_Successors;

   private static int countQueries = 0;

   /** Attribute used for splitting. */
   private ColumnDescriptor m_Attribute;

   /** Class value if node is leaf. */
   private String m_ClassValue;

   /** Class distribution if node is leaf. */
   private double[] m_Distribution;

   boolean DEBUG = false;

   static SSDataSource dataSource;

   static int numOfClassLabels;

   static Vector<String> classLabels;

   static SingleRelationDataDescriptor dataDesc;

   static Vector<ColumnDescriptor> allAttributes;

   private static HashMap<ColumnDescriptor, String> mostLikelyValues = new HashMap<ColumnDescriptor, String>();

   static ColumnDescriptor classAttribute;

   // @Override
   public void buildClassifier(LDInstances instances) throws Exception {

      dataDesc = (SingleRelationDataDescriptor) instances.getDesc();
      dataSource = instances.getDataSource();
      allAttributes = dataDesc.getTableDesc().getColumns();

      // We assume last attribute is the classIndex
      classAttribute = allAttributes.lastElement();
      classLabels = classAttribute.getPossibleValues();
      numOfClassLabels = classLabels.size();

      // TODO check whether input contains only nominal values

      Vector<AttribValuePair> classifyingAttrib = new Vector<AttribValuePair>();
      AttribValuePair classAttribValue = new AttribValuePair();
      classAttribValue.setAttribName(classAttribute.getColumnName());
      classifyingAttrib.add(classAttribValue);

      // option too handle missing values. Read and store it . If read as
      // Utils.getFlag() in
      // recursion makeTree() it reads incorrect values after second
      // invocation
      boolean replaceMissingWithModeValue = Utils.getFlag("?", getOptions());

      boolean firstTime = true;
      makeTree(classifyingAttrib, firstTime, replaceMissingWithModeValue);
      Debug(Id3SimpleClassifier.countQueries + " Queries requested");
      System.out.println(Id3SimpleClassifier.countQueries
            + " Queries requested");
   }

   /**
    * 
    * @param usedAttribValueSet set of attribute-value pairs used to reach
    * the current node in the tree. (Even for an empty or null tree) The
    * first attribute of usedAttribValueSet should be the classifying
    * attribute with no values associated with it.
    * @param isFirsTime records if this is the first invocation of this
    * recusrion call
    * @param replaceMissingWithModeValue records the option to handle
    * missing values. Passed down the recursion tree as Utils.getOption()
    * does not retain values down recursionTree
    * @throws Exception
    */
   private void makeTree(Vector<AttribValuePair> usedAttribValueSet,
         boolean isFirstTime, boolean replaceMissingWithModeValue)
         throws Exception {
      if (usedAttribValueSet == null) {
         System.err
               .println("Cannot make the decision tree: class label not set");
         System.exit(0);
      }

      if (this.DEBUG) {
         printusedAttributeValueSet(usedAttribValueSet);
      }

      boolean zeroInstances = false;
      int numberOfInstances = 0;
      // Compute attribute with maximum information gain
      double[] infoGains = new double[allAttributes.size() - 1];
      ColumnDescriptor tempAttrib;
      Vector<String> possibleValues;
      double maxInfoGain = 0;
      int[][] counts = null;
      // iterate through all attributes except the class
      for (int i = 0; i < allAttributes.size() - 1 && !zeroInstances; i++) {
         numberOfInstances = 0;
         tempAttrib = allAttributes.get(i);
         if (contains(usedAttribValueSet, tempAttrib)) {
            infoGains[i] = 0;
         } else {
            counts = computeCounts(usedAttribValueSet, tempAttrib, isFirstTime);
            for (int x = 0; x < tempAttrib.getNumValues(); x++)
               for (int y = 0; y < numOfClassLabels; y++) {
                  numberOfInstances += counts[x][y];
               }
            // Debug("Inside makeTree: numberOfInstances=" +
            // numberOfInstances);
            if (numberOfInstances == 0) {
               // NEERAJ-->Explain This
               zeroInstances = true;
               m_Attribute = null;
               m_ClassValue = ColumnDescriptor.DEFAULT_MISSING_VALUE;
               m_Distribution = new double[numOfClassLabels];
               return;
            }
            infoGains[i] = computeInfoGain(counts);
         }
         Debug("Inside makeTree(): Attribute Index=" + i + " Attribute  Name="
               + tempAttrib.getColumnName() + " Info Gain=" + infoGains[i]);
      }
      int maxIndex = Utils.maxIndex(infoGains);
      maxInfoGain = infoGains[maxIndex];
      m_Attribute = allAttributes.get(maxIndex);
      Debug("Inside makeTree(): Attribute with maximum information gain is "
            + m_Attribute.getColumnName() + " infoGain =" + maxInfoGain);
      // NEERAJ: why doing seperate when maxInfoGain=0
      if (maxInfoGain == 0) {
         if (counts != null) {
            m_Attribute = null;
            m_Distribution = new double[numOfClassLabels];
            int sum;

            for (int i = 0; i < numOfClassLabels; i++) {
               sum = 0;
               for (int j = 0; j < counts.length; j++) {
                  sum += counts[j][i];
               }
               m_Distribution[i] = sum;
            }
         } else {
            m_Distribution = computeDistribution(usedAttribValueSet);
         }
         if (DEBUG) {
            for (int i = 0; i < numOfClassLabels; i++) {
               Debug("Inside makeTree(): m_Distribution[" + i + "]="
                     + m_Distribution[i]);
            }
         }
         Utils.normalize(m_Distribution); // NEERAJ: Explain Normalization
         m_ClassValue = classLabels.elementAt(Utils.maxIndex(m_Distribution));

      } else {
         /*
          * Instances[] splitData = splitData(data, m_Attribute). Following
          * segment of code mimics this statement of Id3 in Weka
          */

         Vector<AttribValuePair> newAttribValueSet = new Vector<AttribValuePair>();

         // AttribValuePair[] newAttribValueSet = new
         // AttribValuePair[usedAttribValueSet.length + 1];
         int passedSize = usedAttribValueSet.size();
         for (int i = 0; i < usedAttribValueSet.size(); i++) {
            newAttribValueSet.add(usedAttribValueSet.get(i));
            // newAttribValueSet[i] = usedAttribValueSet[i];
         }

         /**
          * Add a place holder for the current Attribute. The possible
          * value for each path will be set later by iterating through all
          * possible values for the attribute
          */
         AttribValuePair placeHolder = new AttribValuePair();
         newAttribValueSet.add(placeHolder);

         m_Successors = new Id3SimpleClassifier[m_Attribute.getNumValues()];
         possibleValues = m_Attribute.getPossibleValues();

         for (int j = 0; j < m_Attribute.getNumValues(); j++) {
            AttribValuePair m_AttributeValuePair = new AttribValuePair();
            m_AttributeValuePair.setAttribName(m_Attribute.getColumnName());
            m_AttributeValuePair.setAttribValue(possibleValues.get(j));
            if (replaceMissingWithModeValue) {
               String mostLikelyValue = getMostLikelyValue(m_Attribute);

               if (possibleValues.get(j).equals(mostLikelyValue)) {
                  m_AttributeValuePair.setIncludeMissingValue(m_Attribute
                        .getMissingValue());
               }
            }

            /* replace Place Holder with correct set */
            newAttribValueSet.set(passedSize, m_AttributeValuePair);
            m_Successors[j] = new Id3SimpleClassifier();
            /**
             * record the next call to makeTree is NOT the firstInvocation
             */
            isFirstTime = false;
            m_Successors[j].makeTree(newAttribValueSet, isFirstTime,
                  replaceMissingWithModeValue);
         }
      }
   }

   private void printusedAttributeValueSet(
         Vector<AttribValuePair> usedAttribValueSet) {
      String output = "";
      for (int i = 0; i < usedAttribValueSet.size(); i++) {
         output += usedAttribValueSet.get(i).getAttribName() + "="
               + usedAttribValueSet.get(i).getAttribValue() + ";";
      }
      System.out.println("usedAttribute Set->" + output);

   }

   public double[] computeDistribution(
         Vector<AttribValuePair> usedAttribValueSet) throws Exception {
      double[] distrib = new double[numOfClassLabels];
      ISufficentStatistic tempSuffStat;

      // get the attributeValue pair at the first location which is class
      // label
      AttribValuePair temp = usedAttribValueSet.get(0);
      for (int i = 0; i < numOfClassLabels; i++) {
         temp.setAttribValue(classLabels.get(i));
         usedAttribValueSet.set(0, temp);
         // usedAttribValueSet[0].setAttribValue(classLabels.get(i));
         AttribValuePair[] usedAttribValueSetAsArray = (AttribValuePair[]) usedAttribValueSet
               .toArray(new AttribValuePair[usedAttribValueSet.size()]);
         tempSuffStat = dataSource
               .getSufficientStatistic(usedAttribValueSetAsArray);
         Id3SimpleClassifier.countQueries++;
         distrib[i] = tempSuffStat.getValue();
      }
      return distrib;
   }

   public int[][] computeCounts(Vector<AttribValuePair> oldAttribValueSet,
         ColumnDescriptor newAttrib, boolean firstTime) throws Exception {
      int[][] counts = new int[newAttrib.getNumValues()][numOfClassLabels];
      Vector<AttribValuePair> newAttribValueSet = new Vector<AttribValuePair>();

      // Neeraj: See if you require this and what about using arrayCopy
      for (int i = 0; i < oldAttribValueSet.size(); i++) {
         newAttribValueSet.add(oldAttribValueSet.get(i));
      }

      AttribValuePair newAttribValuePair = new AttribValuePair(newAttrib
            .getColumnName(), "");
      /* Add the new Attrib , Each possible values will be set below */
      newAttribValueSet.add(newAttribValuePair);
      Vector<String> newAttribValues = newAttrib.getPossibleValues();
      ISufficentStatistic tempSuffStat;
      for (int i = 0; i < newAttrib.getNumValues(); i++) {

         /* set the possible value for newAttrib */
         newAttribValuePair.setAttribValue(newAttribValues.get(i));

         /*
          * Add the fully formed newAttributeValuePair at the correct
          * location
          */
         newAttribValueSet.set(oldAttribValueSet.size(), newAttribValuePair);
         AttribValuePair classLabelAttribValuePair;
         for (int j = 0; j < numOfClassLabels; j++) {
            classLabelAttribValuePair = newAttribValueSet.get(0);

            // set the current value for class label
            classLabelAttribValuePair.setAttribValue(classLabels.get(j));

            newAttribValueSet.set(0, classLabelAttribValuePair);
            // newAttribValueSet[0].setAttribValue(classLabels.get(j));
            AttribValuePair[] conjuctionQuery = (AttribValuePair[]) newAttribValueSet
                  .toArray(new AttribValuePair[newAttribValueSet.size()]);
            tempSuffStat = dataSource.getSufficientStatistic(conjuctionQuery);
            Id3SimpleClassifier.countQueries++;
            counts[i][j] = tempSuffStat.getValue().intValue();
            /*
             * Debug("Inside computeCounts(): counts[" + i + "][" + j +
             * "]=" + counts[i][j]);
             */

         }
      }

      /**
       * If this is the firstTime that it was called, the counts can be
       * used to the most likely value for the attributes
       */
      if (firstTime) {
         AddMostCommonValue(newAttrib, counts);
      }

      return counts;
   }

   private void AddMostCommonValue(ColumnDescriptor newAttrib, int[][] counts) {
      int maxIndex = 0;
      int maxCount = 0;

      for (int i = 0; i < counts.length; i++) {
         int currAttribCount = 0;
         for (int j = 0; j < counts[i].length; j++) {
            currAttribCount += counts[i][j];
         }
         if (currAttribCount > maxCount) {
            maxCount = currAttribCount;
            maxIndex = i;
         }
      }
      String value = newAttrib.getPossibleValues().get(maxIndex);
      System.out.println(" Most common value for " + newAttrib.getColumnName()
            + "=" + value);
      mostLikelyValues.put(newAttrib, value);
   }

   private boolean contains(Vector<AttribValuePair> usedAttribs,
         ColumnDescriptor newAttrib) {

      for (int i = 0; i < usedAttribs.size(); i++) {
         if (usedAttribs.get(i).getAttribName().equals(
               newAttrib.getColumnName())) {
            Debug("newAttrib " + newAttrib.getColumnName() + "already in use");
            return true;
         }
      }
      return false;
      /*
       * for (int i = 0; i < usedAttribs.length; i++) { if
       * ((usedAttribs[i].getAttribName()).equalsIgnoreCase(newAttrib
       * .getColumnName())) { Debug("newAttrib " +
       * newAttrib.getColumnName() + "already in use"); return true; } }
       * return false;
       */
   }

   /**
    * 
    * @param classAndOldAttribSet list of old attribute value pairs used to
    * produce a subset of data for computing Information Gain
    * @param newAttribValues the new attribute and its values for which
    * Information Gain is to be computed on the subset of data
    * @return Information Gain for the subset of data based on the
    * newAttribValues
    * @throws Exception
    */

   private double computeInfoGain(int[][] counts) throws Exception {
      if (counts == null) {
         System.out.println("Cannot compute information gain");
         return -1;
      } else {
         int sum, oldCount = 0;
         int[] classCounts = new int[numOfClassLabels];
         for (int i = 0; i < numOfClassLabels; i++) {
            sum = 0;
            for (int j = 0; j < counts.length; j++) {
               sum += counts[j][i];
            }
            classCounts[i] = sum;
            oldCount += classCounts[i];
         }
         int newCount;
         double infoGain = computeEntropy(classCounts);
         double newEntropy;
         for (int i = 0; i < counts.length; i++) {
            newCount = 0;
            for (int j = 0; j < numOfClassLabels; j++) {
               classCounts[j] = counts[i][j];
               newCount += counts[i][j];
            }
            if (newCount > 0) {
               newEntropy = computeEntropy(classCounts);
               // Debug("newCount= " + newCount);
               infoGain -= ((double) newCount / oldCount) * newEntropy;
            }
         }
         // Debug("InfoGain= " + infoGain);
         return infoGain;
      }
   }

   /**
    * 
    * @param classAndAttributes list of attribute value pairs (beginning
    * with the class attribute) according to which a subset of data is to
    * be chosen for computing Entropy
    * @return entropy for the subset of data specified by
    * classAndAttributes
    * @throws Exception
    */
   private double computeEntropy(int[] classCounts) throws Exception {
      if (classCounts == null) {
         System.err.println("Entropy cannot be computed");
         /* Debug("Entropy= " + -1); */
         return -1;
      } else {
         double entropy = 0, total = 0;
         for (int j = 0; j < numOfClassLabels; j++) {
            if (classCounts[j] > 0) {
               /*
                * temp = classCounts[j] Utils.log2(classCounts[j]);
                * 
                * Debug("Class Count " + j + " = " + classCounts[j]);
                * Debug("temp=" + temp);
                */
               entropy -= classCounts[j] * Utils.log2(classCounts[j]);
               total += classCounts[j];
            }
         }
         entropy /= total;
         entropy += Utils.log2(total);
         /* Debug("Entropy= " + entropy); */
         return entropy;
      }

   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * airldm2.classifiers.Classifier#classifyInstance(airldm2.core.LDInstance
    * )
    */
   // @Override
   public double classifyInstance(LDInstance instance) throws Exception {
      // TODO Correct and Finish this code
      double[] dist = distributionForInstance(instance);
      double index = -1;
      double max = -1;

      for (int i = 0; i < dist.length; ++i) {
         if (dist[i] > max) {
            max = dist[i];
            index = i;
         }
      }
      return index;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * airldm2.classifiers.Classifier#distributionForInstance(airldm2.core
    * .LDInstance)
    */
   // @Override
   public double[] distributionForInstance(LDInstance instance)
         throws Exception {
      // TODO check if an instance has missing value
      /*
       * CODE SEGMENT FROM WEKA if (instance.hasMissingValue()) { throw new
       * NoSupportForMissingValuesException( "Id3: no missing values, " +
       * "please."); }
       */
      if (m_Attribute == null) {
         return m_Distribution;
      } else {
         String m_Value = instance.getValue(m_Attribute);

         return m_Successors[m_Attribute.getIndex(m_Value)]
               .distributionForInstance(instance);
      }
   }

   /**
    * This function converts the Id3 into a printable string format. This
    * function mimics the operation of its Weka counterpart
    */
   @Override
   public String toString() {

      if ((m_Distribution == null) && (m_Successors == null)) {
         return "Id3: No model built yet.";
      }
      return " Resultant Decison Tree \n\n" + toString(0);
   }

   private String toString(int level) {

      StringBuffer text = new StringBuffer();

      if (m_Attribute == null) {
         if (m_ClassValue.equals(ColumnDescriptor.DEFAULT_MISSING_VALUE)) {
            text.append(": null");
         } else {
            text.append(": " + m_ClassValue);
         }
      } else {
         Vector<String> possibleValues = m_Attribute.getPossibleValues();
         for (int j = 0; j < m_Attribute.getNumValues(); j++) {
            text.append("\n");
            for (int i = 0; i < level; i++) {
               text.append("|  ");
            }
            text.append(m_Attribute.getColumnName() + " = "
                  + possibleValues.elementAt(j));
            text.append(m_Successors[j].toString(level + 1));
         }
      }
      return text.toString();
   }

   private void Debug(String debug) {
      if (DEBUG) {
         System.out.println(debug);
      }
   }

   public static String getMostLikelyValue(ColumnDescriptor desc) {
      return mostLikelyValues.get(desc);

   }

   public static void main(String[] args) {

      Id3SimpleClassifier classifier = new Id3SimpleClassifier();
      runClassifier(classifier, args);

   }

}