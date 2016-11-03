Simple machine learning project for prediction binary characters.


If you will ask some man to randomly say zero or one for the long time and you will keep the answers, so after keeping enough count of answers next answer will can be predicted by machine learning algorithms.


This project use ngramms based algorithm.

While the answers keeping this algorithm build the frequency tables of ngramms with various size (tuning in code).

To predict a binary it watch to the slice of the end of kept answers with size less than ngramm size by one. Then it choose more probable character from frequency table.

For creating .jar archive use "assembly" sbt command.
.jar archive is applied.

For running .jar archive perform command "java -jar binary-predictions-assembly.jar --help" and follow instructions.