# Indus Learning Framework(ILF) #
The indus learning framework is a suite of machine learning algorithms that learn from  datasets using sufficient statistics.  This framework is particularly useful in the following scenarios:
  * When the data set is huge and the it cannot be fit into memory (e.g arff file is huge and weka runs out of memory)

  * When access to underlying data instances is not available (due to considerations such as security or cost) but the datasource provides  an endpoint or an querying interface over which certain statistics about the data can be obtained (e.g.  an RDF datasource that does not provide data dumps but can be queried via a SPARQL endpoint)

The current implementation of the framework provides Naive Bayes , Decision Trees and  a Relational Bayesian Classifier  based approach    to learn from a  RDF dataset (via a SPARQL endpoint).  The framework has been written so that it can be extended to include more classifiers that are amenable to the sufficient statistics approach.

A detailed user's guide   describing how to  configure and use the classifiers  in the system is  included in the zip available for download.
An overview of how to use the system is described below and also included in the   user's guide wiki.

> # ILF for Propositional Data #

## Set Up and Configuration ##
Download the latest ILF jar.

In the directory you downloaded the ILF.jar, create a config directory. In it create a file called database.properties with following fields
```
DataSource.driverClassName=com.mysql.jdbc.Driver
DataSource.url=jdbc:mysql://localhost/db_research
DataSource.username=ilf_user
DataSource.password=ilf_password
```
Change the values as appropriate to your case. These are used to make connection to the database that contains the learning instances

## Command Line Options ##

The Ilf.jar depends on the following jars:
  * WEKA (weka.jar)  for some utility functions
  * appropriate jdbc driver (e.g  mysql-connector-java-5.0.7-bin.jar)
  * DBCP commons (e.g. commons-dbcp-1.2.2.jar ) http://commons.apache.org/dbcp/

When calling from command line the  -classpath option is to use to point to these jars (alternatively environment variable CLASSPATH can be set)

  * Naive Bayes
_java -classpath < semi colon seperated list of dependent jars and ilf.jar> airldm2.classifiers.bayes.NaiveBayesClassifier_

&lt;options&gt;

 

  * Decision Trees
_java -classpath < semi colon seperated list of dependent jars and ilf.jar> airldm2.classifiers.trees.id3Classifier_

&lt;options&gt;

 

### Supported Options ###

  * **-a**    The training file is to be read from an arff file and inserted into database.

  * **-b**     The training instances are in a  relational database.

  * **-trainTable**  The Name of the table  which contains training instances (used in conjuction with flag -b).

  * **-testFile** The arff file against which to test.

  * **-trainFile** The arff file which contains training instances ( use in conjuction with flag -a ).



---



# ILF for RDF Data #

## Set Up and Configuration ##
ILF requires an RDF database to store RDF data. The current system has been tested using a Virtuoso database. See [VirtuosoGuide](VirtuosoGuide.md) for instructions on how to set up a Virtuoso database.

Download the latest ILF jar.

ILF provides two ways to connect to the RDF database that contains the training dataset as well as the test dataset for the classifier to build – an SPARQL endpoint or a local RDF database connection.  The information required to make the connection is read from a configuration file called rdfstore.properties. This configuration file contains the following fields (as name value pairs):
```
DataSource.sparqlEndpoint=<SPARQL endpoint URL>
DataSource.url=<Local RDF database URL> 
DataSource.username=<User Name>
DataSource.password=<Password> 
```

Specify Line 1 for an SPARQL endpoint connection, and specify Line 2-4 for a local RDF database connection.  If both are specified, the SPARQL endpoint will be used by default.
An example rdfstore.properties is given below
```
DataSource.sparqlEndpoint=http://localhost:8890/sparql
DataSource.url=jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2
DataSource.username=dba
DataSource.password=dba
```

## Command Line Options ##

Two modes of running the Relational Bayesian classifier (RBC) are provided: (i) using a locally configured RDF store with classifier evaluation, and (ii) using a remotely connected RDF store

### Running RBC using locally configured RDF store ###
```
ilf.bat rbc [options]
```
#### Supported Options ####
  * **-desc** The name of the descriptor file that describes the attributes and target types – see rbc\_example/README.txt for the descriptor format.
  * **-trainGraph** (OPTIONAL) The name of the context (RDF named graph) which contains the training triples.
  * **-testGraph** (OPTIONAL) The name of the context (RDF named graph) which contains the test triples.

### Running RBC to learn and classify remote RDF stores ###
```
ilf.bat remote-rbc [options]
```
#### Supported Options ####
  * **-desc** The name of the descriptor file that describes the attributes and target types – see rbc\_example/README.txt for the descriptor format.
  * **-output** The name of the output file to store classifications in RDF/XML format
  * **-trainEndpoint** The URI of SPARQL endpoint holding training RDF data.
  * **-trainGraph** (OPTIONAL) The name of the context (RDF named graph) which contains the training triples.
  * **-testEndpoint** The URI of SPARQL endpoint holding test RDF data.
  * **-testGraph** (OPTIONAL) The name of the context (RDF named graph) which contains the test triples.


## API Based Integration ##

Besides being run from command line the ILF allows provides an  API which can be used to integrated into a target application. An example to integrate Naive Bayes Classifier in an application is given below


### Integration Samples ###
#### sample 1 ####
```
import airldm2.core.datatypes.relational.SingleRelationDataDescriptor;
import airldm2.core.datatypes.relational.RelationalDataSource;
import  airldm2.util.SimpleArffFileReader;
import airldm2.classifiers.Evaluation
import weka.classifiers.evaluation.ConfusionMatrix;
import weka.core.Utils;

........
.......

String[] options= {"-b",  "-trainTable", "votes_train", "-testFile","sample/HouseVotesTrain.arff"};
    
          
String trainTableName = Utils.getOption("trainTable", options);
String testFile = Utils.getOption("testFile", options);

NaiveBayesClassifier classifier = new NaiveBayesClassifier();
         
SingleRelationDataDescriptor  desc = null;
     
      
SimpleArffFileReader readTest = new SimpleArffFileReader(testFile);
LDTestInstances testInst = readTest.getTestInstances();
desc = (SingleRelationDataDescriptor )testInst.getDesc();
         
SSDataSource dataSource = new RelationalDataSource(trainTableName);
// Create a Large DataSet Instance and set its descriptor and source
LDInstances trainData = new LDInstances();
trainData.setDesc(desc);
trainData.setDataSource(dataSource);
         
ConfusionMatrix matrix = Evaluation.evlauateModel2(classifier, trainData, testInst, options);
System.out.println(matrix.toString("===Confusion Matrix==="));
         

      
```

## Extension With Indus Integration Framework ##
The system can use a data integration system to be able to learn from multiple disparate data sources. The current implementation has been extended to use
[Indus Integration Framework](http://code.google.com/p/indusintegrationframework/).  User's are referred to the code and an example included in the source tree [induse\_extension\_src](http://code.google.com/p/induslearningframework/source/browse/#svn/trunk/indus_extension_src)



For feature requests contact neeraj.kaul@gmail.com


