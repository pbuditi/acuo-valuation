package com.acuo.valuation.requests;

import java.util.Date;

public interface Request {

	Date getValuationDate();

	String getValuationCurrency();

	RequestData getData();

}
