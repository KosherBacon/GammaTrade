package gy.jk.strategy;

import com.google.inject.Inject;
import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.candles.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.trading.rules.*;

public class MomentumStrategy implements StrategyBuilder {

  @Inject
  MomentumStrategy() {
  }

  @Override
  public Strategy buildStrategy(TimeSeries timeSeries) {
    ClosePriceIndicator closePrices = new ClosePriceIndicator(timeSeries);

    ChaikinMoneyFlowIndicator moneyFlowIndicator =
        new ChaikinMoneyFlowIndicator(timeSeries, 8);
    CMOIndicator cmoIndicator = new CMOIndicator(closePrices, 3);

    Rule entryRule = new OverIndicatorRule(moneyFlowIndicator, Decimal.ZERO)
        .and(new CrossedUpIndicatorRule(cmoIndicator, Decimal.valueOf(50)));

    Rule exitRule = new UnderIndicatorRule(moneyFlowIndicator, Decimal.ZERO)
        .and(new CrossedDownIndicatorRule(cmoIndicator, Decimal.valueOf(-50)));

    return new BaseStrategy(entryRule, exitRule);
  }
}
