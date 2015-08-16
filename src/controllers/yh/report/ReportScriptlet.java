package controllers.yh.report;

import net.sf.jasperreports.engine.JRDefaultScriptlet;

public class ReportScriptlet extends JRDefaultScriptlet {
	public String show(String name){
        return "my name is "+name;
	}
}
