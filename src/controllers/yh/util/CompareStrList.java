package controllers.yh.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CompareStrList {
	public List compare(List<Object> tList,String[] strs){
		List<Object> list = new ArrayList<Object>();
		List<Object> temp = new ArrayList<Object>();
		
		List<String> strList = new ArrayList(Arrays.asList(strs));
		for (Object object : tList) {			
			for (Object obj : strList) {
				//判断不相等
				if(object.equals(obj)){
					temp.add(object);					
				}
			}
		}
		
		for (Object object : temp) {
			tList.remove(object);
			strList.remove(object);
		}
		
		list.add(0,tList);
		list.add(1,strList);
	
		
		return list;
	}
}
