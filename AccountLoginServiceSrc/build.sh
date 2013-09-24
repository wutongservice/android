#!/bin/sh
#DEBUG On/Off
#set -x
#
# AUTO sign apk by ant tool
# put below ant configuration in the project folder so that ANT can sign the apk automatically
# filename: "<project folder>/ant.properties"
#       key.store=<your key file path>
#       key.alias=<your signature alias name>
#       key.store.password=<your store password>
#       key.alias.password=<your alias password>
#
# NOTE for ANT tool
#  You shall have ant v1.8 or later version installed (you can use binary release(it is java)
#   source : http://www.apache.org/dist/ant/source/apache-ant-1.8.3-src.tar.gz
#   binary : http://labs.mop.com/apache-mirror//ant/binaries/apache-ant-1.8.3-bin.tar.gz
#
CUR_PATH_SAVE=`pwd`
#export ANDROID_SDK="$HOME/Downloads/android-sdk-linux"
#export JAVA_HOME="/usr/local/jdk1.6"
export ANDROID_SDK="$HOME/and_sdk4"
export JAVA_HOME="/usr/java/jdk1.6.0_30"

cd $CUR_PATH_SAVE
# do clean build
rm -rf bin/*
rm -rf gen/*
rm -rf out/*

if [ -f ./build.xml ]; then
    rm build.xml
fi

# start build & sign the apk if "ant.properties" is defined in project folder
$ANDROID_SDK/tools/android update project --name accountlogin --path .
ant release

mkdir out
cp -f bin/classes.jar out/acls.jar

OUT_DIR="out/AccountLoginService"
mkdir $OUT_DIR 
cp -fr assets $OUT_DIR/
cp -fr res $OUT_DIR/
cp project.properties $OUT_DIR/
cp AndroidManifest.xml $OUT_DIR/

mkdir $OUT_DIR/libs
cp -f bin/classes.jar $OUT_DIR/libs/acls.jar

cd $CUR_PATH_SAVE
#END

