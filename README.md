# customer-statement-processor
 
A simple service that validates a list of MT940 bank account statements. The statements can be supplied in both csv and xml format
and can bevalidated through the "/validate" post rest-endpoint. 

It returns a result that tells if the supplied content contains any validation violations or not.
