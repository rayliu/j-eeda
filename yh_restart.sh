#!/bin/sh  

#build and restart tomcat

git_dir="/var/git"                            # git目录
tomcat_dir="/alidata/server/tomcat7/bin"     # tomcat目录  
proc_name="tomcat"                           # 进程名  
file_name="/logs/build.log"                  # 日志文件  
pid=0  


proc_num()                                              # 计算进程数  
{  
    num=`ps -ef | grep $proc_name | grep -v grep | wc -l`  # wc -l 统计行数。
    echo $num
    return $num  
}  
  
proc_id()                                               # 进程号  
{  
    pid=`ps -ef | grep $proc_name | grep -v grep | awk '{print $2}'`  
}  
  
proc_num
number=$? 
if [ $number -eq 1 ]                                # 判断进程是否存在  
then
    proc_id
    kill ${pid}                                     # 关闭tomcat
fi

cd $git_dir                                     # 进入git目录  
git pull origin develop                         # 获取最新代码
ant yh-prod                                     # 编译最新代码 
$tomcat_dir/startup.sh                          # 启动tomcat 

proc_id
echo ${pid}, `date` >> $host_dir$file_name      # 将新进程号和重启时间记录