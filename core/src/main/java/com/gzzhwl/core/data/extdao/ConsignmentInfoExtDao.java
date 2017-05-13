package com.gzzhwl.core.data.extdao;

import java.util.List;
import java.util.Map;

import com.gzzhwl.core.page.Page;

public interface ConsignmentInfoExtDao {
	 public final static String PREFIX = ConsignmentInfoExtDao.class.getName();
	 
	 public <E, K, V> Page<E> pageConsignList(Map<K, V> params, int current, int pagesize);
	 
	 // 获取应收列表
	 public <E, K, V> Page<E> pageRecieveList(Map<K, V> params, int currentPage, int pageSizes);
	
	 //查询订单概要信息
	 public <K, V> Map<K, V> getOrderGenernalInfo(Map<K, V> params) ;
	 
	 //应收对账单列表查询
	 public <E, K, V> List<E> getReceiveSatementList(Map<K, V> params);
}
