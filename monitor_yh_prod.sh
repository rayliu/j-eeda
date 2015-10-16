#!/bin/sh  
  
host_dir="/root/.jenkins/workspace/build-YH"     # 当前用户根目录  
proc_name="yh_prod"                                     # 进程名  
file_name="/logs/cron.log"                         # 日志文件  
pid=0  


proc_num()                                              # 计算进程数  
{  
    num=`ps -ef | grep $proc_name | grep -v grep | wc -l`  # wc -l 统计行数。
    #这里在shell 中执行=2， 实际在命令行中运行=0，why???
    return $num  
}  
  
proc_id()                                               # 进程号  
{  
    pid=`ps -ef | grep $proc_name | grep -v grep | awk '{print $2}'`  
}  
  
proc_num  
number=$?
echo proc_num = $number
if [ $number -eq 2 ]                                # 判断进程是否存在  
then   
    cd /root/.jenkins/workspace/build-YH; nohup ant yh_prod >/dev/null 2>&1 &    # 重启进程的命令，请相应修改  
    proc_id                                         # 获取新进程号 
    echo ${pid}, `date` >> $host_dir$file_name      # 将新进程号和重启时间记录  
fi  

# 参考 http://blog.csdn.net/liumangxiong/article/details/7084637
#crontab修改

# chen@IED_40_125_sles10sp1:~/CandyAT/Bin> 
#crontab -e 
# */1 6-23 * * * /root/.jenkins/jobs/TMS-DailyBuild/workspace/monitor_uat.sh  
# 上面的意思是每天6点到23点之间每分钟调用一下脚本monitor.sh

#$crontab -l 列出用户目前的crontab
#$crontab -e 编辑     :w 保存   :q退出  