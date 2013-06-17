#!/bin/bash

LOG=AbinitGUI_bin_ver.log

OPTS='-Rf'

Version='2013r3'
PackageFolder='AbinitGUI-'$Version

#bash MakeGUI
#ant standalone

# Création du dossier contenant le Package AbinitGUI
[ -d $PackageFolder ] && echo 'Folder '$PackageFolder' exists!' || mkdir $PackageFolder

echo 'Copy of other files...'
cp $OPTS test1.in $PackageFolder/. 2>> $LOG
cp $OPTS test2.in $PackageFolder/. 2>> $LOG
cp $OPTS test3.in $PackageFolder/. 2>> $LOG
cp $OPTS tbener $PackageFolder/. 2>> $LOG

rsync -a --exclude='.*' np $PackageFolder/. 2>> $LOG
rsync -a --exclude='.*' PSP $PackageFolder/.
rsync -a --exclude='.*' ClustepEx $PackageFolder/. 2>> $LOG
rsync -a --exclude='.*' scriptsBib $PackageFolder/. 2>> $LOG
#rsync -a --exclude='.*' doc $PackageFolder/. 2>> $LOG
cp $OPTS doc/AbinitGUI_doc_v2.pdf $PackageFolder/doc_v2.pdf 2>> $LOG
cp $OPTS json_vars.txt $PackageFolder/. 2>> $LOG

cp $OPTS Jmol.jar $PackageFolder/. 2>> $LOG
cp $OPTS FlavioChart.jar $PackageFolder/. 2>> $LOG

cp $OPTS Tight-Binding_src.tar.gz $PackageFolder/. 2>> $LOG
cp $OPTS CLUSTEP_src.tar.gz $PackageFolder/. 2>> $LOG

cp $OPTS listScripts.xml $PackageFolder/. 2>> $LOG

cp $OPTS generic_config.xml $PackageFolder/config.xml 2>> $LOG
cp $OPTS README_binVer.txt $PackageFolder/README 2>> $LOG
#cp $OPTS AbinitGUI.jar $PackageFolder/AbinitGUI.jar 2>> $LOG
cp $OPTS standalone/AbinitGUI.jar $PackageFolder/AbinitGUI.jar 2>> $LOG
echo 'Files copied'

find $PackageFolder -name .DS_Store -type f -print0 | xargs -0 /bin/rm -f

#echo 'Creating AbinitGUI.tar.gz file...'
#tar -zcvf AbinitGUI.tar.gz $PackageFolder >> $LOG
#echo 'AbinitGUI.tar.gz file created.'

echo 'Creating AbinitGUI.zip file...'
zip -r AbinitGUI $PackageFolder >> $LOG
echo 'AbinitGUI.zip file created.'
