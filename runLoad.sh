#!/bin/bash

# Recreate tables
mysql < drop.sql
mysql < create.sql

# Compile and run SAX
javac MySAX.java
java MySAX $EBAY_DATA/*.xml

# Load data
mysql ad < load.sql

# Delete temp files
rm *.csv
