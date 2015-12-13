#A High Level Design Overview.
# A High Level Design Overview #
## Major Classses ##
The following are the major classes in the learning framework.
  * **Classifier:** Interface which every new classifier must implement. Current implementation includes Naïve Bayes and Decision Trees.

  * **LDInstances:**   A large data set instance which may possibly exceed the size that can be stored in memory. It consists of data descriptor (Desc(D))  and a handle to the  datsource  that answers sufficient statistics.

  * **SSDataSource:**  Interface that every  datasource that  answers sufficient statistics queries will implement.  Current implementation has RelationalDataSource.  Future one will have IndusDataSource.

  * **ReplaceMissingValue:**  An unsupervised filter that replaces missing values by assigning the counts proportionally. Classifiers can call the function in this filter to get modified counts.

  * **Evaluation:**  Evaluates the given classifier on train and test data to return a Confusion matrix. Any implemented Classifier will automatically have access to this.

The Class diagram is shown below

> ![http://induslearningframework.googlecode.com/files/Classifier_seeded2.jpg](http://induslearningframework.googlecode.com/files/Classifier_seeded2.jpg)

### Execution Flow Overview ###
The implementing Classifier Class (say NaiveBayes/ID3) constructs the queries (including path clause )   that  are required for this particular type of  classifier. Any Optimizations (which may depend on passed options) are also performed here.   The constructed  queries  are posed to the  SSDataSource. The Classifier does not need to know how the queries are actually answered as they are abstracted out by SSDataSource Interface ( As an illustration the IndusDataSource will be a virtual data source). The classifier if   required can call the ReplaceMissingValues filter to get the modified counts.

