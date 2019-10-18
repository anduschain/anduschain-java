//Main.java

import io.anduschain.javasdk.AnduschainDefaultGasProvider;
import io.anduschain.javasdk.AnduschainRawTransaction;
import io.anduschain.javasdk.AnduschainRawTransactionManager;
import io.anduschain.javasdk.AnduschainTransactionEncoder;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpList;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.ExecutionException;

public class Test {

    static Web3j web3;

    //credentials
    String keyStorePath = "/home/jhp/projects/anduschain-java/target/test-classes/UTC--2019-10-17T01-50-41.737000000Z--a2ae980a5b4e1cd07b36803e4d978dbd65da3f34.json";
    String passWord = "1111";

    // address
    String myAddress = "0xa2ae980a5b4e1cd07b36803e4d978dbd65da3f34";
    String targetAddress = "0xfbeef2d370a59ac46aa142d47f7fb6dcd9389f44";
    String ganache = "0xdC98D7489680413b4C664b010Cc6499775BFCC4c";

    public static void main(String[] args) {
        web3 = Web3j.build(new HttpService("HTTP://127.0.0.1:8545"));
//        Web3j web3 = Web3j.build(new HttpService("HTTP://127.0.0.1:7545"));  //for ganache

        try {
            Web3ClientVersion clientVersion = web3.web3ClientVersion().send();
            EthBlockNumber blockNumber = web3.ethBlockNumber().send();
//            EthGasPrice gasPrice = web3.ethGasPrice().send();
//            EthBlock latest = web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf("latest"), true).send();

            // Print result
            System.out.println("Client version: " + clientVersion.getWeb3ClientVersion());
            System.out.println("current Block number: " + blockNumber.getBlockNumber());
//            System.out.println("Gas price: " + gasPrice.getGasPrice());
//            System.out.println("latest block hash : " + latest.getBlock().getHash());
//            new Test().make_wallet();
            new Test().transferOneEther();
            new Test().test_contract();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void make_wallet() throws CipherException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        String walletFileName = WalletUtils.generateFullNewWalletFile(passWord, new File("path/to/file"));
        String[] fetchAddress = walletFileName.split("--");
        String getAddress = fetchAddress[fetchAddress.length - 1].split("\\.")[0];
        System.out.println("addr : " + getAddress);
    }

    public void transferOneEther() throws IOException, CipherException, ExecutionException, InterruptedException {
        EthSendTransaction ethCall = null;

        EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(
                myAddress, DefaultBlockParameterName.LATEST).sendAsync().get();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();
//        System.out.println("nonce : " + nonce);

        EthGetBalance ethGetBalance = web3.ethGetBalance(myAddress, DefaultBlockParameter.valueOf("latest")).send();
        System.out.println("daon : " + ethGetBalance.getBalance());

//        BigInteger blockGasLimit = web3.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().getBlock().getGasLimit();
//        System.out.println("blockgaslimit : " + blockGasLimit);

        AnduschainRawTransaction rtm = AnduschainRawTransaction.createEtherTransaction(
                new BigInteger("0"),
                nonce,
                new BigInteger("23809523805524"),
                new BigInteger("21000"),
                targetAddress,
                Convert.toWei("0.0000001", Convert.Unit.ETHER).toBigInteger()
        );

        Credentials credentials = WalletUtils.loadCredentials(
                passWord,
                keyStorePath);

        byte[] signedMessage = AnduschainTransactionEncoder.signMessage(rtm, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        ethCall = web3.ethSendRawTransaction(hexValue).sendAsync().get();

        RlpList rl = RlpDecoder.decode(signedMessage);

        if (ethCall.hasError()) {
            System.out.println(ethCall.getError().getMessage());
        } else {
            System.out.println(ethCall.getTransactionHash());
        }
    }


    public void test_contract() throws Exception {
//        EthEstimateGas estimateGas = web3.ethEstimateGas(anduschain.io.javasdk.SimpleStorage.BINARY);
        Credentials credentials = WalletUtils.loadCredentials(
                passWord,
                keyStorePath);

        SimpleStorage ss = SimpleStorage.deploy(web3,
                new AnduschainRawTransactionManager(web3, credentials),
                new AnduschainDefaultGasProvider()).send();

        ss.isValid();
        System.out.println("tx receipt : " + ss.getTransactionReceipt());
    }
}

