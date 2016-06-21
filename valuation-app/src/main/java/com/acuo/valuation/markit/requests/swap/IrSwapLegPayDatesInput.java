package com.acuo.valuation.markit.requests.swap;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import com.acuo.common.marshal.jaxb.DateAdapter;

public class IrSwapLegPayDatesInput {

	public IrSwapLegPayDatesInput() {

	}

	public IrSwapLegPayDatesInput(IrSwapLegPayDates dates) {
		startDate = dates.startDate();
		frequency = dates.frequency();
		enddate = dates.enddate();
		rollCode = dates.rollCode();
		adjust = dates.isAdjust();
		eom = dates.isEom();
	}

	@XmlPath("startdate/text()")
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date startDate;

	@XmlPath("freq/text()")
	public String frequency;

	@XmlPath("enddate/text()")
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date enddate;

	@XmlPath("rollcode/text()")
	public String rollCode;

	@XmlPath("adjust/text()")
	public boolean adjust;

	@XmlPath("eom/text()")
	public boolean eom;
}