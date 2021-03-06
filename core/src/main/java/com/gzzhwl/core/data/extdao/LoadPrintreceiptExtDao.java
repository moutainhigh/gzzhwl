package com.gzzhwl.core.data.extdao;

import java.util.Map;

import com.gzzhwl.core.data.model.LoadPrintreceipt;


public interface LoadPrintreceiptExtDao {
	public final static String PREFIX = LoadPrintreceiptExtDao.class.getName();
	
	public LoadPrintreceipt getPrintreceipt(Map<String, Object> params);
	
	public Map<String,Object> getCurrentPrintreceipt(String loadId);
}
