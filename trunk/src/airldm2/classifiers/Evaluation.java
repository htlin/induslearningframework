package airldm2.classifiers;

import java.util.List;

import weka.classifiers.evaluation.ConfusionMatrix;
import weka.classifiers.evaluation.NominalPrediction;
import weka.core.Utils;
import airldm2.classifiers.rl.AggregatedInstance;
import airldm2.classifiers.rl.InstanceAggregator;
import airldm2.classifiers.rl.RelationalBayesianClassifier;
import airldm2.core.LDInstances;
import airldm2.core.LDTestInstances;
import airldm2.core.SSDataSource;
import airldm2.core.SSDataSourceFactory;
import airldm2.core.datatypes.relational.SingleRelationDataDescriptor;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.util.SimpleArffFileReader;

/**
 * 
 * TODO Write class description here.
 * 
 * @author neeraj (neeraj.kaul@gmail.com, neeraj@cs.iastate.edu)
 * @since Aug 10, 2008
 * @version $Date: $
 */
public class Evaluation {

   public static String evaluateModel(Classifier classifier, String[] options)
         throws Exception {

      boolean trainFileInArff = Utils.getFlag("a", options);
      boolean trainFileInDB = Utils.getFlag("b", options);
      if (trainFileInArff && trainFileInDB) {
         throw new Exception(" Flag -a  and -b  are incompatible");
      }

      if (!(trainFileInArff || trainFileInDB)) {
         throw new Exception("Either Flag -a or -b must be specified ");
      }

      String testFile = Utils.getOption("testFile", options);
      SimpleArffFileReader readTest = new SimpleArffFileReader(testFile);
      LDTestInstances testInst = readTest.getTestInstances();
      SingleRelationDataDescriptor desc = (SingleRelationDataDescriptor) testInst
            .getDesc();
      if (trainFileInDB) {
         String dataSourceType = "relational";
         // set dataSourceType to Indus if option is used
         boolean dataSourceTypeOption = Utils.getFlag("indus", options);
         if (dataSourceTypeOption) {
            dataSourceType = "indus";

         }

         String trainTableName = Utils.getOption("trainTable", options);

         // SSDataSource dataSource = new
         // RelationalDataSource(trainTableName);
         SSDataSource dataSource;
         if (dataSourceType.equals("relational")) {
            dataSource = SSDataSourceFactory.getSSDataSourceImpl("relational");

         } else if (dataSourceType.equals("indus")) {
            dataSource = SSDataSourceFactory.getSSDataSourceImpl("indus");

            /*
             * get access to the directory containing the indus
             * configuration files (including indus.conf)
             */
            String indusBase = Utils.getOption("indus_base", options);

            if (indusBase == null || indusBase.equals("")) {
               throw new Exception(
                     " Check options. The indus Flag should be followed by a directory to initialize Indus Integration Framework ");

            }

            /* Initialize the Indus Data Source */
            dataSource.init(indusBase);
         } else
            throw new Exception(
                  " Only Relational and indus Data Sources are supported");
         /*************************************************************************/
         dataSource.setRelationName(trainTableName);
         LDInstances trainData = new LDInstances();
         trainData.setDesc(desc);
         trainData.setDataSource(dataSource);
         ConfusionMatrix matrix = Evaluation.evaluateModel(classifier,
               trainData, testInst, options);
         return matrix.toString("===Confusion Matrix===");

      } else if (trainFileInArff) {
         String trainFile = Utils.getOption("trainFile", options);
         SimpleArffFileReader readTrain = new SimpleArffFileReader(trainFile);
         LDInstances trainData = readTrain.getLDInstances(true);
         ConfusionMatrix matrix = Evaluation.evaluateModel(classifier,
               trainData, testInst, options);
         return matrix.toString("===Confusion Matrix===");
      } else {
         StringBuffer str = new StringBuffer();
         str.append("options" + "\t\n");
         str
               .append("-a"
                     + "\t The training file is to be read from an arff file and inserted into database \n");
         str.append("-b"
               + "\t The training file is to be read from  database \n");
         str
               .append("-trainTable"
                     + "\t  name of the table  which contains training instances (in conjuction with flag -b)   \n");
         str.append("-testFile" + "\t The arff file against which to test \n");
         str
               .append("-trainFile"
                     + "\t The arff file which contains training instances (in conjuction with flag -a) \n");

         throw new Exception("Incorrect options \n" + str.toString());

      }

   }

   public static ConfusionMatrix evaluateModel(Classifier classifier,
         LDInstances trainData, LDTestInstances testData, String[] options)
         throws Exception {

      // initialize Confusion Matrix with Class Labels
      String[] classLabels = testData.getDesc().getClassLabels();
      ConfusionMatrix wekaConfusionMatrix = new ConfusionMatrix(classLabels);

      int numTestInstances = testData.getNumberInstances();
      double[] distribution = new double[classLabels.length];
      classifier.setOptions(options);
      classifier.buildClassifier(trainData);

      for (int i = 0; i < numTestInstances; i++) {
         distribution = classifier.distributionForInstance(testData
               .getLDInstance(i));
         double actual = testData.getLDInstance(i).getClassValueLocation();

         // return some error message if the class label is not according
         // to the descriptor
         if (actual == -1) {
            System.out
                  .println("Please check the class label of  test instances match their description");
         }
         wekaConfusionMatrix.addPrediction(new NominalPrediction(actual,
               distribution));
      }

      return wekaConfusionMatrix;
   }

   public static ConfusionMatrix evaluateRBCModel(RelationalBayesianClassifier rbc, LDInstances trainInstances, LDInstances testInstances) throws Exception {
      RDFDataDescriptor desc = (RDFDataDescriptor) trainInstances.getDesc();
      String[] classLabels = desc.getClassLabels();
      ConfusionMatrix wekaConfusionMatrix = new ConfusionMatrix(classLabels);

      rbc.buildClassifier(trainInstances);
      List<AggregatedInstance> aggregatedInstances = InstanceAggregator.aggregate(testInstances);
      for (AggregatedInstance i : aggregatedInstances) {
         double[] distribution = rbc.distributionForInstance(i);
         double actual = i.getLabel();
         // return some error message if the class label is not according to the descriptor
         if (actual == -1) {
            System.out.println("Please check the class label of test instances match their description");
         }
         wekaConfusionMatrix.addPrediction(new NominalPrediction(actual, distribution));
      }
      
      return wekaConfusionMatrix;
   }

}