This document describes how to set up a Virtuoso repository (storing RDF graphs) under Windows environment, and then load RDF graphs from a file.
For an up-to-date description please refer to http://virtuoso.openlinksw.com/dataspace/dav/wiki/Main/VOSIndex


# Setting up Virtuoso #

  1. Download Virtuoso Open-Source Edition from http://virtuoso.openlinksw.com/dataspace/dav/wiki/Main/VOSDownload
  1. Extract to a folder (which will be referred to as %VIRTUOSO\_ROOT% from now on)
  1. Start up server using default configs: %VIRTUOSO\_ROOT%\bin\virtuoso-t.exe -f

SPARQL server should now be accessed through http://localhost:8890/, click Conductor, login as dba/dba, and go into RDF tab


# Load RDF graphs using script #

  1. Copy docs\virtuoso\rdfloader.sql into %VIRTUOSO\_ROOT%\database
  1. Copy RDF file (e.g. moviesTrain.xml) into %VIRTUOSO\_ROOT%\database
  1. Run %VIRTUOSO\_ROOT%\bin\isql.exe in %VIRTUOSO\_ROOT%\database
  1. 
```
load rdfloader.sql;
```
  1. 
```
ld_dir ('./', '*.xml', ':moviesTrain');
```
> > (':moviesTrain' specifies the graph IRI)
  1. 
```
rdf_loader_run ();
```

# Load RDF graphs using an importer #

Run airldm2.util.rdf.RDFImporter <graph IRI> <RDF file name in XML format>