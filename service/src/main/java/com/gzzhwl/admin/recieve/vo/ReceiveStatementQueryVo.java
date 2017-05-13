package com.gzzhwl.admin.recieve.vo;

import java.text.ParseException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Maps;
import com.gzzhwl.core.constant.ConsignType;
import com.gzzhwl.core.constant.LoadBillType;
import com.gzzhwl.core.utils.DateUtils;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ReceiveStatementQueryVo {
	private String customerId;// 客户ID
	private String startCodeP;// 线路（起）-省
	private String startCodeC;// 线路（起）-市
	private String endCodeP;// 线路（止）-省
	private String endCodeC;// 线路（止）-市
	private String departId;// 业务部门
	private String tripStartTime;// 发车时间（起）
	private String tripEndTime;// 发车时间（止）
	private String isPredict;// 运费是否预估

	public Map<String, Object> getParams() throws ParseException {
		Map<String, Object> params = Maps.newHashMap();
		if (StringUtils.isNotBlank(customerId)) {
			params.put("customerId", customerId);
		}
		if (StringUtils.isNotBlank(startCodeP)) {
			params.put("startCodeP", startCodeP);
		}
		if (StringUtils.isNotBlank(startCodeC)) {
			params.put("startCodeC", startCodeC);
		}
		if (StringUtils.isNotBlank(endCodeP)) {
			params.put("endCodeP", endCodeP);
		}
		if (StringUtils.isNotBlank(endCodeC)) {
			params.put("endCodeC", endCodeC);
		}
		if (StringUtils.isNotBlank(departId)) {
			params.put("departId", departId);
		}
		if (StringUtils.isNotBlank(tripStartTime)) {
			params.put("tripStartTime", DateUtils.getStartTime(tripStartTime));
		}
		if (StringUtils.isNotBlank(tripEndTime)) {
			params.put("tripEndTime", DateUtils.getEndTime(tripEndTime));
		}
		if (StringUtils.isNotBlank(isPredict)) {
			params.put("isPredict", isPredict);
		}

		params.put("status", ConsignType.CONSIGN_VERIFIED.getCode());
		params.put("loadStatusArray", new String[] { LoadBillType.ELECRECEIPT.getCode(),
				LoadBillType.PRINTRECEIPT.getCode(), LoadBillType.CLOSED.getCode() });
		return params;
	}
}
