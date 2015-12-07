package com.tinakit.currency.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.tinakit.currency.CurrencyConverter;
import com.tinakit.currency.model.ExchangeRate;

public class CurrencyService implements Runnable {

	protected static ArrayList<ExchangeRate> mConversionList = new ArrayList<ExchangeRate>();
	private static final String URL_YAHOO = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.xchange%20where%20pair%20in%20(%22USDEUR%22%2C%22USDAUD%22%2C%22USDGBP%22%2C%22USDCNY%22%2C%22USDDKK%22%2C%20%22USDINR%22%2C%20%22USDMXN%22%2C%20%22USDPEN%22%2C%20%22USDSAR%22%2C%20%22USDVND%22)&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

	private ArrayList<ExchangeRate> mCurrencyList;

	private CurrencyConverter mConverter;

	public CurrencyService(CurrencyConverter converter) {
		mCurrencyList = new ArrayList<ExchangeRate>();
		mConverter = converter;
	}

	public ArrayList<ExchangeRate> getCurrencyList() {
		return mCurrencyList;
	}

	public void run() {

		XmlPullParser parser = null;
		ExchangeRate exchangeRate = null;

		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			parser = factory.newPullParser();

			// String fullUrl = buildUrl();

			parser.setInput(new InputStreamReader(getUrlData(URL_YAHOO)));

			beginDocument(parser, "query");

			int eventType = parser.getEventType();

			String countryName = "";
			float exchange = 0.0f;
			String tagName = "";
			// tracking array index
			int currencyIndex = 0;

			while (eventType != XmlPullParser.END_DOCUMENT) {

				switch (eventType) {

				case XmlPullParser.START_TAG:
					if (parser.getName().equals("Name")) {
						tagName = parser.getName();
						exchangeRate = new ExchangeRate();
					} else if (parser.getName().equals("Rate"))
						tagName = parser.getName();
					break;

				case XmlPullParser.TEXT:
					if (tagName.equals("Name")) {
						countryName = parser.getText();
					} else if (tagName.equals("Rate")) {
						exchange = Float.parseFloat(parser.getText());
					}
					break;

				case XmlPullParser.END_TAG:
					if (tagName.equals("Name")) {
						// exchangeRate.setCountryName(countryName);
						exchangeRate
								.setCountryName(CurrencyConverter.CURRENCY_LIST[currencyIndex]);
						countryName = "";

					} else if (tagName.equals("Rate")
							&& exchangeRate.getCountryName() != null) {
						exchangeRate.setExchangeRate(exchange);
						mCurrencyList.add(exchangeRate);
						currencyIndex++;
					}
					tagName = "";
					break;

				default:
					break;

				}
				eventType = parser.next();
			}

			// TODO: the update should be done from CurrencyConverter using
			// Callable
			// update the CurrencyConverter exchange rate values
			// (conversionList)
			for (int i = 0; i < mCurrencyList.size(); i++) {
				mConverter.conversionList[i] = mCurrencyList.get(i)
						.getExchangeRate();
			}

			// display dialog to indicate the update has been made
			JOptionPane.showMessageDialog(null,
					"exchange rates have been updated");
		}

		catch (ClientProtocolException e) {
			e.printStackTrace();
		}

		catch (XmlPullParserException e) {
			e.printStackTrace();
		}

		catch (URISyntaxException e) {
			e.printStackTrace();
		}

		catch (IOException e) {
			e.printStackTrace();
		} finally {
		}

	}

	public InputStream getUrlData(String url) throws URISyntaxException,
			ClientProtocolException {

		HttpClient client = HttpClientBuilder.create().build();

		HttpGet method = new HttpGet(new URI(url));

		try {
			HttpResponse res = client.execute(method);
			return res.getEntity().getContent();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	public final void beginDocument(XmlPullParser parser,
			String firstElementName) throws XmlPullParserException, IOException {
		int type;

		while ((type = parser.next()) != XmlPullParser.START_TAG
				&& type != XmlPullParser.END_DOCUMENT) {

		}

		if (type != XmlPullParser.START_TAG) {
			throw new XmlPullParserException("No Start Tag Found");
		}
		if (!parser.getName().equals(firstElementName)) {
			throw new XmlPullParserException("Unexpected Start Tag Found "
					+ parser.getName() + ", expected " + firstElementName);
		}
	}

}
