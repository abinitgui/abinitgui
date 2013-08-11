#!python

import abipy;
import os, sys;
import argparse;

def bsanddos(inputDos,inputBand,outputDos,outputBand,outputBoth,titleBS,titleDOS,show):

    ebands = abipy.ebands.ElectronBands.from_file(inputBand)
    ebandsDos = abipy.ebands.ElectronBands.from_file(inputDos)

    dos = ebandsDos.get_edos();

    fig=dos.plot(title=titleDOS,show=show,savefig=outputBand)

    fig=ebands.plot(title=titleBS,show=show,savefig=outputDos)

    fig=ebands.plot_with_dos(dos,savefig=outputBoth,show=show)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('-b', '--inputBand')
    parser.add_argument('-d', '--inputDos')
    parser.add_argument('-B', '--outputBand')
    parser.add_argument('-D', '--outputDos')
    parser.add_argument('-A', '--outputBoth')
    parser.add_argument('-t', '--titleBS')
    parser.add_argument('-u', '--titleDOS')
    parser.add_argument('-s', '--show')
    args = parser.parse_args()
    bsanddos(args.inputDos,args.inputBand,args.outputDos,args.outputBand,args.outputBoth,args.titleBS,args.titleDOS,args.show);
