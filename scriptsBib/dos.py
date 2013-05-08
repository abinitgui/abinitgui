#!python

import abipy;
import os, sys;
import argparse;

def dos(inputFile,outputFile,title,backend,method,step,width):

    import matplotlib;
    matplotlib.use(backend);
    import matplotlib.pyplot as plt;

    ebands = abipy.ebands.ElectronBands.from_ncfile(inputFile)

    dos = ebands.get_dos(method=method, step=float(step), width=float(width));

    dos.plot(title=title)
    
    fig.savefig(outputFile);

    plt.close(fig);

def usage():
    """usage de la ligne de commande"""
    print "usage: ", sys.argv[0], "--inputFile=file --outputFile=file"

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('-o', '--outputFile')
    parser.add_argument('-t', '--title')
    parser.add_argument('-i', '--inputFile')
    parser.add_argument('-f', '--backend')
    parser.add_argument('--method')
    parser.add_argument('--step')
    parser.add_argument('--width')
    args = parser.parse_args()
    dos(args.inputFile,args.outputFile,args.title,args.backend,args.method,args.step,args.width)
