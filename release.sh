#!/bin/bash

java=$JAVA_HOME/bin/java

WS=~/workspace
R=src/main/resources

mv $WS/httpclient/$R /tmp/httpclient-res -v
mv $WS/webdatagrabber/$R /tmp/webdatagrabber-res -v

cd ../httpclient
mvn -q clean package install

cd ../webdatagrabber
mvn -q clean package install

cd ../hhparser
./increment-build.sh
mvn -q clean package install

mv /tmp/webdatagrabber-res $WS/webdatagrabber/$R -v
mv /tmp/httpclient-res $WS/httpclient/$R -v

DIR=~/Software/HHDG

mkdir -p $DIR
mkdir -p ~/Shared/hh

rm $DIR/*jar -rf
rm $DIR/log/* -rf
rm $DIR/out/* -rf
rm $DIR/out* -rf

cp target/headhunter*ies.jar $DIR/hhdg.jar -rfv
cp hhparser.conf $DIR -rfv

cd $DIR

echo
$java -jar hhdg.jar --version
echo

DIR_NAME=$($java -jar hhdg.jar --version-dirname)

mkdir $DIR_NAME

mv *jar $DIR_NAME
mv *conf $DIR_NAME

tar cfz $DIR_NAME.tar.gz $DIR_NAME

cp -rfv $DIR_NAME.tar.gz ~/Shared/hh
cp -rfv $DIR_NAME ~/Shared/hh

if [ "$1" == "--scp" ]; then
	scp $DIR_NAME.tar.gz ancevt@ancevt.ru:hh/
	scp -r $DIR_NAME ancevt@ancevt.ru:hh/
fi
