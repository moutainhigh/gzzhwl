package com.gzzhwl.api.quoted.service.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.gzzhwl.admin.load.service.LoadLinkStatusService;
import com.gzzhwl.admin.quoted.service.QuotedHisService;
import com.gzzhwl.admin.quoted.service.QuotedManageService;
import com.gzzhwl.admin.remark.service.RemarkInfoService;
import com.gzzhwl.admin.source.service.SourceLinkStatusService;
import com.gzzhwl.api.agent.service.AgentInfoService;
import com.gzzhwl.api.common.service.CommonSourceService;
import com.gzzhwl.api.quoted.service.QuotedService;
import com.gzzhwl.api.quoted.vo.FinishQuotedVo;
import com.gzzhwl.api.source.service.ApiSourceService;
import com.gzzhwl.api.utils.CardIdConvert;
import com.gzzhwl.common.model.FlowActionCategory;
import com.gzzhwl.common.model.ZHFlow;
import com.gzzhwl.common.service.FlowsService;
import com.gzzhwl.common.service.RegionService;
import com.gzzhwl.core.constant.ErrorCode;
import com.gzzhwl.core.constant.Global;
import com.gzzhwl.core.constant.QuotedType;
import com.gzzhwl.core.data.dao.OrderSourceInfoDao;
import com.gzzhwl.core.data.dao.QuotedInfoDao;
import com.gzzhwl.core.data.dao.QuotedPlusInfoDao;
import com.gzzhwl.core.data.extdao.OrderInfoExtDao;
import com.gzzhwl.core.data.extdao.QuotedInfoExtDao;
import com.gzzhwl.core.data.model.FlowDef;
import com.gzzhwl.core.data.model.OrderSourceInfo;
import com.gzzhwl.core.data.model.QuotedInfo;
import com.gzzhwl.core.data.model.QuotedPlusInfo;
import com.gzzhwl.core.data.model.RegionInfo;
import com.gzzhwl.core.page.Page;
import com.gzzhwl.core.utils.IdentifierUtils;
import com.gzzhwl.core.utils.ValidateUtils;
import com.gzzhwl.rest.exception.RestException;
import com.gzzhwl.tms.service.TMSApiService;

@Service
public class QuotedServiceImpl implements QuotedService {

	@Autowired
	private QuotedInfoDao quotedInfoDao;
	@Autowired
	private OrderSourceInfoDao orderSourceInfoDao;
	@Autowired
	private FlowsService flowsService;
	@Autowired
	private SourceLinkStatusService sourceLinkStatusService;
	@Autowired
	private QuotedHisService quotedHisService;
	@Autowired
	private QuotedPlusInfoDao quotedPlusInfoDao;
	@Autowired
	private QuotedInfoExtDao quotedInfoExtDao;
	@Autowired
	private ApiSourceService apiSourceService;
	@Autowired
	@Qualifier("QuoteCardService")
	private CommonSourceService quoteCardService;
	@Autowired
	private Mapper beanMapper;
	@Autowired
	private LoadLinkStatusService loadLinkStatusService;
	@Autowired
	private OrderInfoExtDao orderInfoExtDao;
	@Autowired
	private RegionService regionService;
	@Autowired
	private AgentInfoService agentInfoService;
	@Autowired
	private QuotedManageService quotedManageService;
	@Autowired
	private RemarkInfoService remarkInfoService;
	@Autowired
	private TMSApiService tmsApiService;
	// 全部订单
	private final String ORDER_ALL = "0";
	// 待完善订单
	private final String ORDER_NOTIMPROVE = "1";
	// 待到达订单
	private final String ORDER_ONLINE = "2";
	// 结算订单
	private final String ORDER_COMPLETE = "3";

	@Override
	public String addQuoted(String accountId, String sourceId, String price, String remark) {

		// //验证是否冻结
		// if(!accountService.isFrozen(accountId)){
		// throw new RestException(ErrorCode.ERROR_900017.getCode(),
		// ErrorCode.ERROR_900017.getDesc());
		// }
		// 验证账号是否审核通过
		if (!agentInfoService.isPassed(accountId)) {

			Map<String, Object> agentMap = agentInfoService.getAgentInfoByCondition(accountId);

			if (agentMap == null) {
				throw new RestException(ErrorCode.ERROR_900026);
			}
			String status = (String) agentMap.get("status");

			if (status.equals(Global.STATUS_NOT_PASSED.toString())) {
				throw new RestException(ErrorCode.ERROR_900025);
			}
			if (status.equals(Global.STATUS_WAIT.toString())) {
				throw new RestException(ErrorCode.ERROR_900017);
			}

		}

		if (ValidateUtils.isEmpty(price)) {
			throw new RestException(ErrorCode.ERROR_900003.getCode(), "报价金额" + ErrorCode.ERROR_900003.getDesc());
		}
		if (!apiSourceService.allowBid(sourceId, accountId)) {
			throw new RestException(ErrorCode.ERROR_500001);
		}
		if (!NumberUtils.isNumber(price)) {
			throw new RestException(ErrorCode.ERROR_900005.getCode(), "报价金额" + ErrorCode.ERROR_900005.getDesc());
		}

		if (NumberUtils.createDouble(price) < 0) {
			throw new RestException(ErrorCode.ERROR_500005);
		}
		if (NumberUtils.createDouble(price) > 100000) {
			throw new RestException(ErrorCode.ERROR_500006);
		}

		FlowDef flowdef_start = flowsService.startFlow(ZHFlow.QUOTED, FlowActionCategory.YSJ);

		// FlowDef flowdef = flowsService.executeFlow(ZHFlow.QUOTED, "11",
		// flowdef_start.getLinkedStatus(), FlowActionCategory.YSJ);
		QuotedInfo quotedInfo = new QuotedInfo();
		String quotedId = IdentifierUtils.getId().generate().toString();
		quotedInfo.setQuotedId(quotedId);
		quotedInfo.setAccountId(accountId);
		quotedInfo.setCreatedBy(accountId);
		quotedInfo.setIsDeleted(Global.ISDEL_NORMAL.toString());
		quotedInfo.setPrice(new DecimalFormat("#0.00").format(Double.parseDouble(price)));
		quotedInfo.setRemark(remark);
		quotedInfo.setSourceId(sourceId);
		quotedInfo.setStatus(flowdef_start.getTransitionStatus());
		quotedInfo.setUpdatedBy(accountId);
		quotedInfoDao.insert(quotedInfo);

		quotedHisService.saveQuotedHis(quotedId, flowdef_start.getLinkedStatus(), flowdef_start.getTransitionStatus(),
				FlowActionCategory.YSJ, flowdef_start.getMsgTemplate(), accountId);

		return quotedId;
	}

	public void repeatQuotedInfoValidator(String accountId, String sourceId) {

		Map<String, Object> params = new HashMap<>();
		params.put("accountId", accountId);
		params.put("sourceId", sourceId);

		List<Map<String, Object>> listMap = quotedInfoDao.find(params);

		if (ValidateUtils.isEmpty(listMap)) {
			return;
		}

		for (int i = 0; i < listMap.size(); i++) {

			String status = listMap.get(i).get("status").toString();

			if (status.equals(QuotedType.ONLINE.getCode()) || status.equals(QuotedType.ONLINE_TRIP.getCode())
					|| status.equals(QuotedType.QUOTED.getCode()) || status.equals(QuotedType.NOTIMPROVE.getCode())
					|| status.equals(QuotedType.INVALID.getCode()) || status.equals(QuotedType.COMPLETE.getCode())) {
				throw new RestException(ErrorCode.ERROR_500001);
			}

		}

	}

	@Override
	public String closeQuotedInfo(String sourceId, String accountId) {

		QuotedInfo quotedInfo = getMyQuoted(sourceId, accountId);

		if (quotedInfo == null) {
			throw new RestException(ErrorCode.ERROR_900013);
		}

		FlowDef flowdef = flowsService.executeFlow(ZHFlow.QUOTED, "12", quotedInfo.getStatus(), FlowActionCategory.YSJ);

		quotedInfo.setStatus(flowdef.getTransitionStatus());

		quotedInfoDao.updateSelective(quotedInfo);

		quotedHisService.saveQuotedHis(quotedInfo.getQuotedId(), flowdef.getLinkedStatus(),
				flowdef.getTransitionStatus(), FlowActionCategory.YSJ, flowdef.getMsgTemplate(), accountId);

		return quotedInfo.getQuotedId();
	}

	@Override
	public Integer getQuotedCountBySourceId(String sourceId) {

		Map<String, Object> params = new HashMap<>();
		params.put("sourceId", sourceId);

		return quotedInfoDao.find(params).size();
	}

	@Override
	public String finishQuoted(FinishQuotedVo finishQuotedVo, String accountId) {

		String sourceId = finishQuotedVo.getSourceId();

		QuotedInfo quotedInfo = getMyQuoted(sourceId, accountId);

		if (quotedInfo == null) {
			throw new RestException(ErrorCode.ERROR_900013);
		}

		FlowDef flowdef = flowsService.executeFlow(ZHFlow.QUOTED, "14", quotedInfo.getStatus(), FlowActionCategory.YSJ);

		quotedInfo.setStatus(flowdef.getTransitionStatus());
		quotedInfo.setUpdatedBy(accountId);
		quotedInfoDao.updateSelective(quotedInfo);
		// 完善报价司机车辆关系
		finishQuotedPlusInfo(finishQuotedVo, quotedInfo.getQuotedId(), sourceId);
		// 修改货源状态
		sourceLinkStatusService.avttSource(quotedInfo.getSourceId(), accountId);
		// 创建提货单
		String loadId = loadLinkStatusService.createLoadFromYSJ(quotedInfo.getQuotedId(), accountId);
		quotedHisService.saveQuotedHis(quotedInfo.getSourceId(), flowdef.getLinkedStatus(),
				flowdef.getTransitionStatus(), FlowActionCategory.YSJ, flowdef.getMsgTemplate(), accountId);
		tmsApiService.doFinishBid(loadId);
		return quotedInfo.getQuotedId();
	}

	private void finishQuotedPlusInfo(FinishQuotedVo finishQuotedVo, String quotedId, String sourceId) {
		OrderSourceInfo orderSourceInfo = orderSourceInfoDao.get(sourceId);

		if (orderSourceInfo == null) {
			throw new RestException(ErrorCode.ERROR_500003);
		}

		String vehicleInfoId = finishQuotedVo.getVehicleInfoId();

		if (ValidateUtils.isEmpty(vehicleInfoId)) {
			throw new RestException(ErrorCode.ERROR_800001);
		}

		if (ValidateUtils.isEmpty(finishQuotedVo.getBidDriverInfoList())) {
			throw new RestException(ErrorCode.ERROR_800002);
		}

		String driverNum = orderSourceInfo.getNeedDriverNum();
		Integer num = NumberUtils.createInteger(driverNum);
		int dirverNum = finishQuotedVo.getBidDriverInfoList().size();
		if (num == null) {
			throw new RestException(ErrorCode.ERROR_900010);
		}
		if (dirverNum != num) {
			throw new RestException(ErrorCode.ERROR_900004.getCode(), "需要选择" + num + "位司机");
		}
		for (int i = 0; i < dirverNum; i++) {
			QuotedPlusInfo quotedPlusInfo = new QuotedPlusInfo();
			quotedPlusInfo.setVehicleInfoId(vehicleInfoId);
			quotedPlusInfo.setQuotedId(quotedId);
			quotedPlusInfo.setDriverInfoId(finishQuotedVo.getBidDriverInfoList().get(i).getDriverInfoId());
			quotedPlusInfoDao.insert(quotedPlusInfo);
		}

	}

	@Override
	public String invalidTheBid(String sourceId, String accountId) {

		QuotedInfo quotedInfo = getMyQuoted(sourceId, accountId);

		if (quotedInfo == null) {
			throw new RestException(ErrorCode.ERROR_900013);
		}

		FlowDef flowdef = flowsService.executeFlow(ZHFlow.QUOTED, "16", quotedInfo.getStatus(), FlowActionCategory.YSJ);

		quotedInfo.setStatus(flowdef.getTransitionStatus());
		quotedInfo.setUpdatedBy(accountId);
		quotedInfoDao.updateSelective(quotedInfo);

		sourceLinkStatusService.breakPromiseSourceByUser(quotedInfo.getSourceId(), accountId);

		quotedHisService.saveQuotedHis(quotedInfo.getQuotedId(), quotedInfo.getStatus(), flowdef.getTransitionStatus(),
				FlowActionCategory.YSJ, flowdef.getMsgTemplate(), accountId);

		return quotedInfo.getQuotedId();
	}

	@Override
	public QuotedInfo getMyQuoted(String sourceId, String accountId) {

		if (ValidateUtils.isEmpty(sourceId)) {
			throw new RestException(ErrorCode.ERROR_500003);
		}

		if (ValidateUtils.isEmpty(accountId)) {
			throw new RestException(ErrorCode.ERROR_900003.getCode(), "accountId" + ErrorCode.ERROR_900003.getDesc());
		}

		Map<String, Object> params = new HashMap<>();
		params.put("sourceId", sourceId);
		params.put("accountId", accountId);
		params.put("isDeleted", Global.ISDEL_NORMAL.toString());
		params.put("invalidStatus", QuotedType.CLOSED.getCode());

		List<Map<String, Object>> listMap = quotedInfoExtDao.find(params);

		if (ValidateUtils.isEmpty(listMap)) {
			return null;
		}

		return beanMapper.map(listMap.get(0), QuotedInfo.class);
	}

	@Override
	public Page<Map<String, Object>> getQuotedOrderList(String sourceType, String accountId, int pageIndex,
			int pageSize) {

		Map<String, Object> paraMap = new HashMap<>();

		String[] strArray = null;

		if (sourceType.equals(ORDER_ALL)) {
			strArray = new String[] { QuotedType.QUOTED.getCode(), QuotedType.CLOSED.getCode(),
					QuotedType.INVALID.getCode(), QuotedType.NOTIMPROVE.getCode(), QuotedType.ONLINE.getCode(),
					QuotedType.SYSCLOSE.getCode(), QuotedType.ENDDING.getCode(), QuotedType.CANCLETRIP.getCode(),
					QuotedType.ONLINE_TRIP.getCode(), QuotedType.COMPLETE.getCode() };
		} else if (sourceType.equals(ORDER_NOTIMPROVE)) {
			strArray = new String[] { QuotedType.NOTIMPROVE.getCode() };
		} else if (sourceType.equals(ORDER_ONLINE)) {
			strArray = new String[] { QuotedType.ONLINE.getCode(), QuotedType.ONLINE_TRIP.getCode() };
		} else if (sourceType.equals(ORDER_COMPLETE)) {
			strArray = new String[] { QuotedType.COMPLETE.getCode() };
		} else {
			throw new RestException(ErrorCode.ERROR_900006);
		}
		paraMap.put("quotedStatusList", strArray);
		paraMap.put("accountId", accountId);
		paraMap.put("startTime", "now()");
		paraMap.put("isDeleted", Global.ISDEL_NORMAL.toString());
		Page<Map<String, Object>> resPage = quotedInfoExtDao.getQuotedList(paraMap, pageIndex, pageSize);

		List<Map<String, Object>> sourceIdList = resPage.getRows();

		if (CollectionUtils.isNotEmpty(sourceIdList)) {
			String[] proIds = CardIdConvert.getColumnData(sourceIdList, "quotedId");
			List<Map<String, Object>> resultList = quoteCardService.getCardInfo(proIds, accountId);
			int dataSize = proIds.length;
			for (int i = 0; i < dataSize; i++) {
				Map<String, Object> current = sourceIdList.get(i);
				current.putAll(resultList.get(i));
			}
		}

		// 返回功能按钮
		CollectionUtils.transform(resPage.getRows(), new Transformer<Map<String, Object>, Map<String, Object>>() {
			@Override
			public Map<String, Object> transform(Map<String, Object> input) {

				String status = (String) input.get("status");

				input.put("actionList", flowsService.whatToDo(ZHFlow.QUOTED, status, FlowActionCategory.YSJ));

				return input;
			}
		});

		return resPage;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getQuotedOrderDetail(String quotedId, String accountId) {

		QuotedInfo quotedInfo = quotedInfoDao.get(quotedId);

		if (quotedInfo == null) {
			throw new RestException(ErrorCode.ERROR_900013);
		}

		Map<String, Object> resMap = beanMapper.map(quotedInfo, Map.class);

		String[] strArray = new String[] { quotedInfo.getQuotedId() };

		List<Map<String, Object>> resultList = quoteCardService.getCardInfo(strArray, accountId);

		if (!ValidateUtils.isEmpty(resultList)) {
			// 返回功能按钮
			String status = (String) resultList.get(0).get("status");

			if (QuotedType.getQuotedType(status).equals(QuotedType.ONLINE)
					|| QuotedType.getQuotedType(status).equals(QuotedType.ONLINE_TRIP)
					|| QuotedType.getQuotedType(status).equals(QuotedType.ENDDING)
					|| QuotedType.getQuotedType(status).equals(QuotedType.COMPLETE)) {
				Map<String, Object> loadMap = this.getLoadBillByQuotedId(quotedId);
				resMap.put("loadInfo", loadMap);
				if (loadMap != null) {
					resMap.put("onLineRecord", remarkInfoService.getOnlineRecordNew((String) loadMap.get("loadId")));
				} else {
					resMap.put("onLineRecord", null);
				}
			} else {
				resMap.put("loadInfo", null);
				resMap.put("onLineRecord", null);
			}

			if (QuotedType.getQuotedType(status).equals(QuotedType.NOTIMPROVE)
					|| QuotedType.getQuotedType(status).equals(QuotedType.ONLINE)
					|| QuotedType.getQuotedType(status).equals(QuotedType.ONLINE_TRIP)
					|| QuotedType.getQuotedType(status).equals(QuotedType.COMPLETE)) {
				Map<String, Object> bidVehicleInfo = quotedManageService.getBidVehicleInfo(quotedInfo.getSourceId());
				resMap.put("bidVehicleInfo", bidVehicleInfo);
			} else {
				resMap.put("bidVehicleInfo", null);
			}

			// 报价状态不是已报价则前台不显示报价时间
			if (QuotedType.getQuotedType(status).equals(QuotedType.QUOTED)) {
				resultList.get(0).put("isShowTime", true);
			} else {
				resultList.get(0).put("isShowTime", false);
			}

			resultList.get(0).put("actionList", flowsService.whatToDo(ZHFlow.QUOTED, status, FlowActionCategory.YSJ));

			resMap.putAll(resultList.get(0));
		}

		return resMap;
	}

	@Override
	public Map<String, Object> getOrderCount(String accountId) {

		if (ValidateUtils.isEmpty(accountId)) {
			throw new RestException(ErrorCode.ERROR_900013);
		}

		Map<String, Object> countMap = new HashMap<>();

		Integer noDriverCount = quotedInfoExtDao.getOrderCount(accountId,
				new String[] { QuotedType.NOTIMPROVE.getCode() });
		Integer onlineCount = quotedInfoExtDao.getOrderCount(accountId,
				new String[] { QuotedType.ONLINE.getCode(), QuotedType.ONLINE_TRIP.getCode() });
		// 已完成
		Integer completeCount = quotedInfoExtDao.getOrderCount(accountId,
				new String[] { QuotedType.COMPLETE.getCode() });
		countMap.put("noDriverCount", noDriverCount);
		countMap.put("onLineCount", onlineCount);
		countMap.put("notSettleCount", completeCount);

		return countMap;
	}

	@Override
	public Map<String, Object> getLoadBillByQuotedId(String quotedId) {

		if (ValidateUtils.isEmpty(quotedId)) {
			throw new RestException(ErrorCode.ERROR_500004.getCode(), ErrorCode.ERROR_500004.getDesc());
		}

		Map<String, Object> resMap = orderInfoExtDao.getLoadBillByQuotedId(quotedId);

		if (!ValidateUtils.isEmpty(resMap)) {
			String storeCityCode = (String) resMap.get("storeCityCode");
			resMap.put("storeCityCodeCn", this.getCodeCn(storeCityCode));

			String storeProvinceCode = (String) resMap.get("storeProvinceCode");
			resMap.put("storeProvinceCodeCn", this.getCodeCn(storeProvinceCode));

			String storeDistrictCode = (String) resMap.get("storeDistrictCode");
			resMap.put("storeDistrictCodeCn", this.getCodeCn(storeDistrictCode));

			String senderAreaCode = (String) resMap.get("sendArea");
			if (StringUtils.isNotBlank(senderAreaCode)) {
				List<RegionInfo> regionList = regionService.findRootByCode(senderAreaCode);
				resMap.put("senderAreaList", regionList);
			} else {
				resMap.put("senderAreaList", Lists.newArrayList());
			}

			String receiveAreaCode = (String) resMap.get("receiveArea");
			if (StringUtils.isNotBlank(receiveAreaCode)) {
				List<RegionInfo> regionList = regionService.findRootByCode(receiveAreaCode);
				resMap.put("receiveAreaList", regionList);
			} else {
				resMap.put("receiveAreaList", Lists.newArrayList());
			}
		}

		return resMap;
	}

	private String getCodeCn(String code) {
		if (StringUtils.isNotBlank(code)) {
			RegionInfo startCodePCn = regionService.findByCode(code);
			if (startCodePCn != null) {
				return startCodePCn.getRegionName();
			}
		}
		return "";
	}

}
