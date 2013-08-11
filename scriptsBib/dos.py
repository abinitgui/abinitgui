#!python

import abipy;
import os, sys;
import argparse;

def dos(inputFile,outputFile,title,method,step,width,show):

    ebands = abipy.ebands.ElectronBands.from_file(inputFile)

    dos = ebands.get_edos(method=method, step=float(step), width=float(width));

    fig=dos.plot(title=title,show=show,savefig=outputFile)

def usage():
    """usage de la ligne de commande"""
    print "usage: ", sys.argv[0], "--inputFile=file --outputFile=file"

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('-o', '--outputFile')
    parser.add_argument('-t', '--title')
    parser.add_argument('-i', '--inputFile')
    parser.add_argument('-s', '--show')
    parser.add_argument('--method')
    parser.add_argument('--step')
    parser.add_argument('--width')
    args = parser.parse_args()
    dos(args.inputFile,args.outputFile,args.title,args.method,args.step,args.width,args.show)
