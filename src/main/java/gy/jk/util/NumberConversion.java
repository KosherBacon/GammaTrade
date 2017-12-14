package gy.jk.util;

import com.google.protobuf.ByteString;
import gy.jk.proto.Shared.BDecimal;
import gy.jk.proto.Shared.BInteger;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberConversion {

  public static BDecimal BigDecimalToProto(BigDecimal input) {
    return BDecimal.newBuilder()
        .setScale(input.scale())
        .setIntVal(BigIntegerToProto(input.unscaledValue()))
        .build();
  }

  public static BInteger BigIntegerToProto(BigInteger input) {
    return BInteger.newBuilder()
        .setValue(ByteString.copyFrom(input.toByteArray()))
        .build();
  }

  public static BigDecimal ProtoToBigDecimal(BDecimal input) {
    BigInteger value = ProtoToBigInteger(input.getIntVal());
    return new BigDecimal(value, input.getScale());
  }

  public static BigInteger ProtoToBigInteger(BInteger input) {
    return new BigInteger(input.getValue().toByteArray());
  }

}
