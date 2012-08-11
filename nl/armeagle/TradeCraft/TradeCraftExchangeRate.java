package nl.armeagle.TradeCraft;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TradeCraftExchangeRate {
    public int amount;
    public int value;

    private static final Pattern ratePattern = Pattern.compile("(\\d+)\\D+(\\d+)\\s*");

    TradeCraftExchangeRate(String signLine) {
		Matcher matcher = ratePattern.matcher(signLine);

		if (matcher.find()) {
			this.amount = Integer.parseInt(matcher.group(1));
			this.value = Integer.parseInt(matcher.group(2));
		} else {
			this.amount = 0;
			this.value = 0;
		}
    }
    
    public boolean isValid() {
    	return this.amount != 0;
    }
}
