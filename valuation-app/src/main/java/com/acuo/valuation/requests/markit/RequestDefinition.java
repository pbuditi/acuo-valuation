package com.acuo.valuation.requests.markit;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import com.acuo.common.marshal.jaxb.DateAdapter;
import com.acuo.common.marshal.jaxb.DecimalAdapter;
import com.acuo.valuation.requests.Request;
import com.acuo.valuation.requests.RequestData;
import com.acuo.valuation.requests.markit.MarkitRequestData.IrSwap;
import com.acuo.valuation.requests.markit.MarkitRequestData.IrSwapLeg;
import com.acuo.valuation.requests.markit.MarkitRequestData.IrSwapLegFixing;
import com.acuo.valuation.requests.markit.MarkitRequestData.IrSwapLegPayDates;

@XmlRootElement(name = "presentvalue")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestDefinition {

	public RequestDefinition() {
		this.data = new DataDefinition();
	}

	private RequestDefinition(Request request) {
		this.valuationDate = request.getValuationDate();
		this.valuationCurrency = request.getValuationCurrency();
		this.data = new DataDefinition(request.getData());
	}

	public static RequestDefinition definition(Request request) {
		return new RequestDefinition(request);
	}

	public Request request() {
		return MarkitRequest.of(this);
	}

	@XmlPath("valuationdate/text()")
	@XmlJavaTypeAdapter(DateAdapter.class)
	Date valuationDate;

	@XmlPath("valuationcurrency/text()")
	String valuationCurrency;

	static class DataDefinition {

		private DataDefinition() {

		}

		private DataDefinition(RequestData data) {
			List<IrSwap> values = data.values(IrSwap.class);
			swaps = values.stream().map(swap -> new IrSwapDefinition(swap)).collect(Collectors.toList());
		}

		static class IrSwapDefinition {

			private IrSwapDefinition() {

			}

			private IrSwapDefinition(IrSwap swap) {
				tradeId = swap.getTradeId();
				tradeDate = swap.getTradeDate();
				book = swap.getBook();
				legs = swap.getLegs().stream().map(leg -> new IrSwapLegDefinition(leg)).collect(Collectors.toList());
			}

			@XmlPath("tradeid/text()")
			String tradeId;

			@XmlPath("tradedate/text()")
			@XmlJavaTypeAdapter(DateAdapter.class)
			Date tradeDate;

			@XmlPath("book/text()")
			String book;

			static class IrSwapLegDefinition {

				private IrSwapLegDefinition() {

				}

				private IrSwapLegDefinition(IrSwapLeg leg) {
					id = leg.id();
					currency = leg.currency();
					fixing = leg.fixing() != null ? new IrSwapLegFixingDefinition(leg.fixing()) : null;
					spread = leg.spread();
					rate = leg.rate();
					type = leg.type();
					daycount = leg.daycount();
					notional = leg.notional();
					notionalxg = leg.notionalxg();
					payDates = new IrSwapLegPayDatesDefinition(leg.payDates());
				}

				@XmlPath("id/text()")
				int id;

				@XmlPath("currency/text()")
				String currency;

				@XmlElement(name = "fixing")
				IrSwapLegFixingDefinition fixing;

				@XmlPath("spread/text()")
				@XmlJavaTypeAdapter(DecimalAdapter.class)
				Double spread;

				@XmlPath("rate/text()")
				@XmlJavaTypeAdapter(DecimalAdapter.class)
				Double rate;

				@XmlPath("type/text()")
				String type;

				@XmlPath("daycount/text()")
				String daycount;

				@XmlPath("notional/text()")
				@XmlJavaTypeAdapter(DecimalAdapter.class)
				Double notional;

				@XmlPath("notionalxg/text()")
				String notionalxg;

				static class IrSwapLegFixingDefinition {

					private IrSwapLegFixingDefinition() {

					}

					private IrSwapLegFixingDefinition(IrSwapLegFixing fixing) {
						name = fixing.name();
						term = fixing.term();
						arrears = fixing.isArrears();
					}

					@XmlPath("name/text()")
					String name;

					@XmlPath("term/text()")
					String term;

					@XmlPath("arrears/text()")
					boolean arrears;

				}

				static class IrSwapLegPayDatesDefinition {

					private IrSwapLegPayDatesDefinition() {

					}

					private IrSwapLegPayDatesDefinition(IrSwapLegPayDates dates) {
						startDate = dates.startDate();
						frequency = dates.frequency();
						enddate = dates.enddate();
						rollCode = dates.rollCode();
						adjust = dates.isAdjust();
						eom = dates.isEom();
					}

					@XmlPath("startdate/text()")
					@XmlJavaTypeAdapter(DateAdapter.class)
					Date startDate;

					@XmlPath("freq/text()")
					String frequency;

					@XmlPath("enddate/text()")
					@XmlJavaTypeAdapter(DateAdapter.class)
					Date enddate;

					@XmlPath("rollcode/text()")
					String rollCode;

					@XmlPath("adjust/text()")
					boolean adjust;

					@XmlPath("eom/text()")
					boolean eom;
				}

				@XmlElement(name = "paydates")
				IrSwapLegPayDatesDefinition payDates;
			}

			@XmlElement(name = "leg")
			public List<IrSwapLegDefinition> legs;
		}

		@XmlElement(name = "irswap")
		public List<IrSwapDefinition> swaps;
	}

	@XmlElement(name = "data", required = true)
	public final DataDefinition data;
}
