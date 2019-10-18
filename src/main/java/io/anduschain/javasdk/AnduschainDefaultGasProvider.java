package io.anduschain.javasdk;

import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;

public class AnduschainDefaultGasProvider extends StaticGasProvider {
    public static final BigInteger GAS_LIMIT;
    public static final BigInteger GAS_PRICE;

    public AnduschainDefaultGasProvider() {
        super(GAS_PRICE, GAS_LIMIT);
    }

    static {
//        GAS_LIMIT = Contract.GAS_LIMIT;
//        GAS_PRICE = ManagedTransaction.GAS_PRICE;
        GAS_LIMIT = new BigInteger("210000");
        GAS_PRICE = new BigInteger("23809523805524");
    }
}
