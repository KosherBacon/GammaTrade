package gy.jk.strategy;

import com.google.inject.Inject;
import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.candles.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.*;

public class MomentumStrategy implements StrategyBuilder {

  @Inject
  MomentumStrategy() {
  }

  @Override
  public Strategy buildStrategy(TimeSeries timeSeries) {
    ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);

    // The bias is bullish when the shorter-moving average moves above the longer moving average.
    // The bias is bearish when the shorter-moving average moves below the longer moving average.
    EMAIndicator shortEma = new EMAIndicator(closePrice, 9);
    EMAIndicator longEma = new EMAIndicator(closePrice, 26);

    StochasticOscillatorKIndicator stochasticOscillK = new StochasticOscillatorKIndicator(timeSeries, 14);

    MACDIndicator macd = new MACDIndicator(closePrice, 9, 26);
    ZLEMAIndicator emaMacd = new ZLEMAIndicator(macd, 18);

    // Entry rule
    Rule entryRule = new OverIndicatorRule(shortEma, longEma) // Trend
        .and(new CrossedDownIndicatorRule(stochasticOscillK, Decimal.valueOf(20))) // Signal 1
        .and(new OverIndicatorRule(macd, emaMacd)); // Signal 2

    // Exit rule
    Rule exitRule = new UnderIndicatorRule(shortEma, longEma) // Trend
        .and(new CrossedUpIndicatorRule(stochasticOscillK, Decimal.valueOf(80))) // Signal 1
        .and(new UnderIndicatorRule(macd, emaMacd)); // Signal 2

    return new BaseStrategy(entryRule, exitRule);
  }
}
