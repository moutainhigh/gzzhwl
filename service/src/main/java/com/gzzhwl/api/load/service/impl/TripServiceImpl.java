package com.gzzhwl.api.load.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.gzzhwl.admin.load.service.LoadBillService;
import com.gzzhwl.admin.load.service.LoadHistoryService;
import com.gzzhwl.admin.remark.service.RemarkInfoService;
import com.gzzhwl.api.agent.service.AgentInfoService;
import com.gzzhwl.api.load.service.ArrivedService;
import com.gzzhwl.api.load.service.TripService;
import com.gzzhwl.common.model.FlowActionCategory;
import com.gzzhwl.common.model.ZHFlow;
import com.gzzhwl.common.service.FlowsService;
import com.gzzhwl.common.service.RegionService;
import com.gzzhwl.core.constant.DataSource;
import com.gzzhwl.core.constant.ErrorCode;
import com.gzzhwl.core.constant.LoadBillType;
import com.gzzhwl.core.data.extdao.OrderLoadInfoExtDao;
import com.gzzhwl.core.data.extdao.RealDriverInfoExtDao;
import com.gzzhwl.core.data.model.FlowDef;
import com.gzzhwl.core.data.model.FlowStatus;
import com.gzzhwl.core.data.model.LoadDriverInfo;
import com.gzzhwl.core.data.model.OrderLoadInfo;
import com.gzzhwl.core.data.model.RegionInfo;
import com.gzzhwl.rest.exception.RestException;

@Service
public class TripServiceImpl implements TripService {

	private final static Logger LOG = LoggerFactory.getLogger(TripService.class);

	@Autowired
	private RealDriverInfoExtDao realDriverInfoExtDao;

	@Autowired
	private FlowsService flowsService;

	@Autowired
	private RegionService regionService;

	@Autowired
	private AgentInfoService agentInfoService;

	@Autowired
	private LoadHistoryService loadHistoryService;

	@Autowired
	private LoadBillService loadBillService;

	@Autowired
	private OrderLoadInfoExtDao orderLoadInfoExtDao;

	@Autowired
	private ArrivedService arrivedService;

	@Autowired
	private RemarkInfoService remarkInfoService;

	@Override
	public List<Map<String, Object>> getDriverOrderList(String accountId, String[] statusArray,DataSource dataSource) {

		Map<String, Object> agentMap = agentInfoService.getAgentInfoByCondition(accountId);

		if (agentMap == null) {
			throw new RestException(ErrorCode.ERROR_900013.getCode(), "经纪人 - " + ErrorCode.ERROR_900013.getDesc());
		}

		String idno = (String) agentMap.get("idno");

		List<Map<String, Object>> listMap = orderLoadInfoExtDao.getDriverOrderList(idno, statusArray,dataSource);

		// 更新状态描述
		CollectionUtils.transform(listMap, new Transformer<Map<String, Object>, Map<String, Object>>() {
			@Override
			public Map<String, Object> transform(Map<String, Object> input) {
				String status = (String) input.get("status");
				if (StringUtils.isNotBlank(status)) {
					FlowStatus statusCn = flowsService.getStatus(ZHFlow.LOAD_BILL, status);
					input.put("statusCn", statusCn.getName());
				} else {
					input.put("statusCn", StringUtils.EMPTY);
				}

				String loadId = (String) input.get("loadId");

				input.put("printStatus", null);
				input.put("elecStatus", null);
				input.put("isCanUpPrint", false);
				input.put("isCanUpElec", false);

				if (status.equals(LoadBillType.ARRIVED.getCode()) || status.equals(LoadBillType.ELECRECEIPT.getCode())
						|| status.equals(LoadBillType.DEPART.getCode()) || status.equals(LoadBillType.CLOSED.getCode())
						|| status.equals(LoadBillType.PRINTRECEIPT.getCode())) {
					String elecStatus = arrivedService.getCurrentElecStatus(loadId);
					// 电子回单状态
					input.put("elecStatus", elecStatus);
					// 是否能上传电子回单
					input.put("isCanUpElec", arrivedService.isCanUploadReceipt(elecStatus));
				}

				if (status.equals(LoadBillType.ELECRECEIPT.getCode()) || status.equals(LoadBillType.CLOSED.getCode())
						|| status.equals(LoadBillType.PRINTRECEIPT.getCode())) {
					String printStatus = arrivedService.getCurrentPrintStatus(loadId);
					// 纸质回单状态
					input.put("printStatus", printStatus);
					// 是否能上传纸质回单
					input.put("isCanUpPrint", arrivedService.isCanUploadReceipt(printStatus));
				}

				String startCodeP = (String) input.get("startCodeP");
				input.put("startCodePCn", this.getCodeCn(startCodeP));

				String endCodeP = (String) input.get("endCodeP");
				input.put("endCodePCn", this.getCodeCn(endCodeP));

				String startCodeC = (String) input.get("startCodeC");
				input.put("startCodeCCn", this.getCodeCn(startCodeC));

				String endCodeC = (String) input.get("endCodeC");
				input.put("endCodeCCn", this.getCodeCn(endCodeC));

				String transferCodeP = (String) input.get("transferCodeP");
				input.put("transferCodePCn", this.getCodeCn(transferCodeP));

				String transferCodeC = (String) input.get("transferCodeC");
				input.put("transferCodeCCn", this.getCodeCn(transferCodeC));

				String storeCityCode = (String) input.get("storeCityCode");
				input.put("storeCityCodeCn", this.getCodeCn(storeCityCode));

				String storeProvinceCode = (String) input.get("storeProvinceCode");
				input.put("storeProvinceCodeCn", this.getCodeCn(storeProvinceCode));

				String storeDistrictCode = (String) input.get("storeDistrictCode");
				input.put("storeDistrictCodeCn", this.getCodeCn(storeDistrictCode));

				String senderAreaCode = (String) input.get("sendArea");
				if (StringUtils.isNotBlank(senderAreaCode)) {
					List<RegionInfo> regionList = regionService.findRootByCode(senderAreaCode);
					input.put("senderAreaList", regionList);
				} else {
					input.put("senderAreaList", Lists.newArrayList());
				}

				String receiveAreaCode = (String) input.get("receiveArea");
				if (StringUtils.isNotBlank(receiveAreaCode)) {
					List<RegionInfo> regionList = regionService.findRootByCode(receiveAreaCode);
					input.put("receiveAreaList", regionList);
				} else {
					input.put("receiveAreaList", Lists.newArrayList());
				}

				return input;
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
		});

		return listMap;
	}

	@Override
	public List<Map<String, Object>> getDriverOrderList(String accountId,DataSource dataSource) {

		return this.getDriverOrderList(accountId,
				new String[] { LoadBillType.NOTVEHICLE.getCode(), LoadBillType.VEHICLECHECK.getCode(),
						LoadBillType.CLOSETOSURFACE.getCode(), LoadBillType.DEPART.getCode(),
						LoadBillType.ARRIVED.getCode(), LoadBillType.ELECRECEIPT.getCode() },dataSource);
	}

	@Override
	public List<Map<String, Object>> getFinishedOrderList(String accountId) {

		return this.getDriverOrderList(accountId,
				new String[] { LoadBillType.PRINTRECEIPT.getCode(), LoadBillType.CLOSED.getCode() },DataSource.VLORRY);
	}

	@Override
	public List<Map<String, Object>> getAllOrderList(String accountId) {

		return this.getDriverOrderList(accountId,
				new String[] { LoadBillType.NOTVEHICLE.getCode(), LoadBillType.VEHICLECHECK.getCode(),
						LoadBillType.CLOSETOSURFACE.getCode(), LoadBillType.DEPART.getCode(),
						LoadBillType.ARRIVED.getCode(), LoadBillType.ELECRECEIPT.getCode(),
						LoadBillType.CANCEL_TRIP.getCode(), LoadBillType.PRINTRECEIPT.getCode(),
						LoadBillType.CLOSED.getCode(), LoadBillType.HAS_INVALID.getCode() },DataSource.VLORRY);
	}

	@Override
	public List<Map<String, Object>> getDriverOrderHistoryList(String accountId) {

		return this.getDriverOrderList(accountId, new String[] { LoadBillType.CANCEL_TRIP.getCode(),
				LoadBillType.PRINTRECEIPT.getCode(), LoadBillType.CLOSED.getCode() },DataSource.PLATFORM);
	}

	// @Override
	// public OutputStream getDriverOrderListStream(String accountId, String
	// timeStamp, OutputStream outputStream) {
	//
	// BufferedOutputStream outputStm = new BufferedOutputStream(outputStream);
	//
	// List<Map<String, Object>> listMap = getDriverOrderList(accountId);
	//
	// try {
	// if (!ValidateUtils.isEmpty(listMap)) {
	// outputStm.write(JacksonJsonMapper.objectToJson(listMap).getBytes());
	// outputStm.flush();
	// }
	//
	// } catch (IOException e) {
	// LOG.error("文件流写入失败,异常原因" + e.getMessage());
	// } finally {
	// if (outputStm != null) {
	// try {
	// outputStm.close();
	// } catch (IOException e) {
	// LOG.error("文件流写入失败,异常原因" + e.getMessage());
	// }
	// }
	// }
	//
	// return outputStream;
	// }

	@Override
	public void ysjVehicleCheck(String loadNo, String accountId, String actionTime) {

		OrderLoadInfo orderLoadInfo = loadBillService.getLoadInfoByLoadNo(loadNo);

		if (orderLoadInfo == null) {
			throw new RestException(ErrorCode.ERROR_900013);
		}

		boolean isMajoy = this.isLoadMajorDriver(orderLoadInfo.getLoadId(), accountId);
		if (!isMajoy) {
			throw new RestException(ErrorCode.ERROR_110011);
		}

		// 跑出异常不处理,直接获取到该流程
		FlowDef flowdef = new FlowDef();
		flowdef.setActionCode("02");
		flowdef.setCategory(FlowActionCategory.YSJ.getCode());
		flowdef.setTransitionStatus(LoadBillType.VEHICLECHECK.getCode());
		flowdef.setMsgTemplate("司机完成了车检");

		loadHistoryService.saveErrorLoadHistory(orderLoadInfo.getLoadId(), orderLoadInfo.getStatus(), actionTime,
				flowdef, accountId);
	}

	@Override
	public void ysjCloseToSurface(String loadNo, String accountId, String actionTime) {

		OrderLoadInfo orderLoadInfo = loadBillService.getLoadInfoByLoadNo(loadNo);

		if (orderLoadInfo == null) {
			throw new RestException(ErrorCode.ERROR_900013);
		}
		boolean isMajoy = this.isLoadMajorDriver(orderLoadInfo.getLoadId(), accountId);
		if (!isMajoy) {
			throw new RestException(ErrorCode.ERROR_110011);
		}
		// 跑出异常不处理,直接获取到该流程
		FlowDef flowdef = new FlowDef();
		flowdef.setActionCode("03");
		flowdef.setCategory(FlowActionCategory.YSJ.getCode());
		flowdef.setTransitionStatus(LoadBillType.CLOSETOSURFACE.getCode());
		flowdef.setMsgTemplate("司机完成了靠台");

		loadHistoryService.saveErrorLoadHistory(orderLoadInfo.getLoadId(), orderLoadInfo.getStatus(), actionTime,
				flowdef, accountId);

	}

	@Override
	public void ysjTripTheCar(String loadNo, String accountId, String actionTime) {

		OrderLoadInfo orderLoadInfo = loadBillService.getLoadInfoByLoadNo(loadNo);

		if (orderLoadInfo == null) {
			throw new RestException(ErrorCode.ERROR_900013);
		}
		boolean isMajoy = this.isLoadMajorDriver(orderLoadInfo.getLoadId(), accountId);
		if (!isMajoy) {
			throw new RestException(ErrorCode.ERROR_110011);
		}
		// 跑出异常不处理,直接获取到该流程
		FlowDef flowdef = new FlowDef();
		flowdef.setActionCode("05");
		flowdef.setCategory(FlowActionCategory.YSJ.getCode());
		flowdef.setTransitionStatus(LoadBillType.DEPART.getCode());
		flowdef.setMsgTemplate("司机已发车");

		loadHistoryService.saveErrorLoadHistory(orderLoadInfo.getLoadId(), orderLoadInfo.getStatus(), actionTime,
				flowdef, accountId);

	}

	@Override
	public Map<String, Object> getDriverOrderCount(String accountId,DataSource dataSource) {

		Map<String, Object> countMap = new HashMap<>();

		Integer onlineCount = orderLoadInfoExtDao.getDriverOrderCount(accountId,
				new String[] { LoadBillType.NOTVEHICLE.getCode(), LoadBillType.VEHICLECHECK.getCode(),
						LoadBillType.CLOSETOSURFACE.getCode(), LoadBillType.DEPART.getCode(),
						LoadBillType.ARRIVED.getCode(), LoadBillType.ELECRECEIPT.getCode()},dataSource);

		Integer completedCount = orderLoadInfoExtDao.getDriverOrderCount(accountId,
				new String[] { LoadBillType.PRINTRECEIPT.getCode(), LoadBillType.CLOSED.getCode() },dataSource);

		countMap.put("onlineCount", onlineCount);
		countMap.put("completedCount", completedCount);

		return countMap;
	}

	@Override
	public Map<String, Object> getDriverOrderDetail(String loadId, String accountId) {

		Map<String, Object> agentMap = agentInfoService.getAgentInfoByCondition(accountId);

		if (agentMap == null) {
			throw new RestException(ErrorCode.ERROR_900013.getCode(), "经纪人 - " + ErrorCode.ERROR_900013.getDesc());
		}

		String idno = (String) agentMap.get("idno");

		Map<String, Object> resMap = orderLoadInfoExtDao.getDriverOrderDetail(loadId, idno);

		String status = (String) resMap.get("status");
		if (StringUtils.isNotBlank(status)) {
			FlowStatus statusCn = flowsService.getStatus(ZHFlow.LOAD_BILL, status);
			resMap.put("statusCn", statusCn.getName());
		} else {
			resMap.put("statusCn", StringUtils.EMPTY);
		}

		resMap.put("printStatus", null);
		resMap.put("elecStatus", null);
		resMap.put("isCanUpPrint", false);
		resMap.put("isCanUpElec", false);

		if (status.equals(LoadBillType.ARRIVED.getCode()) || status.equals(LoadBillType.ELECRECEIPT.getCode())
				|| status.equals(LoadBillType.DEPART.getCode()) || status.equals(LoadBillType.CLOSED.getCode())
				|| status.equals(LoadBillType.PRINTRECEIPT.getCode())) {
			String elecStatus = arrivedService.getCurrentElecStatus(loadId);
			// 电子回单状态
			resMap.put("elecStatus", elecStatus);
			// 是否能上传电子回单
			resMap.put("isCanUpElec", arrivedService.isCanUploadReceipt(elecStatus));
		}

		if (status.equals(LoadBillType.ELECRECEIPT.getCode()) || status.equals(LoadBillType.CLOSED.getCode())
				|| status.equals(LoadBillType.PRINTRECEIPT.getCode())) {
			String printStatus = arrivedService.getCurrentPrintStatus(loadId);
			// 纸质回单状态
			resMap.put("printStatus", printStatus);
			// 是否能上传纸质回单
			resMap.put("isCanUpPrint", arrivedService.isCanUploadReceipt(printStatus));
		}

		String startCodeP = (String) resMap.get("startCodeP");
		resMap.put("startCodePCn", this.getCodeCn(startCodeP));

		String endCodeP = (String) resMap.get("endCodeP");
		resMap.put("endCodePCn", this.getCodeCn(endCodeP));

		String startCodeC = (String) resMap.get("startCodeC");
		resMap.put("startCodeCCn", this.getCodeCn(startCodeC));

		String endCodeC = (String) resMap.get("endCodeC");
		resMap.put("endCodeCCn", this.getCodeCn(endCodeC));

		String transferCodeP = (String) resMap.get("transferCodeP");
		resMap.put("transferCodePCn", this.getCodeCn(transferCodeP));

		String transferCodeC = (String) resMap.get("transferCodeC");
		resMap.put("transferCodeCCn", this.getCodeCn(transferCodeC));

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

		resMap.put("onLineRecord", remarkInfoService.getOnlineRecordNew(loadId));

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

	@Override
	public boolean isLoadMajorDriver(String loadId, String accountId) {
		Map<String, Object> params = Maps.newHashMap();
		params.put("accountId", accountId);
		params.put("loadId", loadId);
		params.put("isMajor", LoadDriverInfo.MAJOR_YES);
		return orderLoadInfoExtDao.isMajorDriver(params) > 0;
	}

}
