package com.gzzhwl.api.load.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gzzhwl.admin.load.service.LoadFeedbackService;
import com.gzzhwl.admin.load.vo.LoadFeedbackListVo;
import com.gzzhwl.core.data.model.AccountInfo;
import com.gzzhwl.rest.springmvc.annotation.Authorization;
import com.gzzhwl.rest.springmvc.model.ResponseModel;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
	
	@Autowired
	private LoadFeedbackService loadFeedbackService;

	/**
	 * 外部司机保存异常反馈
	 * 
	 * @param loadFeedbackListVo
	 * @return
	 */
	@RequestMapping(value = "/saveYSJDriverFeedback", method = RequestMethod.POST, consumes = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseModel saveYSJDriverFeedback(@RequestBody LoadFeedbackListVo loadFeedbackListVo, @Authorization AccountInfo accountInfo) {

		loadFeedbackService.saveYSJDriverFeedback(loadFeedbackListVo, accountInfo.getAccountId());

		return new ResponseModel(null);
	}
	
	/**
	 * 内部司机保存异常反馈
	 * 
	 * @param loadFeedbackListVo
	 * @return
	 */
	@RequestMapping(value = "/saveCBSDriverFeedback", method = RequestMethod.POST, consumes = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseModel saveCBSDriverFeedback(@RequestBody LoadFeedbackListVo loadFeedbackListVo, @Authorization AccountInfo accountInfo) {

		
		loadFeedbackService.saveCBSDriverFeedback(loadFeedbackListVo, accountInfo.getAccountId());
		
		return new ResponseModel(null);
	}
	
	/**
	 * 定位信息上报
	 * 
	 * @param loadFeedbackListVo
	 * @return
	 */
	@RequestMapping(value = "/saveGPSInfo", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseModel saveGPSInfo(@RequestBody LoadFeedbackListVo loadFeedbackListVo, @Authorization AccountInfo accountInfo) {

		loadFeedbackService.saveGPSInfo(loadFeedbackListVo, accountInfo.getAccountId());

		return new ResponseModel(null);
	}
	
	/**
	 * 结束异常反馈
	 * 
	 * @param loadFeedbackListVo
	 * @return
	 */
	@RequestMapping(value = "/endFeedback", method = RequestMethod.POST)
	public ResponseModel endFeedback(@RequestParam(required = true)String feedbackId,@RequestParam(required = false)String actionTime,@Authorization AccountInfo accountInfo) {

		loadFeedbackService.endFeedback(feedbackId,actionTime, accountInfo.getAccountId());

		return new ResponseModel(null);
	}
	
	/**
	 * 异常反馈图片上传
	 * 
	 * @param request
	 * @param vehicleInfoId
	 * @param type
	 * @return
	 */
	@RequestMapping(value = "/feedBackUploadImage", method = RequestMethod.POST)
	public ResponseModel feedBackUploadImage(@RequestParam MultipartFile image,
			@Authorization AccountInfo accountInfo) {
		String imageId = loadFeedbackService.updateImage(image, accountInfo.getAccountId());
		Map<String, Object> resMap = new HashMap<>();
		resMap.put("imageId", imageId);
		return new ResponseModel(resMap);
	}
	
	
	/**
	 * 是否能保存异常反馈
	 * @param feedBackId
	 * @param price
	 * @return
	 */
	@RequestMapping(value = "/isCanSaveYSJFeedback", method = RequestMethod.GET)
	public ResponseModel isCanSaveYSJFeedback(@RequestParam String loadId) {

		boolean res = loadFeedbackService.isCanSaveYSJFeedback(loadId);
		
		Map<String, Object> resMap = new HashMap<>();
		resMap.put("result", res);
		
		return new ResponseModel(resMap);
	}
	
	
	
	
	
}
