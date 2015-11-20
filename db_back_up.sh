#!/bin/sh  

filename=`date +%Y%m%d-%H%M%S`_yh.sql

# $1:db IP   $2:use_name  $3:pwd  $4:db_name
mysqldump -h$1 -u$2 -p$3 $4>/var/db/$filename

# 参考 http://blog.csdn.net/liumangxiong/article/details/7084637
#crontab修改

#crontab -e 
# */1 6-23 * * * /root/.jenkins/jobs/TMS-DailyBuild/workspace/monitor_uat.sh  
# 上面的意思是每天6点到23点之间每分钟调用一下脚本monitor.sh

#$crontab -l 列出用户目前的crontab
#$crontab -e 编辑     
#在nano帮助文档里，Ctrl-键被表示为一个脱字符（^）,如 Ctrl-X 为退出