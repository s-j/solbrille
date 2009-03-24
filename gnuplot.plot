reset
set terminal latex 
set size 2.8/5, 3/3.
set output "bench.tex"
set title "Precision/Recall"
set xlabel "Recall"
set ylabel "Precision"
set yrange [0:]
set xrange [0:]
set grid
set key outside below
plot 'bench.log' using 2:3 title 'Query 1' w lp, \
'bench.log' using 5:6 title 'Query 2' w lp, \
'bench.log' using 8:9 title 'Query 3' w lp, \
'bench.log' using 11:12 title 'Query 4' w lp, \
'bench.log' using 14:15 title 'Query 5' w lp, \
'bench.log' using 17:18 title 'Query 6' w lp, \
'bench.log' using 20:21 title 'Query 7' w lp, \
'bench.log' using 23:24 title 'Query 8' w lp, \
'bench.log' using 26:27 title 'Query 9' w lp, \
'bench.log' using 29:30 title 'Query 10' w lp