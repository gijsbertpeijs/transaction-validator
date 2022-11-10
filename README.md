# transaction-validator

This transaction validator will check CSV and XML files for transaction validity and duplication. The input files can 
be configured using the *csv.file* and *xml.file properties*. The validator is setup to continue on invalid records until a 
threshold is reached. This threshold is defined via the properties *csv.skip.limit* and *xml.skip.limit*.

The final result of the validator is a report of the transaction with duplicate references or with an invalid end balance.

