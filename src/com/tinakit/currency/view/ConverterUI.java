package com.tinakit.currency.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import com.tinakit.currency.model.ExchangeRate;
import com.tinakit.currency.service.CurrencyService;

/**
 * Currency Converter UI Object.
 * 
 * <P>
 * UI for the Currency Converter application
 * 
 * 
 * @author Tina Fredericks
 * @version 2.0
 */

public class ConverterUI implements Observer {

	// CONSTANTS
	private static final String LOOK_AND_FEEL = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
	private static final String REGEX_INTEGER_DECIMAL = "^\\d+(\\.\\d*)?|\\.\\d+$";
	private static final String VALIDATION_ERROR = "Please enter an integer or decimal value.";
	private static final String CHECKBOX_ERROR = "Please check at least one checkbox";
	private static final String EMPTY_DOLLAR_ERROR = "Please enter a value for US Dollars.";
	private static final String HEADER_LINE_1 = "1) Enter the dollar amount you want to convert.";
	private static final String HEADER_LINE_2 = "2) Check the currencies you want to convert to.";
	private static final String HEADER_LINE_3 = "3) Click the Convert button.";
	private static final String US_DOLLARS_EQUALS = "US Dollars Equals";
	private final Dimension TEXTFIELD_PREFERRED_SIZE = new Dimension(100, 30);
	private static final int FRAME_WIDTH = 600;
	private static final int FRAME_HEIGHT = 300;

	// UI COMPONENTS
	private JTextField valueDollar;
	private JButton mConvertButton;
	private JButton mClearButton;
	private JFrame mFrame;
	private ArrayList<JCheckBox> mCheckBoxList = new ArrayList<JCheckBox>();
	private JLabel mEquationLabel;

	float[] mValueList;

	// cache
	private List<ExchangeRate> mExchangeRateList;
	private CurrencyService mCurrencyService = null;

	public ConverterUI(List<ExchangeRate> exchangeRateList,
			CurrencyService currencyService) {

		mExchangeRateList = exchangeRateList;
		mCurrencyService = currencyService;
	}

	public void update(Observable observable, Object arg) {

		if (observable == mCurrencyService) {

			// update Converter UI exchange rates
			updateExchangeRateList((List<ExchangeRate>) arg);

			JOptionPane.showMessageDialog(null,
					"CurrencyConverter.update called");
		}

	}

	public void updateExchangeRateList(List<ExchangeRate> list) {

		mExchangeRateList = list;
	}

	/**
	 * CustomSwingWorker Object.
	 * 
	 * <P>
	 * allocates a worker thread to do the task of converting US Dollar amount
	 * to other currencies
	 * <P>
	 * within the dispatch thread, as defined in done(), updates the Currency
	 * Conversion UI to display the values for other currencies in a JLabel
	 * 
	 * @author Tina Fredericks
	 * @version 1.0
	 */
	public class CustomSwingWorker extends SwingWorker<Float, Void> {

		private float mValueUS;
		private Double mExchangeRate;
		private String mCurrencyName;

		private Object lock = new Object();

		public CustomSwingWorker(float valueUS, Double exchangeRate,
				String currencyName) {

			mValueUS = valueUS;
			mExchangeRate = exchangeRate;
			mCurrencyName = currencyName;

		}

		protected Float doInBackground() throws Exception {

			Float valueNonUs = Math.round((mValueUS * mExchangeRate) * 100)
					/ ((float) 100);

			return valueNonUs;

		}

		protected void done() {

			synchronized (lock) {

				float valueNonUS = 0.0f;

				try {

					valueNonUS = get();

					if (!mEquationLabel.getText().equals(""))
						mEquationLabel.setText(mEquationLabel.getText() + ", "
								+ String.valueOf(valueNonUS) + " "
								+ mCurrencyName);
					else
						mEquationLabel.setText(mEquationLabel.getText() + "  "
								+ String.valueOf(valueNonUS) + " "
								+ mCurrencyName);

				} catch (ExecutionException e) {

					e.printStackTrace();

				} catch (InterruptedException ie) {

					ie.printStackTrace();
				}
			}// end lock

		}

	}

	/**
	 * converts dollars to other currencies by calling CustomSwingWorker class
	 * <p>
	 * This method does not return anything and takes no parameters.
	 * 
	 */

	private void convertToNonUS() {

		for (int i = 0; i < mCheckBoxList.size(); i++) {

			if (mCheckBoxList.get(i).isSelected()) {

				// spawn thread if checkbox is checked
				new CustomSwingWorker(Float.parseFloat(valueDollar.getText()),
						mExchangeRateList.get(i).getExchangeRate(),
						mExchangeRateList.get(i).getCountryName()).execute();
			}

		}
	}

	/**
	 * creates the UI for the Currency Converter
	 * <p>
	 * This method does not return anything and takes no parameters.
	 * 
	 */

	public void displayUI() {

		/***** LOOK_AND_FEEL ********************************/
		setLookAndFeel();

		/***** panelHeader ********************************/
		JPanel panelHeader = new JPanel();
		panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.Y_AXIS));

		panelHeader.add(new JLabel(HEADER_LINE_1));
		panelHeader.add(new JLabel(HEADER_LINE_2));
		panelHeader.add(new JLabel(HEADER_LINE_3));

		/***** panelDollar ********************************/
		JPanel panelDollar = new JPanel();
		valueDollar = new JTextField();
		valueDollar.setPreferredSize(TEXTFIELD_PREFERRED_SIZE);
		valueDollar.addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent keyEvent) {

				// clear currency equation to indicate user must click
				// Convert button to get the new values
				clearCurrencyEquation();

				if (keyEvent.getKeyChar() != KeyEvent.VK_BACK_SPACE) {

					if (!validateInput(valueDollar.getText()))

						valueDollar.selectAll();

				}
			}

			public void keyPressed(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent arg0) {
			}

		});

		panelDollar.add(valueDollar);
		JLabel dollarLabel = new JLabel(US_DOLLARS_EQUALS);
		dollarLabel.setFont(new Font("serif", Font.PLAIN, 20));
		panelDollar.add(dollarLabel);

		/***** mEquationLabel ********************************/

		mEquationLabel = new JLabel();
		mEquationLabel.setFont(new Font("serif", Font.PLAIN, 16));

		/***** checkBoxPanel ********************************/

		// add to UI
		JPanel checkBoxPanel = new JPanel();

		// add checkbox and textfield for each currency
		for (int i = 0; i < mExchangeRateList.size(); i++) {

			/***** checkbox for each currency ********************************/
			JCheckBox checkBox = new JCheckBox(mExchangeRateList.get(i)
					.getCountryName());

			checkBox.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					clearCurrencyEquation();
				}

			});

			// put a checkmark on the first three checkboxes
			if (i >= 0 && i < 3)
				checkBox.setSelected(true);

			// save references to checkboxes
			mCheckBoxList.add(checkBox);

			// add checkbox to the panel
			checkBoxPanel.add(checkBox);
		}

		/***** mConvertButton ********************************/
		mConvertButton = new JButton("Convert");
		mConvertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// check if dollar textfield is empty or has white space
				if (valueDollar.getText().trim().isEmpty()) {

					JOptionPane.showMessageDialog(mFrame, EMPTY_DOLLAR_ERROR);
					return;
				}

				if (validateInput(valueDollar.getText())
						&& validateCheckBoxes()) {

					// clear previous text
					clearCurrencyEquation();
					convertToNonUS();
				} else {

					// highlight the text so user can change it by
					// immediately typing
					valueDollar.selectAll();
				}
			}
		});

		/***** mClearButton ********************************/
		mClearButton = new JButton("Clear");
		mClearButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				valueDollar.setText("");
				clearCheckBoxes();
				clearCurrencyEquation();

			}

		});

		/***** buttonPanel ********************************/
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(mClearButton);
		buttonPanel.add(mConvertButton);

		/***** mainPanel ********************************/
		// main panel to be added to ContentPane
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		// topPanel
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.add(panelHeader);
		topPanel.add(panelDollar);
		topPanel.add(mEquationLabel);

		mainPanel.add(BorderLayout.NORTH, topPanel);
		mainPanel.add(BorderLayout.CENTER, checkBoxPanel);
		mainPanel.add(BorderLayout.SOUTH, buttonPanel);

		/***** frame ********************************/
		mFrame = new JFrame("The Currency Machine");
		mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// set the frame properties and make it visible
		mFrame.getContentPane().add(mainPanel);
		mFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		mFrame.setVisible(true);

	}

	/**
	 * clears text from mEquationLabel of all non-US currencies
	 * <p>
	 * This method does not return anything and takes no parameters.
	 * 
	 */

	public void clearCurrencyEquation() {

		mEquationLabel.setText("");

	}

	/**
	 * clears checkmarks from all checkboxes
	 * <p>
	 * This method does not return anything and takes no parameters.
	 * 
	 */

	public void clearCheckBoxes() {

		for (JCheckBox checkBox : mCheckBoxList) {

			checkBox.setSelected(false);
		}
	}

	/**
	 * sets Look & Feel of the Converter UI
	 * <p>
	 * This method does not return anything and takes no parameters.
	 * 
	 */

	public void setLookAndFeel() {

		try {
			UIManager.setLookAndFeel(LOOK_AND_FEEL);
		} catch (Exception error) {
			error.printStackTrace();
		}
	}

	/**
	 * validates that at least one checkbox is checked
	 * <p>
	 * This method does not return anything and takes no parameters.
	 * 
	 */

	private boolean validateCheckBoxes() {

		for (JCheckBox checkBox : mCheckBoxList) {
			if (checkBox.isSelected()) {
				return true;
			}
		}

		// no checkbox has been selected, display message
		JOptionPane.showMessageDialog(mFrame, CHECKBOX_ERROR);
		return false;
	}

	/**
	 * validates for integer and decimal values
	 * <p>
	 * This method does not return anything and takes no parameters.
	 * 
	 */
	public boolean validateInput(String input) {

		Pattern pattern = Pattern.compile(REGEX_INTEGER_DECIMAL);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {

			JOptionPane.showMessageDialog(mFrame, VALIDATION_ERROR);
			return false;
		}

		return true;
	}

}
