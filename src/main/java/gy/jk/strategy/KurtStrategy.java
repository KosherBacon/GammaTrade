package gy.jk.strategy;

import com.google.inject.Inject;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;

public class KurtStrategy implements StrategyBuilder {

  @Inject
  KurtStrategy() {
  }

  @Override
  public Strategy buildStrategy(TimeSeries timeSeries) {
    return null;
  }
}
