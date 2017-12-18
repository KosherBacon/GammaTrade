package gy.jk.strategy;

import com.google.inject.Inject;
import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.volume.NVIIndicator;
import org.ta4j.core.trading.rules.*;

public class PrimaryStrategy implements StrategyBuilder {

  /**
   * The number of ticks needed for the strategy to be fully functional.
   */
  private static final int UNSTABLE_PERIOD = 3;

  /**
   * The number of periods to use for the Chande Momentum Oscillator.
   */
  private static final int CMO_PERIODS = 3;

  /**
   * The upper bound to use for the Chande Momentum Oscillator
   */
  private static final int CMO_UPPER = 50;

  /**
   * The lower bound to use for the Chande Momentum Oscillator
   */
  private static final int CMO_LOWER = -50;

  /**
   * The number of periods to use for the short running part of the MACD.
   */
  private static final int MACD_SHORT_PERIODS = 9;

  /**
   * The number of periods to use for the long running part of the MACD.
   */
  private static final int MACD_LONG_PERIODS = 26;

  /**
   * The number of periods to use for the EMA part of the MACD.
   */
  private static final int MACD_EMA_PERIODS = 18;

  /**
   * The number of periods to use for the short running SMA.
   */
  private static final int SMA_SHORT_PERIODS = 5;

  /**
   * The number of periods to use for the long running SMA.
   */
  private static final int SMA_LONG_PERIODS = 10;

  /**
   * The number of periods to use for the short running EMA.
   */
  private static final int EMA_SHORT_PERIODS = 5;

  /**
   * The number of periods to use for the long running EMA.
   */
  private static final int EMA_LONG_PERIODS = 10;

  /**
   * The threshold (in percent) for the maximum loss allowed on a given
   * trade. This value protects against severe losses, lower values should be
   * used for more conservative trading.
   */
  private static final int STOP_LOSS_THRESHOLD = 10;

  /**
   * The threshold (in percent) for the maximum gain allowed on a given
   * trade. This value protects the gains from a trade, lower values should
   * be used for more conservative trading.
   */
  private static final int STOP_GAIN_THRESHOLD = 10;

  private static final int NUM_TICKS = 60;

  @Inject
  PrimaryStrategy() {
  }

  @Override
  public Strategy buildStrategy(TimeSeries timeSeries) {

    ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);

    SMAIndicator shortSma = new SMAIndicator(closePrice, SMA_SHORT_PERIODS);
    SMAIndicator longSma = new SMAIndicator(closePrice, SMA_LONG_PERIODS);

    CMOIndicator cmo = new CMOIndicator(closePrice, CMO_PERIODS);

    // The bias is bearish when the shorter-moving average moves below the
    // longer moving average.
    ZLEMAIndicator shortEma = new ZLEMAIndicator(closePrice, EMA_SHORT_PERIODS);
    EMAIndicator longEma = new EMAIndicator(closePrice, EMA_LONG_PERIODS);

    StochasticOscillatorKIndicator stochasticOscillK = new
        StochasticOscillatorKIndicator(timeSeries, 14);

    MACDIndicator macd = new MACDIndicator(closePrice, MACD_SHORT_PERIODS,
        MACD_LONG_PERIODS);
    EMAIndicator emaMacd = new EMAIndicator(macd, MACD_EMA_PERIODS);

    NVIIndicator nviIndicator = new NVIIndicator(timeSeries);
    Rule nviEntryRule = new OverIndicatorRule(nviIndicator, longEma);

    // Getting the max price over the past hour
    MaxPriceIndicator maxPrices = new MaxPriceIndicator(timeSeries);
    HighestValueIndicator hourMaxPrice = new HighestValueIndicator(maxPrices, NUM_TICKS);
    // Getting the min price over the past hour
    MinPriceIndicator minPrices = new MinPriceIndicator(timeSeries);
    LowestValueIndicator hourMinPrice = new LowestValueIndicator(minPrices, NUM_TICKS);

    // Going long if the close price goes below the min price
    MultiplierIndicator downWeek = new MultiplierIndicator(hourMinPrice, Decimal.valueOf("1.004"));
    Rule buyingRule = new UnderIndicatorRule(closePrice, downWeek);

    // Going short if the close price goes above the max price
    MultiplierIndicator upWeek = new MultiplierIndicator(hourMaxPrice, Decimal.valueOf("0.996"));
    Rule sellingRule = new OverIndicatorRule(closePrice, upWeek)
        .or(new StopLossRule(closePrice, Decimal.valueOf(2)));

    Rule momentumEntry = new OverIndicatorRule(shortSma, longSma) // Trend
        // Signal 1
        .and(new CrossedDownIndicatorRule(cmo, Decimal.valueOf(CMO_LOWER)))
        // Signal 2
        .and(new OverIndicatorRule(shortEma, closePrice));

    Rule entryRule = new OverIndicatorRule(shortEma, longEma) // Trend
        // Signal 1
        .and(new CrossedDownIndicatorRule(stochasticOscillK,
            Decimal.valueOf(20)))
        // Signal 2
        .and(new OverIndicatorRule(macd, emaMacd))
        .or(momentumEntry.and(nviEntryRule))
        .or(buyingRule);

    Rule momentumExit = new UnderIndicatorRule(shortSma, longSma) // Trend
        // Signal 1
        .and(new CrossedUpIndicatorRule(cmo, Decimal.valueOf(CMO_UPPER)))
        // Signal 2
        .and(new UnderIndicatorRule(shortSma, closePrice));

    // Exit rule
    Rule exitRule = new UnderIndicatorRule(shortEma, longEma) // Trend
        // Signal 1
        .and(new CrossedUpIndicatorRule(stochasticOscillK,
            Decimal.valueOf(80)))
        // Signal 2
        .and(new UnderIndicatorRule(macd, emaMacd))
        .or(momentumExit)
        .or(sellingRule)
        // Protect against severe losses
        .or(new StopLossRule(closePrice, Decimal.valueOf
            (STOP_LOSS_THRESHOLD)))
        // Take profits and run
        .or(new StopGainRule(closePrice, Decimal.valueOf
            (STOP_GAIN_THRESHOLD)));

    BaseStrategy strategy = new BaseStrategy(entryRule, exitRule);
    strategy.setUnstablePeriod(UNSTABLE_PERIOD);
    return strategy;
  }
}
