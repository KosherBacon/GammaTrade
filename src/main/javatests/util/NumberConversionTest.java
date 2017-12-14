package jk.gy.util;

import gy.jk.proto.Shared.BDecimal;
import gy.jk.proto.Shared.BInteger;
import gy.jk.util.NumberConversion;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.BigInteger;

@RunWith(MockitoJUnitRunner.class)
public class NumberConversionTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testBigIntegerConversion() throws Exception {
    BigInteger testNum = new BigInteger("4");
    BInteger result = NumberConversion.BigIntegerToProto(testNum);
    assertThat(NumberConversion.ProtoToBigInteger(result)).isEqualTo(testNum);
  }

  @Test
  public void testBigDecimalConversion() throws Exception {
    BigDecimal testNum = new BigDecimal("1.5");
    BDecimal result = NumberConversion.BigDecimalToProto(testNum);
    assertThat(NumberConversion.ProtoToBigDecimal(result)).isEqualTo(testNum);
  }
}
