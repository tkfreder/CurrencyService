package com.tinakit.currency;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

import com.tinakit.currency.service.CurrencyService;
import com.tinakit.currency.view.ConverterUI;

/**
 * Currency Converter Application Object.
 * 
 * <P>
 * converts one currency into another
 * 
 * 
 * @author Tina Fredericks
 * @version 2.0
 */

public class CurrencyConverter {

	// constants
	private static final int POLL_PERIOD = 3000; // in milliseconds
	private static final int POLLING_TIME_LIMIT = 10000; // in milliseconds

	// cache
	public static List<Double> mExchangeRateList;
	private static CurrencyService mCurrencyService = null;
	private static ConverterUI mConverterUI;

	// executor
	private static final ScheduledExecutorService mExecutor = Executors
			.newSingleThreadScheduledExecutor();

	public CurrencyConverter() {

		mCurrencyService = new CurrencyService();

	}

	/**
	 * starting point for CurrencyConverter
	 * 
	 * <p>
	 * 
	 * @param args
	 *            command line String arguments
	 * 
	 */

	public static void main(String[] args) {

		new CurrencyConverter();

		mConverterUI = new ConverterUI(mCurrencyService.getExchangeRateList(),
				mCurrencyService);

		mConverterUI.displayUI();

		mCurrencyService.addObserver(mConverterUI);

		// start service to periodically poll for latest exchange rates

		mExecutor.scheduleAtFixedRate(mCurrencyService, 0, POLL_PERIOD,
				TimeUnit.SECONDS);

		// stop polling after 10 seconds
		Timer timer = new Timer(POLLING_TIME_LIMIT, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				mExecutor.shutdown();
			}
		});

		// Only execute once
		timer.setRepeats(false);
		timer.start();

	}

}
