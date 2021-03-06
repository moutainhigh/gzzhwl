package com.gzzhwl.api.agent.validate;

import com.gzzhwl.core.constant.AgentType;
import com.gzzhwl.core.constant.ErrorCode;
import com.gzzhwl.core.constant.Global;
import com.gzzhwl.core.data.model.AgentInfo;
import com.gzzhwl.core.utils.IdCardUtils;
import com.gzzhwl.core.utils.ValidateUtils;
import com.gzzhwl.rest.exception.RestException;

public class AgentValidate {

	/**
	 * 验证经纪人信息完整性/正确性
	 * 
	 * @param agentInfo
	 * @throws RestException
	 */
	public static void valid_Empty(AgentInfo agentInfo) throws RestException {
		if (agentInfo == null) {
			throw new RestException(ErrorCode.ERROR_700001);
		} else {
			// if (ValidateUtils.isEmpty(agentInfo.getAgentType())) {
			// throw new RestException(ErrorCode.ERROR_900003.getCode(),
			// "经纪人类型不能为空！");
			// }
			// String agentType=agentInfo.getAgentType();
			// if(!(AgentType.PERSONAL.getCode().equals(agentType)||AgentType.COMPANY.getCode().equals(agentType)))
			// {
			// throw new RestException("900020", "不存在的类型");
			// }
			if (ValidateUtils.isEmpty(agentInfo.getRealName())) {
				throw new RestException(ErrorCode.ERROR_900003.getCode(), "姓名不能为空！");
			}
			boolean realName = ValidateUtils.validateChinese(agentInfo.getRealName(), 2, 8);
			if (false == realName) {
				throw new RestException("900004", "姓名必须8个汉字以内");
			}
			if (ValidateUtils.isEmpty(agentInfo.getIdno())) {
				throw new RestException(ErrorCode.ERROR_900003.getCode(), "身份证号码不能为空！");
			}
			if (ValidateUtils.isEmpty(agentInfo.getIdFImageRefId())) {
				throw new RestException("900021", "缺少身份证正面图片");
			}
			if (ValidateUtils.isEmpty(agentInfo.getIdBImageRefId())) {
				throw new RestException("900022", "缺少身份证背面图片");
			}
			// if(AgentType.COMPANY.getCode().equals(agentType)) {
			// if (ValidateUtils.isEmpty(agentInfo.getCompanyFullName())) {
			// throw new RestException("900023", "公司名称不能为空");
			// }
			// if(agentInfo.getCompanyFullName().length()>30) {
			// throw new RestException("900024", "公司名称长度不能超过30");
			// }
			// if(!ValidateUtils.isEmpty(agentInfo.getUscCode())) {
			// if(StringUtils.isNotEmpty(agentInfo.getUscCode())) {
			// String uscCode=agentInfo.getUscCode();
			// boolean uc=ValidateUtils.validateNumber(uscCode, 18);
			// if(false==uc) {
			// throw new RestException("900007", "统一社会信用代码错误");
			// }
			// }
			// if(ValidateUtils.isEmpty(agentInfo.getBusImageRefId())) {
			// throw new RestException("900025", "缺少营业执照照片");
			// }
			// } else if(ValidateUtils.isEmpty(agentInfo.getUscCode())) {
			// if(ValidateUtils.isEmpty(agentInfo.getBusCode())) {
			// throw new RestException("900025", "缺少营业执照注册号");
			// }
			//
			// if(StringUtils.isNotEmpty(agentInfo.getBusCode())) {
			// String busCode=agentInfo.getBusCode();
			// boolean bc=ValidateUtils.validateNumber(busCode, 15);
			// if(false==bc) {
			// throw new RestException("900008", "营业执照注册号错误");
			// }
			// }
			//
			// if(ValidateUtils.isEmpty(agentInfo.getBusImageRefId())) {
			// throw new RestException("900025", "缺少营业执照照片");
			// }
			//
			// if(ValidateUtils.isEmpty(agentInfo.getOrgCode())) {
			// throw new RestException("900025", "缺少组织机构代码");
			// }
			//
			// if(StringUtils.isNotEmpty(agentInfo.getOrgCode())) {
			// String orgCode=agentInfo.getOrgCode();
			// boolean oc=ValidateUtils.validateOrgCode(orgCode);
			// if(false==oc) {
			// throw new RestException("900009", "组织机构代码错误");
			// }
			// }
			// if(ValidateUtils.isEmpty(agentInfo.getOrgImageRefId())) {
			// throw new RestException("900026", "缺少组织机构证照片");
			// }
			//
			// if(ValidateUtils.isEmpty(agentInfo.getTaxRegCode())) {
			// throw new RestException("900025", "缺少税务登记号");
			// }
			//
			// if(StringUtils.isNotEmpty(agentInfo.getTaxRegCode())) {
			// String taxRegCode=agentInfo.getTaxRegCode();
			// boolean trc=ValidateUtils.validateNumber(taxRegCode, 15);
			// if(false==trc) {
			// throw new RestException("900010", "税务登记号错误");
			// }
			// }
			// if(ValidateUtils.isEmpty(agentInfo.getTaxImageRefId())) {
			// throw new RestException("900027", "缺少税务登记证照片");
			// }
			// }
			// }
		}
	}

	public static void valid_Empty_v2(AgentInfo agentInfo) throws RestException {
		if (agentInfo == null) {
			throw new RestException(ErrorCode.ERROR_700001);
		} else {
			if (ValidateUtils.isEmpty(agentInfo.getAgentType())) {
				throw new RestException(ErrorCode.ERROR_900003.getCode(), "账户类型不能为空！");
			}
			String agentType = agentInfo.getAgentType();
			if (!(AgentType.PERSONAL.getCode().equals(agentType) || AgentType.COMPANY.getCode().equals(agentType))) {
				throw new RestException("900020", "不存在的类型");
			}
			if (ValidateUtils.isEmpty(agentInfo.getRealName())) {
				throw new RestException(ErrorCode.ERROR_900003.getCode(), "姓名不能为空！");
			}
			boolean realName = ValidateUtils.validateChinese(agentInfo.getRealName(), 1, 5);
			if (false == realName) {
				throw new RestException("900004", "姓名必须5个汉字以内");
			}
			if (ValidateUtils.isEmpty(agentInfo.getIdno())) {
				throw new RestException(ErrorCode.ERROR_900003.getCode(), "身份证号码不能为空！");
			}
			if (ValidateUtils.isEmpty(agentInfo.getIdFImageRefId())) {
				throw new RestException("900021", "缺少身份证正面图片");
			}
			if (ValidateUtils.isEmpty(agentInfo.getIdBImageRefId())) {
				throw new RestException("900022", "缺少身份证背面图片");
			}
			if (AgentType.COMPANY.getCode().equals(agentType)) {
				if (ValidateUtils.isEmpty(agentInfo.getBusImageRefId())) {
					throw new RestException("900025", "缺少营业执照照片");
				}
			}
		}
	}

	/**
	 * 已审核通过/待审核的不能修改
	 * 
	 * @param info
	 * @throws RestException
	 */
	public static void valid_status(AgentInfo info) throws RestException {
		if ((Global.STATUS_WAIT.toString().equals(info.getStatus())
				|| (Global.STATUS_PASSED.toString().equals(info.getStatus())))) {
			throw new RestException(ErrorCode.ERROR_900002);
		}
	}

	/**
	 * 验证身份证正确性/是否已使用
	 * 
	 * @param idno
	 * @param num
	 */
	public static void valid_idno(String idno, int num) {
		boolean r = IdCardUtils.validateIdCard18(idno);
		if (false == r) {
			throw new RestException("900005", "身份证号码错误");
		}
		if (num > 0) {
			throw new RestException("900011", "该身份证号已存在");
		}
	}

	public static void valid_not_exist(AgentInfo agentInfo) throws RestException {
		if (agentInfo == null) {
			throw new RestException(ErrorCode.ERROR_700001);
		}
	}
}
