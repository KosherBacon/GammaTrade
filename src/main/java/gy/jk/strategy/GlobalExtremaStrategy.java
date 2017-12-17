package gy.jk.strategy;

import com.google.inject.Inject;
import gy.jk.tick.Annotations.TickLengthMillis;
import org.ta4j.core.*;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.StopLossRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

public class GlobalExtremaStrategy implements StrategyBuilder {

  private static final int MILLIS_PER_30_MINUTES = 30 * 60 * 1000;

  private final int numTicks;

  @Inject
  GlobalExtremaStrategy(@TickLengthMillis long tickLengthMillis) {
    this.numTicks = (int) (MILLIS_PER_30_MINUTES / tickLengthMillis);
  }

  @Override
  public Strategy buildStrategy(TimeSeries timeSeries) {
    ClosePriceIndicator closePrices = new ClosePriceIndicator(timeSeries);

    // Getting the max price over the past week
    MaxPriceIndicator maxPrices = new MaxPriceIndicator(timeSeries);
    HighestValueIndicator weekMaxPrice = new HighestValueIndicator(maxPrices, numTicks);
    // Getting the min price over the past week
    MinPriceIndicator minPrices = new MinPriceIndicator(timeSeries);
    LowestValueIndicator weekMinPrice = new LowestValueIndicator(minPrices, numTicks);

    // Going long if the close price goes below the min price
    MultiplierIndicator downWeek = new MultiplierIndicator(weekMinPrice, Decimal.valueOf("1.004"));
    Rule buyingRule = new UnderIndicatorRule(closePrices, downWeek);

    // Going short if the close price goes above the max price
    MultiplierIndicator upWeek = new MultiplierIndicator(weekMaxPrice, Decimal.valueOf("0.996"));
    Rule sellingRule = new OverIndicatorRule(closePrices, upWeek)
        .or(new StopLossRule(closePrices, Decimal.valueOf(2)));

    return new BaseStrategy(buyingRule, sellingRule);
  }
}
