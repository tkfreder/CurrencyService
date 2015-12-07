package com.tinakit.currency.model;

public class ExchangeRate {

	String countryName;
	float exchangeRate;

	/*
	 * public ExchangeRate(String countryName, float exchangeRate) { super();
	 * this.countryName = countryName; this.exchangeRate = exchangeRate;
	 * 
	 * }
	 */
	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public float getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(float exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

}
