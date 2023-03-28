package com.ethjava;

import com.ethjava.utils.Environment;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;

public class Security {
	private static Web3j web3j;

	public static void main(String[] args) {
		web3j = Web3j.build(new HttpService(Environment.RPC_URL));

		exportPrivateKey("/Users/yangzhengwei/Library/Ethereum/testnet/keystore/UTC--2018-03-03T03-51-50.155565446Z--7b1cc408fcb2de1d510c1bf46a329e9027db4112",
				"yzw");

		importPrivateKey(new BigInteger("", 16),
				"yzw",
				WalletUtils.getTestnetKeyDirectory());

		exportBip39Wallet(WalletUtils.getTestnetKeyDirectory(),
				"yzw");
	}

	/**
	 * 개인 키 내보내기
	 *
	 * @param keystorePath 계정 키 저장소 경로
	 * @param password     비밀번호
	 */
	private static void exportPrivateKey(String keystorePath, String password) {
		try {
			Credentials credentials = WalletUtils.loadCredentials(
					password,
					keystorePath);
			BigInteger privateKey = credentials.getEcKeyPair().getPrivateKey();
			System.out.println(privateKey.toString(16));
		} catch (IOException | CipherException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 개인 키 가져오기
	 *
	 * @param privateKey 개인 키
	 * @param password   비밀번호
	 * @param directory  저장 경로. 기본 테스트 네트워크: WalletUtils.getTestnetKeyDirectory(), 기본 네트워크: WalletUtils.getMainnetKeyDirectory()
	 */
	private static void importPrivateKey(BigInteger privateKey, String password, String directory) {
		ECKeyPair ecKeyPair = ECKeyPair.create(privateKey);
		try {
			String keystoreName = WalletUtils.generateWalletFile(password,
					ecKeyPair,
					new File(directory),
					true);
			System.out.println("keystore name " + keystoreName);
		} catch (CipherException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 니모닉으로 계정 생성
	 *
	 * @param keystorePath
	 * @param password
	 */
	private static void exportBip39Wallet(String keystorePath, String password) {
		try {
			// TODO: 2018년 3월 14일에 예외가 발생합니다. 답변을 위해 공식에 문제가 제출되었습니다.
			Bip39Wallet bip39Wallet = WalletUtils.generateBip39Wallet(password, new File(keystorePath));
			System.out.println(bip39Wallet);
		} catch (CipherException | IOException e) {
			e.printStackTrace();
		}
	}

}
