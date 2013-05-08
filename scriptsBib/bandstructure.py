#!python

import abipy;
import os, sys;
import argparse;

def bandstructure(inputFile,outputFile,title,backend):

    import matplotlib;
    matplotlib.use(backend);
    import matplotlib.pyplot as plt;

    ebands = abipy.ebands.ElectronBands.from_ncfile(inputFile)

    fig = ebands.plot(title=title,klabels={(0.0,0.0,0.0) : "$\Gamma$", (0.5,0.0,0.0) : "L", (0.5,0.5,0.0) : "X", (0.25,0.25,0.0) : "Y"})

    fig.savefig(outputFile);

    plt.close(fig)

def usage():
    """usage de la ligne de commande"""
    print "usage: ", sys.argv[0], "--inputFile=file --outputFile=file"

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('-o', '--outputFile')
    parser.add_argument('-i', '--inputFile')
    parser.add_argument('-t', '--title')
    parser.add_argument('-f', '--backend')
    args = parser.parse_args()
    bandstructure(args.inputFile,args.outputFile,args.title,args.backend)
