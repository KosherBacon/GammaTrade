package gy.jk.strategy;

import com.google.inject.Inject;
import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.MaxPriceIndicator;
import org.ta4j.core.indicators.helpers.MinPriceIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

public class ParabolicSARStrategy implements StrategyBuilder {

  @Inject
  ParabolicSARStrategy() {
  }

  @Override
  public Strategy buildStrategy(TimeSeries timeSeries) {

    ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);

    MinPriceIndicator minPrice = new MinPriceIndicator(timeSeries);
    MaxPriceIndicator maxPrice = new MaxPriceIndicator(timeSeries);

    // RSI
    RSIIndicator rsi = new RSIIndicator(closePrice, 14);

    // MACD
    MACDIndicator macd = new MACDIndicator(closePrice, 9, 26);
    ZLEMAIndicator emaMacd = new ZLEMAIndicator(macd, 18);
    Rule macdCrossUp = new CrossedUpIndicatorRule(emaMacd, macd);

    // Check MACD for recent crossing
    PreviousValueIndicator macdPrev = new PreviousValueIndicator(macd);
    PreviousValueIndicator emaMacdPrev = new PreviousValueIndicator(emaMacd);
    Rule macdCrossUpPrev = new CrossedUpIndicatorRule(emaMacdPrev, macdPrev);

    // Check MACD for recent crossing
    PreviousValueIndicator macdPrev2 = new PreviousValueIndicator(macdPrev);
    PreviousValueIndicator emaMacdPrev2 = new PreviousValueIndicator(emaMacdPrev);
    Rule macdCrossUpPrev2 = new CrossedUpIndicatorRule(emaMacdPrev2, macdPrev2);

    // Recent MACD crossing
    Rule macdCrossing = macdCrossUp.or(macdCrossUpPrev).or(macdCrossUpPrev2);

    // ParabolicSAR
    ParabolicSarIndicator parabolicSar =
        new ParabolicSarIndicator(timeSeries, Decimal.valueOf("0.025"), Decimal.valueOf("0.050"));

    PreviousValueIndicator second = new PreviousValueIndicator(parabolicSar);
    PreviousValueIndicator third = new PreviousValueIndicator(second);

    // Trend confirmed when all three indicate the same trend.
    Rule entryRule = new UnderIndicatorRule(parabolicSar, minPrice)
        .and(new UnderIndicatorRule(second, minPrice))
        .and(new UnderIndicatorRule(third, minPrice))
        .and(macdCrossing);

    Rule sarExit = new OverIndicatorRule(parabolicSar, maxPrice)
        .and(new OverIndicatorRule(second, maxPrice))
        .and(new OverIndicatorRule(third, maxPrice));

    Rule exitRule = sarExit.or(new CrossedDownIndicatorRule(rsi, Decimal.valueOf(64)));

    return new BaseStrategy(entryRule, exitRule);
  }
}
