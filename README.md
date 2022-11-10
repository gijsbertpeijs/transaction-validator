# transaction-validator

This transaction validator will check CSV and XML files for transaction validity and duplication. The input files can 
be configured using the *csv.file* and *xml.file properties*. The validator is setup to continue on invalid records until a 
threshold is reached. This threshold is defined via the properties *csv.skip.limit* and *xml.skip.limit*.

To run the application your system needs Java (version 8 or higher) and Maven 3 installed (or use the mvnw wrapper). 
Put your input files in the resources directory (or in the place defined in the properties) and run: 

`mvn spring-boot:run`

The final result of the validator is a report of the transaction with duplicate references or with an invalid end balance. 
This report is written to the application log.
