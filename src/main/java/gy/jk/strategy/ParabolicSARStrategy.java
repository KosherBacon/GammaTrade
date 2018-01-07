package gy.jk.strategy;

import com.google.inject.Inject;
import org.ta4j.core.*;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.MaxPriceIndicator;
import org.ta4j.core.indicators.helpers.MinPriceIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
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

    RSIIndicator rsi = new RSIIndicator(closePrice, 14);

    ParabolicSarIndicator parabolicSar =
        new ParabolicSarIndicator(timeSeries, Decimal.valueOf("0.025"), Decimal.valueOf("0.050"));

    PreviousValueIndicator second = new PreviousValueIndicator(parabolicSar);
    PreviousValueIndicator third = new PreviousValueIndicator(second);

    // Trend confirmed when all three indicate the same trend.
    Rule entryRule = new UnderIndicatorRule(parabolicSar, minPrice)
        .and(new UnderIndicatorRule(second, minPrice))
        .and(new UnderIndicatorRule(third, minPrice));

    Rule sarExit = new OverIndicatorRule(parabolicSar, maxPrice)
        .and(new OverIndicatorRule(second, maxPrice))
        .and(new OverIndicatorRule(third, maxPrice));

    Rule exitRule = sarExit.or(new CrossedDownIndicatorRule(rsi, Decimal.valueOf(64)));

    return new BaseStrategy(entryRule, exitRule);
  }
}
