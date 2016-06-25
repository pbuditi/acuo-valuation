package com.acuo.valuation.requests;

import java.time.LocalDate;

public interface Request {

	LocalDate getValuationDate();

	String getValuationCurrency();

	RequestData getData();

}
