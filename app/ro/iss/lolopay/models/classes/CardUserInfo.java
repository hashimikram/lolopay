package ro.iss.lolopay.models.classes;

import java.io.Serializable;

public class CardUserInfo implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CardUserInfoEmploymentStatus employmentStatus;

	private CardUserInfoOccupation occupation;

	private CardUserInfoPurpose purpose;

	private CardUserInfoMonthlyIncome monthlyIncome;

	private CardUserInfoEstate estate;

	/**
	 * @return the employmentStatus
	 */
	public CardUserInfoEmploymentStatus getEmploymentStatus() {

		return employmentStatus;
	}

	/**
	 * @param employmentStatus the employmentStatus to set
	 */
	public void setEmploymentStatus(CardUserInfoEmploymentStatus employmentStatus) {

		this.employmentStatus = employmentStatus;
	}

	/**
	 * @return the occupation
	 */
	public CardUserInfoOccupation getOccupation() {

		return occupation;
	}

	/**
	 * @param occupation the occupation to set
	 */
	public void setOccupation(CardUserInfoOccupation occupation) {

		this.occupation = occupation;
	}

	/**
	 * @return the purpose
	 */
	public CardUserInfoPurpose getPurpose() {

		return purpose;
	}

	/**
	 * @param purpose the purpose to set
	 */
	public void setPurpose(CardUserInfoPurpose purpose) {

		this.purpose = purpose;
	}

	/**
	 * @return the monthlyIncome
	 */
	public CardUserInfoMonthlyIncome getMonthlyIncome() {

		return monthlyIncome;
	}

	/**
	 * @param monthlyIncome the monthlyIncome to set
	 */
	public void setMonthlyIncome(CardUserInfoMonthlyIncome monthlyIncome) {

		this.monthlyIncome = monthlyIncome;
	}

	/**
	 * @return the estate
	 */
	public CardUserInfoEstate getEstate() {

		return estate;
	}

	/**
	 * @param estate the estate to set
	 */
	public void setEstate(CardUserInfoEstate estate) {

		this.estate = estate;
	}

}
