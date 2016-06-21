package com.acuo.valuation.requests.dto;

import lombok.Data;

@Data
public class SwapLegDTO {

	private int id;
	private String currency;
	private SwapLegFixingDTO fixing;
	private Double spread;
	private Double rate;
	private String type;
	private String daycount;
	private Double notional;
	private String notionalxg;
	private SwapLegPayDatesDTO payDates;

}
