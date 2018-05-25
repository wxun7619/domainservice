#!/bin/bash
#source common.sh
#set -e

dir=$(cd "$(dirname "$0")";pwd)
params=$*

getparamValue(){
    re=$(echo $params | awk -F "--$1=" '{print $2}' | awk -F " --" '{print $1}')
    echo $re
}

checkparamValue(){
    if [ "$2" = "" ]; then
        echo "错误:参数 --$1 的值不能为空,程序将退出"
        exit
    fi
}

if [ $# -lt 1 ]; then
    echo "当前脚本用于在k8s上部署应用
===================================================
脚本编写：童彬
制作时间：2017-05-31
上海隆腾信息技术有限公司版权所有
====================================================
参数表如下:
----------------------------------------------------
--appname          *应用程序名称
--image            *不带版本号的docker镜像URL路径
--version          *镜像版本号
--namespace        *应用程序所在的命名空间
--replicas         *需要部署应用程序的副本数量
--servicename      选填参数,自定义服务名称，如果不指定默认名称为appname-svc
--serviceport      选填参数,自定服务端口(--serviceport=svcPort:targetPort),指定后系统会尝试创建服务
--nodeport         选填参数,是否需要开放nodeport
--secretkey        选填参数,如果docker库需要认证,可以指令当前命名空间的secret
--ifNotExists=true 选填参数,表示仅在系统中不存在程序时才创建,否则忽略
--command          选填参数,表示镜像启动时的命令行
--memorylimit      选填参数,表示内存请求值及最大值(50Mi,300Mi)
--cpulimit         选填参数,表示cpu请求值及最大值(70m,200m)
=====================================================
    "
    exit
fi

appname=$(getparamValue appname)
checkparamValue appname $appname

image=$(getparamValue image)
checkparamValue image $image

version=$(getparamValue version)
checkparamValue version $appname

namespace=$(getparamValue namespace)
checkparamValue namespace $namespace

replicas=$(getparamValue replicas)
checkparamValue replicas $replicas

servicename=$(getparamValue servicename)
serviceport=$(getparamValue serviceport)
nodeport=$(getparamValue nodeport)
secretkey=$(getparamValue secretkey)
ifNotExists=$(getparamValue ifNotExists)
commands=$(getparamValue command)
memorylimit=$(getparamValue memorylimit)
cpulimit=$(getparamValue cpulimit)


echo "获得本次部署的参数如下:
  appname:     $appname
  image:       $image
  version:     $version
  namespace:   $namespace
  replicas:    $replicas
  servicename: $servicename
  serviceport: $serviceport
  nodeport:    $nodeport  
  secretkey:   $secretkey
  command:     $commands
  memorylimit: $memorylimit
  cpulimit:    $cpulimit
"

nmexists=$(kubectl get namespace $namespace -o name 2>/dev/null)
if [ "$nmexists" = "" ]; then
    kubectl create namespace $namespace
fi

if [ "$secretkey" != "" ]; then
    secretexists=$(kubectl get secret $secretkey -o name -n $namespace 2>/dev/null)
    if [ "$secretexists" = "" ]; then
        echo "错误:secret:\"$secretkey\"在命名空间\"$namespace\"中不存在."
        exit
    fi
fi

appdeployfile=$dir/$appname/$appname-deploy.yaml
mkdir -p $dir/$appname/

#建立部署文件
cat>$appdeployfile<<EOF
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: $appname
  namespace: $namespace
  labels:
    app: "$appname"
spec:
  replicas: $replicas
  selector:
    matchLabels:
      app: "$appname"
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 1
  minReadySeconds: 5
  revisionHistoryLimit: 2
  template:
    metadata:
      labels:
        app: "$appname"
    spec:
      containers:
      - name: "$appname"
        image: $image:$version
        imagePullPolicy: Always
EOF

if [ "$memorylimit" != "" ] || [ "$cpulimit" != "" ]; then
    reqmem=$(echo $memorylimit | awk -F , '{print $1}')
    limitmem=$(echo $memorylimit | awk -F , '{print $2}')
    reqcpu=$(echo $cpulimit | awk -F , '{print $1}')
    limitcpu=$(echo $cpulimit | awk -F , '{print $2}')
    echo "        resources:" >> $appdeployfile
    if [ "$limitmem" != "" ] || [ "$limitcpu" != "" ]; then
        echo "          limits:" >> $appdeployfile
        if [ "$limitmem" != "" ]; then
            echo "            memory: $limitmem" >> $appdeployfile
        fi
        if [ "$limitcpu" != "" ]; then
            echo "            cpu: $limitcpu" >> $appdeployfile
        fi        
    fi
    if [ "$reqmem" != "" ] || [ "$reqcpu" != "" ]; then
        echo "          requests:" >> $appdeployfile
        if [ "$reqmem" != "" ]; then
            echo "            memory: $reqmem" >> $appdeployfile
        fi
        if [ "$reqcpu" != "" ]; then
            echo "            cpu: $reqcpu" >> $appdeployfile
        fi        
    fi
fi

if [ "$commands" != "" ]; then
    echo "        command:" >> $appdeployfile
    for c in $commands; do
        echo "        - $c" >> $appdeployfile
    done
fi

if [ "$secretkey" != "" ]; then
    echo "      imagePullSecrets:" >> $appdeployfile
    echo "      - name: $secretkey" >> $appdeployfile
fi

deployexists=$(kubectl get deploy $appname -o name -n $namespace 2>/dev/null)

if [ "$deployexists" = "" ]; then
    kubectl create -f $appdeployfile
else
    if [ "$ifNotExists" != "true" ]; then
        kubectl replace -f $appdeployfile
    else echo "$appname 已经存在,跳过部署"
    fi
fi

if [ "$serviceport" = "" ]; then
    exit
fi

#var=$(echo $serviceport | bc 2>/dev/null)
#if [ "$var" != "$serviceport" ]; then
#    echo "错误: --serviceport=$serviceport, 设定的端口号不是有效的整数"
#    exit
#fi

if [ "$servicename" = "" ]; then
    servicename="$appname-svc"
fi

svcfile=$dir/$appname/$appname-svc.yaml
cat>$svcfile<<EOF
kind: Service
apiVersion: v1
metadata:
  labels:
    app: $appname
  name: $servicename
  namespace: $namespace
spec:
  selector:
    app: $appname
  ports:
EOF

ports=$(echo $serviceport | sed 's/,/ /g')
for s in $ports
do
    #| awk -F "--$1=" '{print $2}'
    port=$(echo $s | awk -F ":" '{print $1}')
    targetPort=$(echo $s | awk -F ":" '{print $2}')
    echo "  - name: port-$port" >> $svcfile
    echo "    port: $port" >> $svcfile
    if [ "$targetPort" != "" ]; then
        echo "    targetPort: $targetPort" >> $svcfile
    fi
done

if [ "$nodeport" != "" ]; then
    echo "    nodePort: $nodeport" >> $svcfile
    echo "  type: NodePort" >> $svcfile
fi

deployexists=$(kubectl get svc $servicename -o name -n $namespace 2>/dev/null)

if [ "$deployexists" = "" ]; then
    kubectl create -f $svcfile
else
    if [ "$ifNotExists" != "true" ]; then
        kubectl delete -f $svcfile
        kubectl create -f $svcfile
    else echo "$servicename 已经存在,跳过部署"
    fi
fi
