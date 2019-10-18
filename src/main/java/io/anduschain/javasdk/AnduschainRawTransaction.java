package io.anduschain.javasdk;

import org.web3j.crypto.RawTransaction;

import java.math.BigInteger;

public class AnduschainRawTransaction extends RawTransaction {

    private BigInteger type;

    protected AnduschainRawTransaction(BigInteger type, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, BigInteger value, String data) {
        super(nonce, gasPrice, gasLimit, to, value, data);
        this.type = type;
    }

    public static AnduschainRawTransaction createContractTransaction(BigInteger type, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, BigInteger value, String init) {
        return new AnduschainRawTransaction(type, nonce, gasPrice, gasLimit, "", value, init);
    }

    public static AnduschainRawTransaction createEtherTransaction(BigInteger type, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, BigInteger value) {
        return new AnduschainRawTransaction(type, nonce, gasPrice, gasLimit, to, value, "");
    }

    public static AnduschainRawTransaction createTransaction(BigInteger type, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, String data) {
        return createTransaction(type, nonce, gasPrice, gasLimit, to, BigInteger.ZERO, data);
    }

    public static AnduschainRawTransaction createTransaction(BigInteger type, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, BigInteger value, String data) {
        return new AnduschainRawTransaction(type, nonce, gasPrice, gasLimit, to, value, data);
    }

    public BigInteger getType() { return this.type; }

    public BigInteger getNonce() { return super.getNonce(); }

    public BigInteger getGasPrice() {
        return super.getGasPrice();
    }

    public BigInteger getGasLimit() {
        return super.getGasLimit();
    }

    public String getTo() {
        return super.getTo();
    }

    public BigInteger getValue() {
        return super.getValue();
    }

    public String getData() {
        return super.getData();
    }
}
