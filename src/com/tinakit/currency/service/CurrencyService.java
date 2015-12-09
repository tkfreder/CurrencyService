package com.tinakit.currency.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import javax.swing.JOptionPane;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.tinakit.currency.model.ExchangeRate;

public class CurrencyService extends Observable implements Runnable {

	private static final String[] CURRENCY_LIST = { "Euro",
			"Australian Dollar", "British Pound", "Chinese Yuan",
			"Danish Krone", "Indian Rupee", "Mexican Peso",
			"Peruvian Nuevo Sol", "Saudi Riyal", "Vietnamese Dong" };

	// placeholder exchange rates are based on US Dollar
	private static double[] EXCHANGE_RATE_LIST = { 0.9, 1.35, 0.65, 6.2, 6.75,
			63.47, 15.93, 3.18, 3.75, 21814 };

	private static final String URL_YAHOO = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.xchange%20where%20pair%20in%20(%22USDEUR%22%2C%22USDAUD%22%2C%22USDGBP%22%2C%22USDCNY%22%2C%22USDDKK%22%2C%20%22USDINR%22%2C%20%22USDMXN%22%2C%20%22USDPEN%22%2C%20%22USDSAR%22%2C%20%22USDVND%22)&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

	// cache
	public List<ExchangeRate> mExchangeRateList;
	private List<ExchangeRate> mExchangeRateList_previous;

	public CurrencyService() {

		mExchangeRateList = new ArrayList<>();
		mExchangeRateList_previous = new ArrayList<>();

		initializeList();

	}

	public List<ExchangeRate> getExchangeRateList() {

		return mExchangeRateList;
	}

	private void initializeList() {

		for (int i = 0; i < CURRENCY_LIST.length; i++) {

			mExchangeRateList.add(new ExchangeRate(CURRENCY_LIST[i],
					EXCHANGE_RATE_LIST[i]));
		}

	}

	public void run() {

		XmlPullParser parser = null;
		ExchangeRate exchangeRate = null;

		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			parser = factory.newPullParser();

			parser.setInput(new InputStreamReader(getUrlData(URL_YAHOO)));

			beginDocument(parser, "query");

			int eventType = parser.getEventType();

			String countryName = "";
			double exchange = 0.0;
			String tagName = "";
			// tracking array index
			int currencyIndex = 0;

			// flag to check whether there is at least one valid entry
			boolean hasFirstElement = false;

			while (eventType != XmlPullParser.END_DOCUMENT) {

				switch (eventType) {

				case XmlPullParser.START_TAG:
					if (parser.getName().equals("Name")) {
						tagName = parser.getName();

						// if there is at least one valid entry
						if (hasFirstElement == false) {
							hasFirstElement = true;

							// save current list as previous list
							mExchangeRateList_previous = mExchangeRateList;

							// clear out ExchangeRateList
							mExchangeRateList = new ArrayList<>();
						}
					} else if (parser.getName().equals("Rate"))
						tagName = parser.getName();
					break;

				case XmlPullParser.TEXT:
					if (tagName.equals("Name")) {
						countryName = CURRENCY_LIST[currencyIndex];
					} else if (tagName.equals("Rate")) {
						exchange = Float.parseFloat(parser.getText());
					}
					break;

				case XmlPullParser.END_TAG:
					if (tagName.equals("Rate") && countryName != "") {
						mExchangeRateList.add(new ExchangeRate(countryName,
								exchange));

						// clear out countryName and exchange rate
						countryName = "";
						exchange = 0.0;
						currencyIndex++;
					}
					tagName = "";
					break;

				default:
					break;

				}
				eventType = parser.next();
			}

			// if the list has changed since the last pull, notify observers
			if (!hasSameEntries(mExchangeRateList, mExchangeRateList_previous)) {

				// Notify observers that data set has changed
				setChanged();
				notifyObservers(mExchangeRateList);

				// display dialog to indicate the update has been made
				JOptionPane.showMessageDialog(null,
						"exchange rates have been updated");
			}

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

	private boolean hasSameEntries(List<ExchangeRate> currentList,
			List<ExchangeRate> previousList) {

		for (int i = 0; i < currentList.size(); i++) {

			if (currentList.get(i).getCountryName()
					.equals(previousList.get(i).getCountryName())) {

				if (currentList.get(i).getExchangeRate() != previousList.get(i)
						.getExchangeRate())
					return false;
			}
		}

		return true;
	}

}
