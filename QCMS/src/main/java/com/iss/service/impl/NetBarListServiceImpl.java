package com.iss.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.iss.constants.SystemConstants;
import com.iss.dao.INetBar2JPADao;
//import com.iss.dao.INetBarJPADao;
import com.iss.entity.AreasBarEntity;
import com.iss.entity.NetBar2Entity;
import com.iss.entity.ProvinceCityBarEntity;
import com.iss.entity.StatAreaEntity;
import com.iss.entity.StatNetBarEntity;
import com.iss.entity.UserEntity;
import com.iss.service.INetBarListService;
import com.iss.util.CommonUtil;
import com.iss.util.DateUtil;
import com.iss.util.HttpClientUtil;
import com.iss.util.NumberUtil;
import com.iss.util.PropertiesUtil;
import com.iss.util.StringUtil;
import com.iss.vo.DataParam;
import com.iss.vo.NetBarPrintVo;

@Service
public class NetBarListServiceImpl implements INetBarListService {

	@Autowired
	private INetBar2JPADao iNetBarJPADao;
	
	@Override
	public List<ProvinceCityBarEntity> loadProvinceCityBar(UserEntity user) {
		List<ProvinceCityBarEntity> data = new ArrayList<ProvinceCityBarEntity>();
		String url=PropertiesUtil.getPropery(SystemConstants.USER_PROVINCE_URL)+user.getId();
		if(SystemConstants.ADMINISTRATOR_NAME.equals(user.getLogin())){
			url=PropertiesUtil.getPropery("provinceUrl");
		}
		String result = HttpClientUtil.netBarListHttpPost(url,new HashMap<String, Object>());
		//json结果转换为List集合
		try {
			if(StringUtil.isNotEmpty(result)){
				data = JSON.parseArray(result,ProvinceCityBarEntity.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	@Override
	public List<ProvinceCityBarEntity> loadProvinceCityBar(UserEntity user,DataParam param) {
		String url = PropertiesUtil.getPropery("cityUrl");
		String value = param.getSearch().get("value");
		if(SystemConstants.ADMINISTRATOR_NAME.equals(user.getLogin())){
			if(StringUtil.isEmpty(value) || value.equals("410000")){
				url = PropertiesUtil.getPropery("provinceUrl");
			}else{
				url = url + value;
			}
		}else{
			String params=user.getId()+":"+value;
			if(StringUtil.isEmpty(value) || value.equals("410000")){
				url = PropertiesUtil.getPropery(SystemConstants.USER_PROVINCE_URL)+user.getId();
			}else{
				url = PropertiesUtil.getPropery(SystemConstants.USER_CITY_URL) + params;
			}
		}
		
		List<ProvinceCityBarEntity> data = new ArrayList<ProvinceCityBarEntity>();
		String result = HttpClientUtil.netBarListHttpPost(url,new HashMap<String, Object>());
		//json结果转换为List集合
		try {
			if(StringUtil.isNotEmpty(result)){
				data = JSON.parseArray(result,ProvinceCityBarEntity.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	@Override
	public List<AreasBarEntity> loadAreasBar(UserEntity user, DataParam param) {
		List<AreasBarEntity> data = new ArrayList<AreasBarEntity>();
		String querytype=param.getSearch().get("querytype");
		String value = param.getSearch().get("value");
		Map<String, Object> parammap=new HashMap<String, Object>();
		if(CommonUtil.isNotEmpty(querytype)){
			if(CommonUtil.isEmpty(value))
				return data;
		}else querytype="code";
		String url=PropertiesUtil.getPropery("areaUrl");
		if(SystemConstants.ADMINISTRATOR_NAME.equals(user.getLogin())){
			url=url+querytype;
			parammap.put("keyword", value);
		}else{
//			String params=user.getId()+":"+querytype+":"+value;
			url = url + querytype;
			parammap.put("keyword", value);
			parammap.put("userId", user.getId());
		}
		String result = HttpClientUtil.netBarListHttpPost(url,parammap);
		//json结果转换为List集合
		try {
			if(StringUtil.isNotEmpty(result)){
				data = JSON.parseArray(result,AreasBarEntity.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	

	@Override
	public List<AreasBarEntity> loadAreasBar(String areaCode, String barId) {
		// TODO Auto-generated method stub
		if(StringUtil.isEmpty(areaCode) || StringUtil.isEmpty(barId))return null;
		List<AreasBarEntity> data = new ArrayList<AreasBarEntity>();
		String head=areaCode.substring(0, 4);
		if(Integer.valueOf(head)>=4189){
			areaCode=head+"00";
		}
		String param=areaCode+":"+barId;
		String result = HttpClientUtil.netBarListHttpPost(
				PropertiesUtil.getPropery(SystemConstants.BAR_AREA_URL)+param
				,new HashMap<String, Object>());
		//json结果转换为List集合
		try {
			if(StringUtil.isNotEmpty(result)){
				data = JSON.parseArray(result,AreasBarEntity.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	@Override
	public List<StatAreaEntity> getStatAreas(String area_code,String parentId){
		String rankno = "";
		if(StringUtil.isEmpty(parentId)){
			rankno = "1";
			area_code = "410000";
		}else if(parentId.equals("0")){
			rankno = "1";
		}else if(parentId.equals("410000")){
			rankno = "2";
		}else{
			rankno = "3";
		}
		return iNetBarJPADao.getStatAreas(area_code, rankno);
	}

	@Override
	public List<StatNetBarEntity> getStatNetBars(String area_code){
		return iNetBarJPADao.getStatNetBars(area_code);
	}
	
	@Override
	public NetBarPrintVo queryNetbarPrintInfo(String barId) {
		// TODO Auto-generated method stub
		NetBarPrintVo vo=new NetBarPrintVo();
		if(CommonUtil.isEmpty(barId))return vo;
		NetBar2Entity e= this.iNetBarJPADao.findOne(barId);
		if(e==null)return vo;
		vo.setBarId(barId);
		vo.setBarName(e.getNetbar_name());
		vo.setApprovalNum(e.getApproval_num());
		vo.setRegAddress(e.getReg_address());
		vo.setUploadDate(DateUtil.getDataString(DateUtil.date_sdf_wz));
		vo.setUploadTime(DateUtil.getDataString(DateUtil.datetimeFormat));
		Long comNum=e.getComputer_num().longValue();
		vo.setZdzs(comNum);
		List<AreasBarEntity> list=loadAreasBar(e.getDistrict_code(), barId);
		if(CommonUtil.isNotEmpty(list)){
			AreasBarEntity ae=list.get(0);
			vo.setInstallNum(ae.getOnline()+ae.getOffline());//已安装终端: OnlineStatistic.onlineNum + OnlineStatistic.offlineNum
			vo.setUnInstallNum(comNum-vo.getInstallNum());//未安装终端: 终端总数 - 已安装终端
			vo.setOnLineCount(ae.getOnlineNumToday());//在线=OnlineStatistic.onlineNumToday
			vo.setOffLineCount(vo.getInstallNum()-vo.getOnLineCount()); //不在线=已安装终端 - 在线
//			在线率: 在线 / 终端总数 * 100%     (保留到小数点后1位)
//			vo.setOnLineCount(1l);
//			vo.setInstallNum(1l);
			double onlineRage=0,installRate=0;
			if(comNum!=null && comNum!=0){
				long onLineCount=vo.getOnLineCount();
				onlineRage=(Double.valueOf(onLineCount)/comNum)*100;
				vo.setOnLineRate(NumberUtil.parseDoubleToString(onlineRage, 1));
//				安装率: 已安装终端 / 终端总数 * 100%	(保留到小数点后1位)
				installRate=(Double.valueOf(vo.getInstallNum())/comNum)*100;
				vo.setInstallRate(NumberUtil.parseDoubleToString(installRate, 1));
			}
			vo.setOnLineRate(NumberUtil.parseDoubleToString(onlineRage, 1));
			vo.setInstallRate(NumberUtil.parseDoubleToString(installRate, 1));
//			
		}
		return vo;
	}
}