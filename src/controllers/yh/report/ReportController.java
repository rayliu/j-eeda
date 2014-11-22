package controllers.yh.report;

import java.util.HashMap;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.log4j.Logger;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.DbKit;

public class ReportController extends Controller {

    private Logger logger = Logger.getLogger(ReportController.class);

    public void index() {
        // this.getRequest().getContextPath()
        String fileName = "report/TransferOrder.jasper";
        String outFileName = "report/test.pdf";
        HashMap<String, Object> hm = new HashMap<String, Object>();
        hm.put("order_no", "YS2014042600002");
        try {

            JasperPrint print = JasperFillManager.fillReport(fileName, hm, DbKit.getConnection());
            JasperExportManager.exportReportToPdfFile(print, outFileName);

        } catch (Exception e) {
            e.printStackTrace();
        }

        renderFile(outFileName);
    }

}
