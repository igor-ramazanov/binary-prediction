Simple machine learning project for prediction binary characters.


If you will ask some man to randomly say zero or one for the long time and you will keep the answers, so after keeping enough count of answers next answer will can be predicted by machine learning algorithms.


In this project ngramms based algorithm is being used.

At the answers keeping this algorithm build the frequency tables of ngramms with various size (tuning in code).

To predict a binary it watch to the slice of the end of kept answers with size less than ngramm size by one. Then it choose more probable character from frequency table.