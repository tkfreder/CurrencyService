package com.tinakit.currency.model;

public class ExchangeRate {

	private String mCountryName;
	private double mExchangeRate;

	public ExchangeRate(String countryName, double exchangeRate) {

		mCountryName = countryName;
		mExchangeRate = exchangeRate;

	}

	public String getCountryName() {
		return mCountryName;
	}

	public void setCountryName(String countryName) {

		mCountryName = countryName;
	}

	public double getExchangeRate() {

		return mExchangeRate;
	}

	public void setExchangeRate(double exchangeRate) {

		mExchangeRate = exchangeRate;
	}

}
