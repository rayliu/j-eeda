package models;

public class ParentOfficeModel {
	Long currentOfficeId;
	Long parentOfficeId;
	Long belongOffice;
	public Long getCurrentOfficeId() {
		return currentOfficeId;
	}
	public void setCurrentOfficeId(Long currentOfficeId) {
		this.currentOfficeId = currentOfficeId;
	}
	public Long getParentOfficeId() {
		return parentOfficeId;
	}
	public void setParentOfficeId(Long parentOfficeId) {
		this.parentOfficeId = parentOfficeId;
	}
	public Long getBelongOffice() {
		return belongOffice;
	}
	public void setBelongOffice(Long belongOffice) {
		this.belongOffice = belongOffice;
	}
	
}
