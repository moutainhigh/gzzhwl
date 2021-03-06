package com.gzzhwl.admin.load.validator;

import org.apache.commons.lang.StringUtils;

import com.gzzhwl.core.constant.ErrorCode;
import com.gzzhwl.rest.exception.RestException;

public class ArrivedValidate {
	public static void valid_load_exist(boolean exist) {
		if(!exist) {
			throw new RestException(ErrorCode.ERROR_110002);
		}
	}
	
	public static void valid_receipt_exist(boolean exist) {
		if(!exist) {
			throw new RestException(ErrorCode.ERROR_110010);
		}
	}
	
	public static void valid_load_id(String loadId) {
		if (StringUtils.isBlank(loadId)) {
			throw new RestException(ErrorCode.ERROR_110001);
		}
	}
	
	public static void valid_receipt_id(String receiptId) {
		if (StringUtils.isBlank(receiptId)) {
			throw new RestException(ErrorCode.ERROR_900003.getCode(), "回单ID不能为空");
		}
	}
}
