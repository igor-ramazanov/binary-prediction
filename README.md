# Binary prediction
Simple machine learning project for prediction binary characters.

## Description
If you ask someone to randomly say zero or one for the long time and you keep the answers, then, after keeping enough amount of answers, you be able to predict next number (zero or one).

This project uses `ngramms` based algorithm.

During keeping of answers, the algorithm builds the frequency tables of ngramms (length of ngramss can be tuned by code).

To predict a number, algorithm looks to the slice of the last answers and choose ngramms with bigger frequence

Example:
```
Ngramm size = 2
Answers: 010100
Frequency table:
00 - 1
01 - 2
10 - 2
11 - 0

Predict a number:
01010(0..)
Ngramm starts with '0'
2 variants:
1) 00 - frequency == 1
2) 01 - frequency == 2

Choose bigger frequency - 01
So, the next number is '1'
```

## Build
```
sbt assembly
```

## Running
```
java -jar binary-predictions-assembly.jar --help
#Follow instructions
```
