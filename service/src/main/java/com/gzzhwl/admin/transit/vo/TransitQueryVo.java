package com.gzzhwl.admin.transit.vo;

import java.text.ParseException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Maps;
import com.gzzhwl.core.constant.ErrorCode;
import com.gzzhwl.core.constant.Global;
import com.gzzhwl.core.constant.OrderType;
import com.gzzhwl.core.data.model.OrderLoadInfo;
import com.gzzhwl.core.utils.DateUtils;
import com.gzzhwl.rest.exception.RestException;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TransitQueryVo {
	private String queryType;//0-快捷搜索  1-高级搜索
	private String keyWord;//关键字
	private String customerId;//客户ID
	private String startCodeP;// 线路（起）-省
	private String startCodeC;// 线路（起）-市
	private String endCodeP;// 线路（止）-省
	private String endCodeC;// 线路（止）-市
	private String customerBillNo;//客户单号
	private String tripTimeStart;//实际发车时间（起）
	private String tripTimeEnd;//实际发车时间（止）
	private String realName;//司机姓名
	private String telphone;//司机电话
	private String tripSort;//按发车时间排序   00-按发车时间升序   01-按发车时间降序
	private String arriveSort;//按计划到达时间排序  00-按计划到达时间升序  01-按计划到达时间降序
	
	private static final String QUERYTYPE_NORMAL = "0";
	private static final String QUERYTYPE_SENIOR = "1";
	
	
	
	public Map<String, Object> getParam() throws ParseException {
		Map<String, Object> params = Maps.newHashMap();
		if(QUERYTYPE_NORMAL.equals(queryType)) {
			if(StringUtils.isNotBlank(keyWord)) {
				params.put("keyWord", keyWord);
			}
		}else if(QUERYTYPE_SENIOR.equals(queryType)) {
			if(StringUtils.isNotBlank(customerId)) {
				params.put("customerId", customerId);
			}
			
			if(StringUtils.isNotBlank(startCodeP)) {
				params.put("startCodeP", startCodeP);
			}
			
			if(StringUtils.isNotBlank(startCodeC)) {
				params.put("startCodeC", startCodeC);
			}
			
			if(StringUtils.isNotBlank(endCodeP)) {
				params.put("endCodeP", endCodeP);
			}
			
			if(StringUtils.isNotBlank(endCodeC)) {
				params.put("endCodeC", endCodeC);
			}
			
			if(StringUtils.isNotBlank(customerBillNo)) {
				params.put("customerBillNo", customerBillNo);
			}
			
			if(StringUtils.isNotBlank(tripTimeStart)) {
				params.put("tripTimeStart", DateUtils.getStartTime(tripTimeStart));
			}
			
			if(StringUtils.isNotBlank(tripTimeEnd)) {
				params.put("tripTimeEnd", DateUtils.getEndTime(tripTimeEnd));
			}
			
			if(StringUtils.isNotBlank(realName)) {
				params.put("realName", "%"+realName+"%");
			}
			
			if(StringUtils.isNotBlank(telphone)) {
				params.put("telphone", telphone);
			}
			
		}else {
			throw new RestException(ErrorCode.ERROR_900006);
			
		}
		if(StringUtils.isNotBlank(tripSort)) {
			params.put("tripSort", tripSort);
		}
		
		if(StringUtils.isNotBlank(arriveSort)) {
			params.put("arriveSort", arriveSort);
		}
		params.put("isDeleted", Global.ISDEL_NORMAL.toString());
		//只查出在途的订单
		params.put("status", OrderType.ON_PASSAGE.toString());
		params.put("loadType", OrderLoadInfo.LOAD_BILL);
		return params;
	}
	
	
}
