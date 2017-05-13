package com.gzzhwl.core.constant;

import com.gzzhwl.core.exception.NotFoundEnumException;

public enum LoadBillType {

	NOTVEHICLE("01", "待车检"), 
	
	VEHICLECHECK("02", "已车检"), 
	
	CLOSETOSURFACE("03", "已靠台"), 
	
	DEPART("05", "在途中"),
	
	CANCEL_TRIP("06","已取消"), 
	
	UNLOAD("07", "待配载"),
	
	CANCEL("08","已取消"),
	
	HAS_LOAD("09","已配载"),
	
	HAS_INVALID("10","YSJ已作废"),
	
	ARRIVED("11", "到达"),
	
	ELECRECEIPT("12", "电子回单审核通过"),
	
	PRINTRECEIPT("13", "收到纸质回单"),
	
	CLOSED("14", "关闭")
	;
	
	private String code;
	private String desc;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	private LoadBillType(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public String toString() {
		return this.code;
	}

	public static LoadBillType getLoadBillType(String code) throws NotFoundEnumException {
		for (LoadBillType lt : values()) {
			if (code.equals(lt.getCode())) {
				return lt;
			}
		}
		throw new NotFoundEnumException();
	}
}
