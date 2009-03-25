
default: quick 

all:
	pdflatex project.tex
	bibtex project
	pdflatex project.tex
	makeindex project.idx
	pdflatex project.tex

clean:
	rm -f *.out *.ilg *ind *.bbl *.blg *.toc *.idx *.log *.aux *.lof *.lot *.loa

quick:
	pdflatex project.tex
