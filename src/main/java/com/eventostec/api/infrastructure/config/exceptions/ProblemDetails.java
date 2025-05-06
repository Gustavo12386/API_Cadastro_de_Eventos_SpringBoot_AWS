package com.eventostec.api.infrastructure.config.exceptions;

public class ProblemDetails {
	
	private String title;
	 private Integer code;
	 private String status;
	 private String detail;
	 private String instance;
	
	public ProblemDetails(String title, Integer code, String status, String detail, String instance) {
		super();
		this.title = title;
		this.code = code;
		this.status = status;
		this.detail = detail;
		this.instance = instance;
	}
	
	public ProblemDetails() {
		super();		
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public String getInstance() {
		return instance;
	}
	public void setInstance(String instance) {
		this.instance = instance;
	}
	  
}
