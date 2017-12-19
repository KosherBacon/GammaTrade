package gy.jk.strategy;

import com.google.inject.Inject;
import org.ta4j.core.*;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.trading.rules.NotRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

/**
 * Honey Badger strategy ported from:
 * https://www.quantopian.com/posts/need-help-porting-profitable-honey-badger-python-script-to-quantopian
 */
public class HoneyBadgerStrategy implements StrategyBuilder {

  @Inject
  HoneyBadgerStrategy() {
  }

  @Override
  public Strategy buildStrategy(TimeSeries timeSeries) {

    ClosePriceIndicator closePrices = new ClosePriceIndicator(timeSeries);

    // Current SMA's
    SMAIndicator ma2 = new SMAIndicator(closePrices, 2);
    SMAIndicator ma30 = new SMAIndicator(closePrices, 30);
    SMAIndicator ma55 = new SMAIndicator(closePrices, 55);
    SMAIndicator ma60 = new SMAIndicator(closePrices, 60);
    SMAIndicator ma90 = new SMAIndicator(closePrices, 90);
    SMAIndicator ma150 = new SMAIndicator(closePrices, 150);

    // Previous SMA's
    PreviousValueIndicator ma2i = new PreviousValueIndicator(ma2);
    PreviousValueIndicator ma30i = new PreviousValueIndicator(ma30);
    PreviousValueIndicator ma60i = new PreviousValueIndicator(ma60);
    PreviousValueIndicator ma90i = new PreviousValueIndicator(ma90);
    PreviousValueIndicator ma150i = new PreviousValueIndicator(ma150);

    // Two Tick Previous SMA's
    PreviousValueIndicator ma2ii = new PreviousValueIndicator(ma2i);
    PreviousValueIndicator ma30ii = new PreviousValueIndicator(ma30i);
    PreviousValueIndicator ma60ii = new PreviousValueIndicator(ma60i);
    PreviousValueIndicator ma90ii = new PreviousValueIndicator(ma90i);
    PreviousValueIndicator ma150ii = new PreviousValueIndicator(ma150i);

    // Double Previous Two Tick SMA's
    PreviousValueIndicator ma30iii = new PreviousValueIndicator(ma30ii);
    PreviousValueIndicator ma60iii = new PreviousValueIndicator(ma60ii);
    PreviousValueIndicator ma90iii = new PreviousValueIndicator(ma90ii);
    PreviousValueIndicator ma150iii = new PreviousValueIndicator(ma150ii);

    // Expressions
    MultiplierIndicator floor = new MultiplierIndicator(ma55, Decimal.valueOf("0.75"));
    MultiplierIndicator moon = new MultiplierIndicator(ma55, Decimal.valueOf("1.05"));
    SumIndicator resistance = new SumIndicator(ma30,
        new MultiplierIndicator(new DifferenceIndicator(ma30, ma60),
            Decimal.valueOf("2.8")));

    // Previous Expressions
    SumIndicator resistancei = new SumIndicator(ma30i,
        new MultiplierIndicator(new DifferenceIndicator(ma30i, ma60i),
            Decimal.valueOf("2.8")));

    // Double Previous Expressions
    SumIndicator resistanceii = new SumIndicator(ma30ii,
        new MultiplierIndicator(new DifferenceIndicator(ma30ii, ma60ii),
            Decimal.valueOf("2.8")));

    // Multiplied ma's
    MultiplierIndicator ma30i2 = new MultiplierIndicator(ma30i, Decimal.valueOf("1.002"));
    MultiplierIndicator ma60i2 = new MultiplierIndicator(ma60i, Decimal.valueOf("1.002"));
    MultiplierIndicator ma90i2 = new MultiplierIndicator(ma90i, Decimal.valueOf("1.002"));

    // Previous Multiplied ma's
    MultiplierIndicator ma30i2i = new MultiplierIndicator(ma30ii, Decimal.valueOf("1.002"));
    MultiplierIndicator ma60i2i = new MultiplierIndicator(ma60ii, Decimal.valueOf("1.002"));
    MultiplierIndicator ma90i2i = new MultiplierIndicator(ma90ii, Decimal.valueOf("1.002"));

    // Double Previous Multiplied ma's
    MultiplierIndicator ma30i2ii = new MultiplierIndicator(ma30iii, Decimal.valueOf("1.002"));
    MultiplierIndicator ma60i2ii = new MultiplierIndicator(ma60iii, Decimal.valueOf("1.002"));
    MultiplierIndicator ma90i2ii = new MultiplierIndicator(ma90iii, Decimal.valueOf("1.002"));

    // Mode 1
    Rule greenDragon = new OverIndicatorRule(ma30, ma60)
        .and(new OverIndicatorRule(ma60, ma90))
        .and(new OverIndicatorRule(ma90, ma150))
        .and(new OverIndicatorRule(ma2, ma30))
        .and(new OverIndicatorRule(ma30, ma30i2))
        .and(new OverIndicatorRule(ma60, ma60i2))
        .and(new OverIndicatorRule(ma90, ma90i2));

    // Previous Mode 1
    Rule greenDragoni = new OverIndicatorRule(ma30i, ma60i)
        .and(new OverIndicatorRule(ma60i, ma90i))
        .and(new OverIndicatorRule(ma90i, ma150i))
        .and(new OverIndicatorRule(ma2i, ma30i))
        .and(new OverIndicatorRule(ma30i, ma30i2i))
        .and(new OverIndicatorRule(ma60i, ma60i2i))
        .and(new OverIndicatorRule(ma90i, ma90i2i));

    // Double Previous Mode 1
    Rule greenDragonii = new OverIndicatorRule(ma30ii, ma60ii)
        .and(new OverIndicatorRule(ma60ii, ma90ii))
        .and(new OverIndicatorRule(ma90ii, ma150ii))
        .and(new OverIndicatorRule(ma2ii, ma30ii))
        .and(new OverIndicatorRule(ma30ii, ma30i2ii))
        .and(new OverIndicatorRule(ma60ii, ma60i2ii))
        .and(new OverIndicatorRule(ma90ii, ma90i2ii));

    // Mode -1
    Rule ma2OverResistance = greenDragon
        .and(new OverIndicatorRule(ma2, resistance));

    // Previous Mode -1
    Rule ma2OverResistancei = greenDragoni
        .and(new OverIndicatorRule(ma2i, resistancei));

    // Double Previous Mode -1
    Rule ma2OverResistanceii = greenDragonii
        .and(new OverIndicatorRule(ma2ii, resistanceii));

    // Mode 3
    Rule redDragon = new NotRule(greenDragon)
        .and(new NotRule(ma2OverResistance))
        .and(new UnderIndicatorRule(resistance, ma150)
            .and(new UnderIndicatorRule(ma2, ma90))
            .and(new UnderIndicatorRule(ma2, ma150))
            .and(new UnderIndicatorRule(ma150, ma150i))
            .and(new UnderIndicatorRule(resistance, ma90)));

    // Previous Mode 3
    Rule redDragoni = new NotRule(greenDragoni)
        .and(new NotRule(ma2OverResistancei))
        .and(new UnderIndicatorRule(resistancei, ma150i)
            .and(new UnderIndicatorRule(ma2i, ma90i))
            .and(new UnderIndicatorRule(ma2i, ma150i))
            .and(new UnderIndicatorRule(ma150i, ma150ii))
            .and(new UnderIndicatorRule(resistancei, ma90i)));

    // Double Previous Mode 3
    Rule redDragonii = new NotRule(greenDragonii)
        .and(new NotRule(ma2OverResistanceii))
        .and(new UnderIndicatorRule(resistanceii, ma150ii)
            .and(new UnderIndicatorRule(ma2ii, ma90ii))
            .and(new UnderIndicatorRule(ma2ii, ma150ii))
            .and(new UnderIndicatorRule(ma150ii, ma150iii))
            .and(new UnderIndicatorRule(resistanceii, ma90ii)));

    // Mode 2
    Rule capitulation = new NotRule(greenDragon)
        .and(new NotRule(redDragon));

    Rule finalCapitulation = capitulation;
    capitulation = capitulation.and(
        ((Rule) (index, tradingRecord) -> index != 0 && finalCapitulation.isSatisfied(index - 1))
            .or(greenDragoni)
            .or(ma2OverResistancei)
            .or(redDragoni));

    // Previous Mode 2
    Rule capitulationi = new NotRule(greenDragoni)
        .and(new NotRule(redDragoni));

    Rule finalCapitulationi = capitulationi;
    capitulationi = capitulationi.and(
        ((Rule) (index, tradingRecord) -> index != 0 && finalCapitulationi.isSatisfied(index - 1))
            .or(greenDragonii)
            .or(ma2OverResistanceii)
            .or(redDragonii));

    // Mode 4
    Rule catBounce = new NotRule(greenDragon)
        .and(new NotRule(redDragon))
        .and(new NotRule(capitulation));

    // Previous Mode 4
    Rule catBouncei = new NotRule(greenDragoni)
        .and(new NotRule(redDragoni))
        .and(new NotRule(capitulationi));

    // Mode 3 Signal
    Rule mode3Signal1 = redDragon
        .and(new UnderIndicatorRule(ma2, floor));
    Rule mode3SignalN1 = redDragon
        .and((new UnderIndicatorRule(ma2, ma2i)
            .and(new UnderIndicatorRule(ma2, ma30))
            .and(new OverIndicatorRule(ma2i, ma30)))
            .or(new OverIndicatorRule(ma2, moon)));

    Rule mode1Buy = greenDragon;
    Rule mode2Buy = capitulation
        .and(capitulationi)
        .and(new UnderIndicatorRule(ma2, ma90));
    Rule mode3Buy = redDragon
        .and(mode3Signal1);
    Rule mode4Buy = (catBounce.and(new NotRule(catBouncei)))
        .or(catBounce
            .and(catBouncei)
            .and(new UnderIndicatorRule(ma2, ma90))
            .and(new UnderIndicatorRule(ma90, ma60))
            .and(new UnderIndicatorRule(ma90, ma30))
            .and(new OverIndicatorRule(ma90, ma90i)));

    Rule buyingRule = mode1Buy
        .or(mode2Buy)
        .or(mode3Buy)
        .or(mode4Buy);

    Rule modeN1Sell = ma2OverResistance;
    Rule mode2Sell = (capitulation.and(new NotRule(capitulationi)))
        .or(capitulation
            .and(capitulationi)
            .and(new OverIndicatorRule(ma2,
                new MultiplierIndicator(ma90, Decimal.valueOf("1.1"))))
            .and(new OverIndicatorRule(ma2, ma60)));
    Rule mode3Sell = (redDragon.and(new NotRule(redDragoni)))
        .or(redDragon
            .and(redDragoni)
            .and(mode3SignalN1));
    Rule mode4Sell = (catBounce.and(catBouncei))
        .and(new UnderIndicatorRule(ma2,
            new MultiplierIndicator(ma30, Decimal.valueOf("1.05")))
        .and(new OverIndicatorRule(ma2, ma60)
        .and(new OverIndicatorRule(ma90,
            new MultiplierIndicator(ma90i, Decimal.valueOf("1.003"))))));

    Rule sellingRule = modeN1Sell
        .or(mode2Sell)
        .or(mode3Sell)
        .or(mode4Sell);

    return new BaseStrategy(buyingRule, sellingRule);
  }
}
