#!/bin/bash
#
# On old Green node
#$ -l nb=false
#
# Ask for pe=parrallel environment, snode or openmpi
# snode= same node, as the shared memory communication is the fastest
#$ -pe openmpi 1
# -pe snode8 8

# keep current working directory
#$ -cwd

#$ -o SGE_out-$JOB_ID.log
#$ -e SGE_err-$JOB_ID.log

# give a name to your job
#$ -N simulation

# keep all the defined variables
#$ -V
#$ -l nb=false

# not mandatory: highmem=true (hm=true) for 32GB node
# or hm=false for 16GB node
# no hm argument does not take about the kind of node ram (16/32)
# -l hm=true

# IMPORTANT: You need to specify the mem_free
# h_vmem can also be set but mf is mandatory!
# max 31G if hm=true and max 15G if hm=false
#$ -l mf=2000

# Specify the requested time
#$ -l h_rt=2:00:00

# To be informed by email (besa= begin,end,stop,abort)
#$ -M yannick.gillet@uclouvain.be
#$ -m besa


MPI=mpirun
${MPI} -np 1 /home/naps/ygillet/MAPR2451-v2/7.3.1-public/build/src/98_main/abinit < /home/users/y/g/ygillet/MySims/test3.files >& /home/users/y/g/ygillet/MySims/logfiles/test3.log


