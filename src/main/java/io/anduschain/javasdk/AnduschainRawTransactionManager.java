package io.anduschain.javasdk;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.exceptions.TxHashMismatchException;
import org.web3j.tx.response.TransactionReceiptProcessor;
import org.web3j.utils.Numeric;
import org.web3j.utils.TxHashVerifier;

import java.io.IOException;
import java.math.BigInteger;

public class AnduschainRawTransactionManager extends TransactionManager {
    private final Web3j web3j;
    final Credentials credentials;
    private final long chainId;
    protected TxHashVerifier txHashVerifier;

    public AnduschainRawTransactionManager(Web3j web3j, Credentials credentials, long chainId) {
        super(web3j, credentials.getAddress());
        this.txHashVerifier = new TxHashVerifier();
        this.web3j = web3j;
        this.credentials = credentials;
        this.chainId = chainId;
    }

    public AnduschainRawTransactionManager(Web3j web3j, Credentials credentials, long chainId, TransactionReceiptProcessor transactionReceiptProcessor) {
        super(transactionReceiptProcessor, credentials.getAddress());
        this.txHashVerifier = new TxHashVerifier();
        this.web3j = web3j;
        this.credentials = credentials;
        this.chainId = chainId;
    }

    public AnduschainRawTransactionManager(Web3j web3j, Credentials credentials, long chainId, int attempts, long sleepDuration) {
        super(web3j, attempts, sleepDuration, credentials.getAddress());
        this.txHashVerifier = new TxHashVerifier();
        this.web3j = web3j;
        this.credentials = credentials;
        this.chainId = chainId;
    }

    public AnduschainRawTransactionManager(Web3j web3j, Credentials credentials) {
        this(web3j, credentials, -1L);
    }

    public AnduschainRawTransactionManager(Web3j web3j, Credentials credentials, int attempts, int sleepDuration) {
        this(web3j, credentials, -1L, attempts, (long)sleepDuration);
    }

    protected BigInteger getNonce() throws IOException {
        EthGetTransactionCount ethGetTransactionCount = (EthGetTransactionCount)this.web3j.ethGetTransactionCount(this.credentials.getAddress(), DefaultBlockParameterName.PENDING).send();
        return ethGetTransactionCount.getTransactionCount();
    }

    public TxHashVerifier getTxHashVerifier() {
        return this.txHashVerifier;
    }

    public void setTxHashVerifier(TxHashVerifier txHashVerifier) {
        this.txHashVerifier = txHashVerifier;
    }

    public EthSendTransaction sendTransaction(BigInteger gasPrice, BigInteger gasLimit, String to, String data, BigInteger value, boolean constructor) throws IOException {
        return this.sendTransaction(new BigInteger("0"), gasPrice, gasLimit, to, data, value, constructor);
    }

    public EthSendTransaction sendTransaction(BigInteger type, BigInteger gasPrice, BigInteger gasLimit, String to, String data, BigInteger value, boolean constructor) throws IOException {
        BigInteger nonce = this.getNonce();
        AnduschainRawTransaction rawTransaction = AnduschainRawTransaction.createTransaction(type, nonce, gasPrice, gasLimit, to, value, data);
        return this.signAndSend(rawTransaction);
    }

    public String sendCall(String to, String data, DefaultBlockParameter defaultBlockParameter) throws IOException {
        return ((EthCall)this.web3j.ethCall(Transaction.createEthCallTransaction(this.getFromAddress(), to, data), defaultBlockParameter).send()).getValue();
    }

    public String sign(AnduschainRawTransaction rawTransaction) {
        byte[] signedMessage;
        if (this.chainId > -1L) {
            signedMessage = AnduschainTransactionEncoder.signMessage(rawTransaction, this.chainId, this.credentials);
        } else {
            signedMessage = AnduschainTransactionEncoder.signMessage(rawTransaction, this.credentials);
        }

        return Numeric.toHexString(signedMessage);
    }

    public EthSendTransaction signAndSend(AnduschainRawTransaction rawTransaction) throws IOException {
        String hexValue = this.sign(rawTransaction);
        EthSendTransaction ethSendTransaction = (EthSendTransaction)this.web3j.ethSendRawTransaction(hexValue).send();
        if (ethSendTransaction != null && !ethSendTransaction.hasError()) {
            String txHashLocal = Hash.sha3(hexValue);
            String txHashRemote = ethSendTransaction.getTransactionHash();
            if (!this.txHashVerifier.verify(txHashLocal, txHashRemote)) {
                throw new TxHashMismatchException(txHashLocal, txHashRemote);
            }
        }

        return ethSendTransaction;
    }
}
