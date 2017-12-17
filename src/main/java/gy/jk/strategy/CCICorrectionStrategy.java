package gy.jk.strategy;

import com.google.inject.Inject;
import org.ta4j.core.*;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

public class CCICorrectionStrategy implements StrategyBuilder {

  @Inject
  CCICorrectionStrategy() {
  }

  @Override
  public Strategy buildStrategy(TimeSeries timeSeries) {
    CCIIndicator longCci = new CCIIndicator(timeSeries, 200);
    CCIIndicator shortCci = new CCIIndicator(timeSeries, 5);
    Decimal plus100 = Decimal.HUNDRED;
    Decimal minus100 = Decimal.valueOf(-100);

    Rule entryRule = new OverIndicatorRule(longCci, plus100) // Bull trend
        .and(new UnderIndicatorRule(shortCci, minus100)); // Signal

    Rule exitRule = new UnderIndicatorRule(longCci, minus100) // Bear trend
        .and(new OverIndicatorRule(shortCci, plus100)); // Signal

    Strategy strategy = new BaseStrategy(entryRule, exitRule);
    strategy.setUnstablePeriod(5);
    return strategy;
  }
}
