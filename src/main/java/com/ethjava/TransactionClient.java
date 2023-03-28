package com.ethjava;

import com.ethjava.utils.Environment;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class TransactionClient {
	private static Web3j web3j;
	private static Admin admin;

	private static String fromAddress = "0x7b1cc408fcb2de1d510c1bf46a329e9027db4112";
	private static String toAddress = "0x05f50cd5a97d9b3fec35df3d0c6c8234e6793bdf";
	private static BigDecimal defaultGasPrice = BigDecimal.valueOf(5);

	public static void main(String[] args) {
		web3j = Web3j.build(new HttpService(Environment.RPC_URL));
		admin = Admin.build(new HttpService(Environment.RPC_URL));

		getBalance(fromAddress);
		sendTransaction();
	}

	/**
	 * 균형을 잡아
	 *
	 * @param address 지갑 주소
	 * @return 균형
	 */
	private static BigInteger getBalance(String address) {
		BigInteger balance = null;
		try {
			EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
			balance = ethGetBalance.getBalance();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("address " + address + " balance " + balance + "wei");
		return balance;
	}

	/**
	 * 공통 거래 객체 생성
	 *
	 * @param fromAddress
	 * @param toAddress
	 * @param nonce       거래 번호
	 * @param gasPrice    gas 가격
	 * @param gasLimit    gas 한계
	 * @param value       amount
	 * @return 거래 파트너
	 */
	private static Transaction makeTransaction(String fromAddress, String toAddress,
											   BigInteger nonce, BigInteger gasPrice,
											   BigInteger gasLimit, BigInteger value) {
		Transaction transaction;
		transaction = Transaction.createEtherTransaction(fromAddress, nonce, gasPrice, gasLimit, toAddress, value);
		return transaction;
	}

	/**
	 * 일반 거래의 가스 한도 얻기
	 *
	 * @param transaction 거래 파트너
	 * @return gas 상한
	 */
	private static BigInteger getTransactionGasLimit(Transaction transaction) {
		BigInteger gasLimit = BigInteger.ZERO;
		try {
			EthEstimateGas ethEstimateGas = web3j.ethEstimateGas(transaction).send();
			gasLimit = ethEstimateGas.getAmountUsed();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return gasLimit;
	}

	/**
	 * 계정 트랜잭션 시간 nonce 가져오기
	 *
	 * @param address 지갑 주소
	 * @return nonce
	 */
	private static BigInteger getTransactionNonce(String address) {
		BigInteger nonce = BigInteger.ZERO;
		try {
			EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send();
			nonce = ethGetTransactionCount.getTransactionCount();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return nonce;
	}

	/**
	 * 정상적인 거래 보내기
	 *
	 * @return 트랜잭션 Hash
	 */
	private static String sendTransaction() {
		String password = "yzw";
		BigInteger unlockDuration = BigInteger.valueOf(60L);
		BigDecimal amount = new BigDecimal("0.01");
		String txHash = null;
		try {
			PersonalUnlockAccount personalUnlockAccount = admin.personalUnlockAccount(fromAddress, password, unlockDuration).send();
			if (personalUnlockAccount.accountUnlocked()) {
				BigInteger value = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
				Transaction transaction = makeTransaction(fromAddress, toAddress,
						null, null, null, value);
				//필수는 아니며 기본값을 사용할 수 있습니다.
				BigInteger gasLimit = getTransactionGasLimit(transaction);
				// 필수는 아님, 기본값은 올바른 값이어야합니다.
				BigInteger nonce = getTransactionNonce(fromAddress);
				// 이 값은 대부분의 채굴자가 수용할 수 있는 가스 가격입니다.
				BigInteger gasPrice = Convert.toWei(defaultGasPrice, Convert.Unit.GWEI).toBigInteger();
				transaction = makeTransaction(fromAddress, toAddress,
						nonce, gasPrice,
						gasLimit, value);
				EthSendTransaction ethSendTransaction = web3j.ethSendTransaction(transaction).send();
				txHash = ethSendTransaction.getTransactionHash();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("tx hash " + txHash);
		return txHash;
	}

	//web3j.ethSendRawTransaction()을 사용하여 트랜잭션을 보내려면 개인 키를 사용하여 트랜잭션을 자체 서명해야 합니다
}
