package com.example.DentalPlus_Backend.dto;

public class RoleDto {

	private String roleType;
	private Long roleId;
	private Long clinicId;
	private String clinicName;
	private Boolean active;
	private String extraInfo;

	public RoleDto() {
	}

	public RoleDto(String roleType, Long roleId, Long clinicId, String clinicName, Boolean active, String extraInfo) {
		this.roleType = roleType;
		this.roleId = roleId;
		this.clinicId = clinicId;
		this.clinicName = clinicName;
		this.active = active;
		this.extraInfo = extraInfo;
	}

	public String getRoleType() {
		return roleType;
	}

	public Long getRoleId() {
		return roleId;
	}

	public Long getClinicId() {
		return clinicId;
	}

	public String getClinicName() {
		return clinicName;
	}

	public Boolean getActive() {
		return active;
	}

	public String getExtraInfo() {
		return extraInfo;
	}

	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public void setClinicId(Long clinicId) {
		this.clinicId = clinicId;
	}

	public void setClinicName(String clinicName) {
		this.clinicName = clinicName;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}
}