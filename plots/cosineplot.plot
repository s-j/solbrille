reset
set terminal postscript color 
set size 1.0, 1.0
set output "bench_cosine.ps"
set title "Precision/Recall"
set ylabel "Precision"
set xlabel "Recall"
set yrange [0:1.0]
set xrange [0:1.0]
set grid
set key outside below
plot 'bench_cosine.log' using 1:2 title 'Query 1' w lp, \
'bench_cosine.log' using 3:4 title 'Query 2' w lp, \
'bench_cosine.log' using 5:6 title 'Query 3' w lp, \
'bench_cosine.log' using 7:8 title 'Query 4' w lp, \
'bench_cosine.log' using 9:10 title 'Query 5' w lp, \
'bench_cosine.log' using 11:12 title 'Query 6' w lp, \
'bench_cosine.log' using 13:14 title 'Query 7' w lp, \
'bench_cosine.log' using 15:16 title 'Query 8' w lp, \
'bench_cosine.log' using 17:18 title 'Query 9' w lp, \
'bench_cosine.log' using 19:20 title 'Query 10' w lp
