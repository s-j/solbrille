reset
set terminal postscript color 
set size 1.0, 1.0
set output "bench_okapi.ps"
set title "Precision/Recall"
set ylabel "Precision"
set xlabel "Recall"
set yrange [0:1.0]
set xrange [0:1.0]
set grid
set key outside below
plot 'bench_okapi.log' using 3:2 title 'Query 1' w lp, \
'bench_okapi.log' using 6:5 title 'Query 2' w lp, \
'bench_okapi.log' using 9:8 title 'Query 3' w lp, \
'bench_okapi.log' using 12:11 title 'Query 4' w lp, \
'bench_okapi.log' using 15:14 title 'Query 5' w lp, \
'bench_okapi.log' using 18:17 title 'Query 6' w lp, \
'bench_okapi.log' using 21:20 title 'Query 7' w lp, \
'bench_okapi.log' using 24:23 title 'Query 8' w lp, \
'bench_okapi.log' using 27:26 title 'Query 9' w lp, \
'bench_okapi.log' using 30:29 title 'Query 10' w lp
