package airldm2.classifiers.rl.estimator;

import static airldm2.constants.Constants.EPSILON;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import airldm2.core.ISufficentStatistic;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.exceptions.RDFDatabaseException;

public class ExponentialEstimator extends AttributeEstimator {

   private Histogram mClassHistogram;
   private int mNumInstances;
   
   private Histogram mValueSums;
   
   public ExponentialEstimator(RbcAttribute att) {
      super(att);
   }

   @Override
   public void estimateParameters(RDFDataSource source, RDFDataDescriptor desc, ClassEstimator classEst) throws RDFDatabaseException {
      mClassHistogram = classEst.getClassHistogram();
      mNumInstances = classEst.getNumInstances();
      RbcAttribute targetAttribute = desc.getTargetAttribute();
      int numOfClassLabels = targetAttribute.getDomainSize();

      double[] valueSums = new double[numOfClassLabels];
      for (int j = 0; j < numOfClassLabels; j++) {
         SuffStatQueryParameter queryParam = new SuffStatQueryParameter(desc.getTargetType(), targetAttribute, j, mAttribute, -1);
         ISufficentStatistic tempSuffStat = source.getSumSufficientStatistic(queryParam);
         valueSums[j] = tempSuffStat.getValue().doubleValue();
      }
      
      mValueSums = new Histogram(valueSums);
   }

   @Override
   public boolean isValid() {
      return !mValueSums.containsZeroCount();
   }

   @Override
   public void mergeWith(List<AttributeEstimator> ests) {
      for (AttributeEstimator est : ests) {
         if (!(est instanceof ExponentialEstimator)) {
            throw new IllegalArgumentException("Expected an ExponentialEstimator but " + est);
         }
         
         ExponentialEstimator otherEst = (ExponentialEstimator) est;
         mClassHistogram = otherEst.mClassHistogram;
         mNumInstances = otherEst.mNumInstances;
         if (mValueSums == null) {
            mValueSums = otherEst.mValueSums.copy(); 
         } else {
            mValueSums.add(otherEst.mValueSums);
         }
      }
   }
   
   @Override
   public double computeLikelihood(int classIndex, AttributeValue v) {
      double val = 0.0;
      
      if (v instanceof Null) {
         return 0.0;
      } else if (!(v instanceof Numeric)) { 
         throw new IllegalArgumentException("Error: value " + v + " is not a Numeric for ExponentialEstimator.");
      } else {
         val = ((Numeric) v).getValue();
      }
      
      double mean = (mValueSums.get(classIndex) + 1) / (mClassHistogram.get(classIndex) + mClassHistogram.size());
      double lambda = 1 / mean;
      
      return Math.log(lambda) - (lambda * val);
   }
   
   @Override
   public double computeLL() {
      double result = 0.0;
      for (int j = 0; j < mClassHistogram.size(); j++) {
         double mean = mValueSums.get(j) / mClassHistogram.get(j);
         if (mean < EPSILON) continue;
         
         double lambda = 1 / mean;
         result += mClassHistogram.get(j) * Math.log(lambda) - lambda * mValueSums.get(j);
      }
      return result;
   }

   @Override
   public double computeDualLL() {
      if (mClassHistogram.size() != 2) {
         throw new UnsupportedOperationException("DualLL not supported for Exponential estimator if there are more than two classes.");
      }
      
      double result = 0.0;
      for (int j = 0; j < mClassHistogram.size(); j++) {
         double mean = mValueSums.get(j) / mClassHistogram.get(j);
         if (mean < EPSILON) continue;
         
         double lambda = 1 / mean;
         result += mClassHistogram.get(j) * Math.log(lambda) - lambda * mValueSums.get(1 - j);
      }
      return result;
   }

   public Histogram getValueSumsForTest() {
      return mValueSums;
   }
   
   @Override
   public String toString() {
      return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
         .append("mValueSums", mValueSums)
         .toString();
   }

   @Override
   public double paramSize() {
      return mValueSums.size();
   }

}
