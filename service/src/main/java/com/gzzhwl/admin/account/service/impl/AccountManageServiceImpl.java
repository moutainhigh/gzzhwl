package com.gzzhwl.admin.account.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gzzhwl.admin.account.service.AccountManageService;
import com.gzzhwl.admin.account.validate.accountValidate;
import com.gzzhwl.core.constant.AgentType;
import com.gzzhwl.core.constant.DataSource;
import com.gzzhwl.core.constant.Global;
import com.gzzhwl.core.constant.RegSource;
import com.gzzhwl.core.data.dao.AccountInfoDao;
import com.gzzhwl.core.data.dao.AgentInfoDao;
import com.gzzhwl.core.data.dao.InternalDriverDao;
import com.gzzhwl.core.data.model.AccountInfo;
import com.gzzhwl.core.data.model.AgentInfo;
import com.gzzhwl.core.data.model.InternalDriver;
import com.gzzhwl.core.utils.IdentifierUtils;
import com.gzzhwl.core.utils.MD5;
import com.gzzhwl.core.utils.RandomCaptchaUtils;
import com.gzzhwl.core.utils.ValidateUtils;
import com.gzzhwl.rest.exception.RestException;

@Service
public class AccountManageServiceImpl implements AccountManageService {
	@Autowired
	private AgentInfoDao agentDao;
	@Autowired
	private AccountInfoDao accountInfoDao;
	@Autowired
	private InternalDriverDao internalDriverDao;

	@Override
	public boolean createAccountForCBS(String idno, String realName, String telphone, String idFImageRefId,
			String idBImageRefId, String staffId) {
		// 判断手机号是否被占用
		AgentInfo agentInfo = validTelUsed(telphone, idno);

		if (agentInfo == null) {
			// 创建账号
			String accountId = this.autoRegister(telphone);
			// 创建账户信息
			autoSaveOrUpdateAgentInfo(idno, realName, telphone, idFImageRefId, idBImageRefId, null, accountId);
		} else {
			// 根据内部司机信息同步app信息
			autoSaveOrUpdateAgentInfo(idno, realName, telphone, idFImageRefId, idBImageRefId,
					agentInfo.getAgentInfoId(), agentInfo.getAccountId());
		}

		return true;
	}

	private String autoRegister(String telphone) throws RestException {
		if (telphone == null) {
			telphone = RandomCaptchaUtils.getRandomString(11);
		}
		// RegSource regs = RegSource.getRegSource(deviceType);// 注册来源
		String password = RandomCaptchaUtils.getRandomNum(8);

		String accountId = IdentifierUtils.getId().generate().toString();
		AccountInfo accountInfo = new AccountInfo();
		accountInfo.setAccountId(accountId);
		accountInfo.setTelphone(telphone);
		accountInfo.setNickName(telphone);
		String _p = MD5.md5(MD5.md5(password));
		accountInfo.setPassword(_p);
		accountInfo.setAccountSource(RegSource.CBS.getCode());
		accountInfo.setStatus(Global.STATUS_NORMAL.toString());
		accountInfo.setIsDeleted(Global.ISDEL_NORMAL.toString());
		accountInfoDao.insert(accountInfo);
		InternalDriver internalDriver = new InternalDriver();
		internalDriver.setAccountId(accountId);
		internalDriver.setIsStaff(InternalDriver.YES);
		internalDriverDao.insert(internalDriver);
		return accountInfo.getAccountId();
	}

	// 如果手机号被占用，并且身份证相符，则返回经纪人信息，身份证不相符或者不存在经纪人信息，报错，如果没账号则返回null;
	private AgentInfo validTelUsed(String telphone, String idno) {
		accountValidate.valid_telphone(telphone);
		AccountInfo accountInfo = this.getAccountInfoByParam(telphone, true);
		if (!ValidateUtils.isEmpty(accountInfo)) {
			AgentInfo agentInfo = agentDao.get(accountInfo.getAccountId());
			// 验证经纪人信息是否存在
			accountValidate.valid_agent(agentInfo);
			// 验证app账户身份证和司机身份证号是否一致
			accountValidate.valid_idno_Accordence(agentInfo.getIdno(), idno);

			return agentInfo;
		}

		return null;
	}

	private String autoSaveOrUpdateAgentInfo(String idno, String realName, String mobile, String idFImageRefId,
			String idBImageRefId, String agentInfoId, String accountId) {
		AgentInfo agentInfo = new AgentInfo();
		if (StringUtils.isNotBlank(agentInfoId)) {
			agentInfo.setAgentInfoId(agentInfoId);
			agentInfo.setIdFImageRefId(idFImageRefId);
			agentInfo.setIdBImageRefId(idBImageRefId);
			agentDao.updateSelective(agentInfo);
		} else {
			agentInfoId = IdentifierUtils.getId().generate().toString();
			agentInfo.setAgentInfoId(agentInfoId);
			agentInfo.setAccountId(accountId);
			agentInfo.setIdno(idno);
			agentInfo.setRealName(realName);
			agentInfo.setIdFImageRefId(idFImageRefId);
			agentInfo.setIdBImageRefId(idBImageRefId);
			agentInfo.setAgentType(AgentType.PERSONAL.getCode());
			agentInfo.setSource(DataSource.VLORRY.getCode());
			agentInfo.setCreatedBy(accountId);
			agentInfo.setStatus(Global.STATUS_PASSED.toString());
			agentInfo.setIsDeleted(Global.ISDEL_NORMAL.toString());
			agentDao.insert(agentInfo);
		}

		return agentInfoId;
	}

	@Override
	public AccountInfo getAccountInfoByParam(String telphone, boolean isDel) {
		Map<String, Object> params = new HashMap<String, Object>();
		if (isDel) {
			params.put("isDel", Global.ISDEL_NORMAL.toString());
		}
		params.put("telphone", telphone);
		List<AccountInfo> loginList = accountInfoDao.findAccountInfoToLogin(params);
		if (CollectionUtils.isEmpty(loginList)) {
			return null;
		} else {
			return loginList.get(0);
		}
	}

	@Override
	public boolean updateAccountForCBS(String idno, String realName, String telphone, String oldTel,
			String idFImageRefId, String idBImageRefId, String staffId) {
		// 判断手机号是否被占用，如果被占用身份证号是否为当前司机的证件号
		AgentInfo agent = validTelUsed(telphone, idno);

		// 如果新手机不存在账号
		if (agent == null) {
			// 查询原手机号是否存在账号
			AccountInfo oldAccount = this.getAccountInfoByParam(oldTel, true);
			if (ValidateUtils.isEmpty(oldAccount)) {
				// 如果原手机也不存在账号
				// 创建账号
				String accountId = this.autoRegister(telphone);
				// 创建账户信息
				autoSaveOrUpdateAgentInfo(idno, realName, telphone, idFImageRefId, idBImageRefId, null, accountId);
			} else {
				// 获取经纪人信息
				AgentInfo agentInfo = agentDao.get(oldAccount.getAccountId());
				if (agentInfo.getIdno().equals(idno)) {
					// 如果身份证相符，则更新
					AccountInfo accountInfo = new AccountInfo();
					accountInfo.setAccountId(oldAccount.getAccountId());
					accountInfo.setTelphone(telphone);
					accountInfo.setNickName(telphone);
					accountInfoDao.updateSelective(accountInfo);

					// 更新经纪人信息
					autoSaveOrUpdateAgentInfo(idno, realName, telphone, idFImageRefId, idBImageRefId,
							agentInfo.getAgentInfoId(), oldAccount.getAccountId());
				} else {
					// 如果身份证不相符
					// 创建账号
					String accountId = this.autoRegister(telphone);
					// 创建账户信息
					autoSaveOrUpdateAgentInfo(idno, realName, telphone, idFImageRefId, idBImageRefId, null, accountId);
				}
			}

		} else {
			// 根据内部司机信息同步app信息
			autoSaveOrUpdateAgentInfo(idno, realName, telphone, idFImageRefId, idBImageRefId, agent.getAgentInfoId(),
					agent.getAccountId());
		}

		return true;
	}
}
