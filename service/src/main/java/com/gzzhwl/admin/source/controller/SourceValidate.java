package com.gzzhwl.admin.source.controller;

import com.gzzhwl.core.constant.ErrorCode;
import com.gzzhwl.rest.exception.RestException;

public class SourceValidate {
	public static void valid_source_exist(boolean exist) {
		if(!exist) {
			throw new RestException(ErrorCode.ERROR_300001);
		}
	}
	
	public static void valid_order_exist(boolean exist) {
		if(!exist) {
			throw new RestException(ErrorCode.ERROR_400002);
		}
	}
}
