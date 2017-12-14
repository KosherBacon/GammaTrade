package gy.jk.strategy;

import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;

public interface StrategyBuilder {

  Strategy buildStrategy(TimeSeries timeSeries);

}
