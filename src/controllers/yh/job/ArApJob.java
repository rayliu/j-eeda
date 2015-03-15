package controllers.yh.job;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.yh.returnOrder.ReturnOrderController;

public class ArApJob implements Job {
	private Logger logger = Logger.getLogger(ArApJob.class);
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		Record rec = Db.findFirst("select * from user_login");
		 logger.debug("你好, "+rec.getStr("user_name"));
	}

}
