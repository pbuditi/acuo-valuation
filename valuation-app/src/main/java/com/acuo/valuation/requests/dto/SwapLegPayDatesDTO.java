package com.acuo.valuation.requests.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class SwapLegPayDatesDTO {

	private LocalDate startDate;
	private String frequency;
	private LocalDate enddate;
	private String rollCode;
	private boolean adjust;
	private boolean eom;

}
