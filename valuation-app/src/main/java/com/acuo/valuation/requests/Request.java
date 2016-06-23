package com.acuo.valuation.requests;

import java.time.LocalDate;
import java.util.Date;

public interface Request {

	LocalDate getValuationDate();

	String getValuationCurrency();

	RequestData getData();

}
