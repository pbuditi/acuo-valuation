package com.acuo.valuation.requests.markit;

import static com.acuo.valuation.requests.markit.Key.key;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import com.acuo.valuation.requests.RequestData;
import com.acuo.valuation.requests.markit.RequestDefinition.DataDefinition;
import com.acuo.valuation.requests.markit.RequestDefinition.DataDefinition.IrSwapDefinition;
import com.acuo.valuation.requests.markit.RequestDefinition.DataDefinition.IrSwapDefinition.IrSwapLegDefinition;
import com.acuo.valuation.requests.markit.RequestDefinition.DataDefinition.IrSwapDefinition.IrSwapLegDefinition.IrSwapLegFixingDefinition;
import com.acuo.valuation.requests.markit.RequestDefinition.DataDefinition.IrSwapDefinition.IrSwapLegDefinition.IrSwapLegPayDatesDefinition;

public class MarkitRequestData implements RequestData {

	private MarkitRequestData(DataDefinition definition) {
		definition.swaps.stream().forEach(swap -> new IrSwap(swap));
	}

	public static RequestData of(DataDefinition definition) {
		return new MarkitRequestData(definition);
	}

	class IrSwap {

		private final String tradeId;
		private final Date tradeDate;
		private final String book;
		private final Set<IrSwapLeg> legs;

		public IrSwap(IrSwapDefinition swap) {
			values.put(key(swap.tradeId, IrSwap.class), this);
			tradeId = swap.tradeId;
			tradeDate = swap.tradeDate;
			book = swap.book;
			legs = swap.legs.stream().map(leg -> new IrSwapLeg(leg)).collect(Collectors.toSet());
		}

		public String getTradeId() {
			return tradeId;
		}

		public Date getTradeDate() {
			return tradeDate;
		}

		public String getBook() {
			return book;
		}

		public Set<IrSwapLeg> getLegs() {
			return legs;
		}
	}

	class IrSwapLeg {

		private final int id;
		private final String currency;
		private final IrSwapLegFixing fixing;
		private final Double spread;
		private final Double rate;
		private final String type;
		private final String daycount;
		private final Double notional;
		private final String notionalxg;
		private final IrSwapLegPayDates dates;

		public IrSwapLeg(IrSwapLegDefinition leg) {
			id = leg.id;
			currency = leg.currency;
			fixing = leg.fixing != null ? new IrSwapLegFixing(leg.fixing) : null;
			spread = leg.spread;
			rate = leg.rate;
			type = leg.type;
			daycount = leg.daycount;
			notional = leg.notional;
			notionalxg = leg.notionalxg;
			dates = new IrSwapLegPayDates(leg.payDates);
		}

		public int id() {
			return id;
		}

		public String currency() {
			return currency;
		}

		public IrSwapLegFixing fixing() {
			return fixing;
		}

		public Double spread() {
			return spread;
		}

		public Double rate() {
			return rate;
		}

		public String type() {
			return type;
		}

		public String daycount() {
			return daycount;
		}

		public Double notional() {
			return notional;
		}

		public String notionalxg() {
			return notionalxg;
		}

		public IrSwapLegPayDates payDates() {
			return dates;
		}

	}

	class IrSwapLegFixing {

		private final String name;
		private final String term;
		private final boolean arrears;

		public IrSwapLegFixing(IrSwapLegFixingDefinition fixing) {
			name = fixing.name;
			term = fixing.term;
			arrears = fixing.arrears;
		}

		public String name() {
			return name;
		}

		public String term() {
			return term;
		}

		public boolean isArrears() {
			return arrears;
		}
	}

	class IrSwapLegPayDates {

		private final Date startDate;
		private final String frequency;
		private final Date enddate;
		private final String rollCode;
		private final boolean adjust;
		private final boolean eom;

		public IrSwapLegPayDates(IrSwapLegPayDatesDefinition dates) {
			startDate = dates.startDate;
			frequency = dates.frequency;
			enddate = dates.enddate;
			rollCode = dates.rollCode;
			adjust = dates.adjust;
			eom = dates.eom;
		}

		public Date startDate() {
			return startDate;
		}

		public String frequency() {
			return frequency;
		}

		public Date enddate() {
			return enddate;
		}

		public String rollCode() {
			return rollCode;
		}

		public boolean isAdjust() {
			return adjust;
		}

		public boolean isEom() {
			return eom;
		}
	}
}
