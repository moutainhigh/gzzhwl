package com.gzzhwl.admin.vehicle.service;


import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.gzzhwl.admin.vehicle.vo.DriverAndVehicleCheckQueryVo;
import com.gzzhwl.admin.vehicle.vo.DriverAndVehicleVo;
import com.gzzhwl.admin.vehicle.vo.VehicleInfoQryVo;
import com.gzzhwl.core.data.model.RealVehicleInfo;
import com.gzzhwl.core.data.model.RealVehiclePlusInfo;
import com.gzzhwl.core.data.model.RealVehicleUsedInfo;
import com.gzzhwl.core.page.Page;

public interface VehicleManageService {
	
//	public String update(VehicleInfo vehicleInfo,VehiclePlusInfo vehicleInfoPlusInfo,VehicleUsedInfo vehicleUsedInfo,String staffId);
	
	//public String save(VehicleInfo vehicleInfo,VehiclePlusInfo vehicleInfoPlusInfo,VehicleUsedInfo vehicleUsedInfo,String staffId);
	
	public String saveVehicle(RealVehicleInfo vehicleInfo,RealVehiclePlusInfo vehicleInfoPlusInfo,RealVehicleUsedInfo vehicleUsedInfo,String staffId) throws ParseException;
	
	public String saveHung(RealVehicleInfo vehicleInfo,RealVehiclePlusInfo vehicleInfoPlusInfo,RealVehicleUsedInfo vehicleUsedInfo,String staffId);
	
	public String updateVehicle(RealVehicleInfo vehicleInfo,RealVehiclePlusInfo vehicleInfoPlusInfo,RealVehicleUsedInfo vehicleUsedInfo,String staffId) throws ParseException;
	
	public String updateHung(RealVehicleInfo vehicleInfo,RealVehiclePlusInfo vehicleInfoPlusInfo,RealVehicleUsedInfo vehicleUsedInfo,String staffId);
	
	public Map<String, Object> queryDetail(String vehicleInfoId);
	
	public String updateImage(MultipartFile file, String staffId);
	
	public void remove(String vehicleInfoId,String staffId);
	
	/**
	 * 车辆信息查询
	 * @param vehicleInfoCheckVo
	 * @return
	 */
	public Page<Map<String, Object>> queryVehicleInfoList(VehicleInfoQryVo vehicleInfoQryVo,int pageIndex, int pageSize);
	
	/**
	 * 车挂信息查询
	 * @param vehicleInfoCheckVo
	 * @return
	 */
	public Page<Map<String, Object>> queryHungList(VehicleInfoQryVo vehicleInfoQryVo,int pageIndex, int pageSize);
	
	/**
	 * 车辆唯一性校验接口
	 * @param plateNumber
	 * @param licenseNo
	 * @param engineNo
	 * @param vin
	 * @param regCertCode
	 * @param operatingCertNo
	 * @return
	 */
	public boolean vehicleManageExistValidator(String plateNumber,String licenseNo,String engineNo,String vin,String regCertCode,String operatingCertNo,String currentVehicleInfoId);
	
	/**
	 * 车辆唯一性校验接口
	 * @param plateNumber
	 * @param licenseNo
	 * @param engineNo
	 * @param vin
	 * @param regCertCode
	 * @param operatingCertNo
	 * @return
	 */
	public boolean vehicleManageExistValidator(String plateNumber,String licenseNo,String engineNo,String vin,String regCertCode,String operatingCertNo,String currentVehicleInfoId,Map<String,Object> resMap);
	
	/**
	 * 车挂唯一性校验接口
	 * @param plateNumber
	 * @param licenseNo
	 * @param vin
	 * @param regCertCode
	 * @param operatingCertNo
	 * @param currentVehicleInfoId
	 * @return
	 */
	public boolean hungManageExistValidator(String plateNumber,String licenseNo,String vin,String regCertCode,String operatingCertNo,String currentVehicleInfoId);
	
	/**
	 * 车挂唯一性校验接口
	 * @param plateNumber
	 * @param licenseNo
	 * @param vin
	 * @param regCertCode
	 * @param operatingCertNo
	 * @param currentVehicleInfoId
	 * @return
	 */
	public boolean hungManageExistValidator(String plateNumber,String licenseNo,String vin,String regCertCode,String operatingCertNo,String currentVehicleInfoId,Map<String,Object> resMap);
	
	
//	public Map<String, Object> queryDriverandVehicleDetail(String driverInfoId,String accountId);
	
	/**
	 * 获取司机车辆审核列表
	 * 
	 * @param driverAndVehicleCheckQueryVo
	 * @param current
	 * @param pagesize
	 * @return
	 */
	public Page<Map<String, Object>> getDriverAndVehicleCheckList(
			DriverAndVehicleCheckQueryVo driverAndVehicleCheckQueryVo, int current, int pagesize);
	
	/**
	 * 获取司机车辆审核明细
	 * @param vehicleInfoId
	 * @param accountId
	 * @return
	 */
	public Map<String, Object> getDriverandVehicleCheckDetail(String vehicleInfoId,String accountId);
	
	/**
	 * 获取正式表司机车辆明细
	 * @param driverAndVehicleVo
	 * @return
	 */
	public Map<String, Object> getDriverandVehicleDetail(DriverAndVehicleVo driverAndVehicleVo);
	
	/**
	 * 获取正式表司机车辆列表
	 * @param driverAndVehicleVo
	 * @return
	 */
	public List<Map<String, Object>> getRealDriverandVehicleList(String accountId);
	
//	/**
//	 * 司机和车辆审核
//	 * 
//	 * @param driverInfoId
//	 * @param staffInfo
//	 * @return
//	 */
//	public void checkDriverAndVehicle(String driverInfoId, String staffId, String authFlag);
	
	/**
	 * 司机车辆审核
	 * @param vehicleInfoId 待审核的车辆ID
	 * @param accountId 待审核的账号ID
	 * @param staffId 员工ID
	 * @param authFlag 审核标识 0 审核通过  1审核不通过
	 */
	public void checkDriverAndVehicle(String vehicleInfoId,String accountId, String staffId, String authFlag);

	public List<Map<String, Object>> listVehicle(String[] arrVehicleIds,Integer departId, String staffId);

	public List<Map<String, Object>> listHung(String[] arrVehicleIds ,Integer departId, String staffId);

	public List<Map<String, Object>> listAll(String staffId, Integer departId);
	
	
}
