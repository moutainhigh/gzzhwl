package com.gzzhwl.admin.supply.service;

import java.util.List;
import java.util.Map;

import com.gzzhwl.admin.supply.vo.SupplyInfoVo;
import com.gzzhwl.admin.supply.vo.SupplyQueryVo;
import com.gzzhwl.core.data.model.SupplyInfo;
import com.gzzhwl.core.page.Page;

public interface SupplyService {
	public String saveSupplyVo(SupplyInfoVo vo, String staffId, Integer departId);

	public String updateSupply(SupplyInfoVo vo, String staffId);

	public void removeSupply(String supplyId, String staffId);

	public List<Map<String, Object>> getSupplyList(String keyWord);

	public Page<Map<String, Object>> pageSupplyList(SupplyQueryVo queryVo, int pageIndex, int pageSize);

	public Map<String, Object> getSupplyDetail(String supplyId);

	public SupplyInfo saveSupplyFromYSJ(String accountId);
}
