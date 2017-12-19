package gy.jk.strategy;

import com.google.inject.Inject;
import org.ta4j.core.*;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;

public class BollingerStrategy implements StrategyBuilder {

  @Inject
  BollingerStrategy() {
  }

  @Override
  public Strategy buildStrategy(TimeSeries timeSeries) {
    ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);

    SMAIndicator sma = new SMAIndicator(closePrice, 20);
    StandardDeviationIndicator std = new StandardDeviationIndicator(closePrice, 20);

    BollingerBandsMiddleIndicator bbmSMA = new BollingerBandsMiddleIndicator(sma);
    BollingerBandsLowerIndicator bblSMA = new BollingerBandsLowerIndicator(bbmSMA, std);
    BollingerBandsUpperIndicator bbuSMA = new BollingerBandsUpperIndicator(bbmSMA, std);

    Rule entryRule = new CrossedUpIndicatorRule(closePrice, bbuSMA);

    Rule exitRule = new CrossedDownIndicatorRule(closePrice, bblSMA);

    return new BaseStrategy(entryRule, exitRule);
  }
}
