package gy.jk.strategy;

import com.google.inject.Inject;
import org.ta4j.core.*;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

/**
 * Represents a primitive RSI trading strategy.
 *
 * @author Joshua Kahn
 */
public class RSIStrategy implements StrategyBuilder {

  private static final int UNSTABLE_PERIOD = 5;

  @Inject
  RSIStrategy() {
  }

  public Strategy buildStrategy(TimeSeries timeSeries) {
    ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
    SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
    SMAIndicator longSma = new SMAIndicator(closePrice, 200);

    // We use a 2-period RSI indicator to identify buying
    // or selling opportunities within the bigger trend.
    RSIIndicator rsi = new RSIIndicator(closePrice, 2);

    // Entry rule
    // The long-term trend is up when a security is above its 200-period SMA.
    Rule entryRule = new OverIndicatorRule(shortSma, longSma) // Trend
        .and(new CrossedDownIndicatorRule(rsi, Decimal.valueOf(5))) // Signal 1
        .and(new OverIndicatorRule(shortSma, closePrice)); // Signal 2

    // Exit rule
    // The long-term trend is down when a security is below its 200-period SMA.
    Rule exitRule = new UnderIndicatorRule(shortSma, longSma) // Trend
        .and(new CrossedUpIndicatorRule(rsi, Decimal.valueOf(95))) // Signal 1
        .and(new UnderIndicatorRule(shortSma, closePrice)); // Signal 2

    BaseStrategy strategy = new BaseStrategy(entryRule, exitRule);
    strategy.setUnstablePeriod(UNSTABLE_PERIOD);
    return strategy;
  }
}
