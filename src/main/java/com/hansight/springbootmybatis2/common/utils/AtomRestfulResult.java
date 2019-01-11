/**
 * Copyright: 瀚思安信（北京）软件技术有限公司，保留所有权利。
 * Author: neven (pengyu_yang@hansight.com)
 * Created: 2016年08月01日
 */
package com.hansight.springbootmybatis2.common.utils;

import com.hansight.spider.Multilingualable;

import java.util.ArrayList;
import java.util.List;

/**
 * Restful返回结果实体类
 * Author: neven (pengyu_yang@hansight.com)
 * Created: 2016年08月01日
 */
public class AtomRestfulResult implements Multilingualable {

	/**
     * 状态码-操作成功
	 */
	public static final int STATUS_CODE_SUCCESS = 0;

	/**
	 * 状态码-操作失败
	 */
	public static final int STATUS_CODE_ERROR = 4;

    /**
     * 状态码-参数异常
	 */
    public static final int STATUS_CODE_PARAMETER_ILLEGALITY = 1;

	/**
	 * 状态码-参数异常
	 */
	public static final int STATUS_CODE_UNAUTHORIZED = 3;



	public static final int STATUS_CODE_FORBIDDEN = 2;
	
	/**
	 * License不合法
	 */
	public static final int STATUS_CODE_LICENSE_ILLEGAL = 1001;

	/**
	 * 状态吗
	 */
	private int statusCode;

	/**
     * 操作描述信息数组
	 */
	private List<String> messages = new ArrayList<>();

    /**
     * 返回数据
	 */
	private Object data;

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public void addMessages(String message) {
		messages.add(message);
	}

	public List<String> getMessages() {
		return messages;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * 返回数据ID
	 * @param id
	 */
	public void addData(String id){
		this.data = new AtomRestfulResultId(id);
	}

	/**
	 * 返回一个数据对象或者对象列表
	 * @param object
	 */
	public void addData(Object object){
		this.data = object;
	}

	class AtomRestfulResultId {
		private String id;

		public AtomRestfulResultId(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}
}


