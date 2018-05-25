#!/bin/bash
#source common.sh
#set -e

dir=$(cd "$(dirname "$0")";pwd)
params=$*

getparamValue(){
    re=$(echo $params | awk -F "--$1=" '{print $2}' | awk '{print $1}')
    echo $re
}

checkparamValue(){
    if [ "$2" = "" ]; then
        echo "错误:参数 --$1 的值不能为空,程序将退出"
        exit
    fi
}

if [ $# -lt 1 ]; then
    echo "当前脚本用于构建Docker镜像
===================================================
脚本编写：童彬
制作时间：2017-05-31
上海隆腾信息技术有限公司版权所有
====================================================
参数表如下:
----------------------------------------------------
--from             *docker镜像底包
--tagname          *docker镜像标识名称
--tagver           *docker镜像标识版本号
--sourcedir        需要COPY到镜像中的文件
--imagedir         COPY到镜像后的文件
--startparam       镜像启动参数(shell命令语句)
--expose           导出端口
=====================================================
    "
    exit
fi

from=$(getparamValue from)
checkparamValue from $from

tagname=$(getparamValue tagname)
checkparamValue tagname $tagname

tagver=$(getparamValue tagver)
checkparamValue tagver $tagver

sourcedir=$(getparamValue sourcedir)
imagedir=$(getparamValue imagedir)
startparam=$(getparamValue startparam)
expose=$(getparamValue expose)
mkdir -p dockerbuild/sourcefiles
cp -rf $sourcedir/* ./dockerbuild/sourcefiles/
cd dockerbuild

cat>boot.sh<<EOF
#!/bin/sh
EOF
chmod a+x boot.sh

if [ "$startparam" != "" ]; then
    echo $startparam | sed 's/\,/ /g' >> boot.sh
fi

cat > Dockerfile <<EOF
FROM $from
MAINTAINER lonntec.com
ENV LANG C.UTF-8

EOF
if [ "$sourcedir" != "" ]; then
    if [ "$imagedir" = "" ]; then
        echo "imagedir参数不能为空."
        exit
    else
        echo "RUN mkdir -p $imagedir" >> Dockerfile
        echo "COPY ./sourcefiles/ $imagedir" >> Dockerfile
        if [ "$expose" != "" ]; then
            echo "EXPOSE $expose" >> Dockerfile
        fi
    fi
fi
echo "COPY boot.sh /" >> Dockerfile

cat >> Dockerfile <<EOF
ENTRYPOINT ["/bin/sh", "/boot.sh"]
EOF

docker build -t "$tagname:$tagver" .
rm -rf boot.sh Dockerfile
docker push $tagname:$tagver
cd $dir
rm -rf dockerbuild
