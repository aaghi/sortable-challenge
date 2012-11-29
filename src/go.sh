#!/bin/bash
javac -cp json-simple-1.1.1.jar aaghi/sortable/challenge/Application.java aaghi/sortable/challenge/Product.java
java -cp .:json-simple-1.1.1.jar aaghi/sortable/challenge/Application
