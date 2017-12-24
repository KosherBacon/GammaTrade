package gy.jk.strategy;

import com.google.inject.Inject;
import org.ta4j.core.Decimal;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.helpers.MaxPriceIndicator;
import org.ta4j.core.indicators.helpers.MinPriceIndicator;

public class ParabolicSARStrategy implements StrategyBuilder {

  @Inject
  ParabolicSARStrategy() {
  }

  @Override
  public Strategy buildStrategy(TimeSeries timeSeries) {

    MinPriceIndicator minPrice = new MinPriceIndicator(timeSeries);
    MaxPriceIndicator maxPrice = new MaxPriceIndicator(timeSeries);

    ParabolicSarIndicator parabolicSar =
        new ParabolicSarIndicator(timeSeries, Decimal.valueOf("0.025"), Decimal.valueOf("0.050"));

    return null;
  }
}
