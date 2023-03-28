package com.ethjava;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bitcoinj.crypto.*;
import org.bitcoinj.wallet.DeterministicSeed;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 이더리움 니모닉 (지갑을 복구하기 위한 12개의 단어)
 * bitcoin org.bitcoinj의 jar 패키지 사용
 */
public class EthMnemonic {
	/**
	 * bip44 프로토콜 기반의 일반 이더리움 니모닉 경로(imtoken jaxx Metamask myetherwallet)
	 */
	private static String ETH_TYPE = "m/44'/60'/0'/0/0";

	private static SecureRandom secureRandom = new SecureRandom();

	public static void main(String[] args) {
		//니모닉 생성
		generateMnemonic(ETH_TYPE, "11111111");

		//니모닉 가져오기
		//[team, bid, property, oval, hedgehog, observe, badge, cabin, color, cruel, casino, blame]
		List<String> list = new ArrayList<>();
		list.add("team");
		list.add("bid");
		list.add("property");
		list.add("oval");
		list.add("hedgehog");
		list.add("observe");
		list.add("badge");
		list.add("cabin");
		list.add("color");
		list.add("cruel");
		list.add("casino");
		list.add("blame");
//		importMnemonic(ETH_TYPE, list, "11111111");
	}

	public static EthHDWallet generateMnemonic(String path, String password) {
		if (!path.startsWith("m") && !path.startsWith("M")) {
			//매개변수가 잘못되었습니다.
			return null;
		}
		String[] pathArray = path.split("/");
		if (pathArray.length <= 1) {
			// 내용이 잘못되었습니다.
			return null;
		}

		if (password.length() < 8) {
			//비밀번호가 너무 짧습니다
			return null;
		}

		String passphrase = "";
		long creationTimeSeconds = System.currentTimeMillis() / 1000;
		DeterministicSeed ds = new DeterministicSeed(secureRandom, 128, passphrase, creationTimeSeconds);
		return createEthWallet(ds, pathArray, password);
	}

	private static EthHDWallet importMnemonic(String path, List<String> list, String password) {
		if (!path.startsWith("m") && !path.startsWith("M")) {
			//매개변수가 잘못되었습니다.
			return null;
		}
		String[] pathArray = path.split("/");
		if (pathArray.length <= 1) {
			// 내용이 잘못되었습니다.
			return null;
		}
		if (password.length() < 8) {
			//비밀번호가 너무 짧습니다
			return null;
		}
		String passphrase = "";
		long creationTimeSeconds = System.currentTimeMillis() / 1000;
		DeterministicSeed ds = new DeterministicSeed(list, null, passphrase, creationTimeSeconds);

		return createEthWallet(ds, pathArray, password);
	}

	private static EthHDWallet createEthWallet(DeterministicSeed ds, String[] pathArray, String password) {
		//아래는 니모닉을 이용해 이더리움 지갑 생성 시 거치게 되는 과정들입니다.
		//루트 개인 키
		byte[] seedBytes = ds.getSeedBytes();
		System.out.println("루트 개인 키" + Arrays.toString(seedBytes));
		//니모닉
		List<String> mnemonic = ds.getMnemonicCode();
		System.out.println("니모닉 " + Arrays.toString(mnemonic.toArray()));

		try {
			//니모닉 시드
			byte[] mnemonicSeedBytes = MnemonicCode.INSTANCE.toEntropy(mnemonic);
			System.out.println("니모닉 시드" + Arrays.toString(mnemonicSeedBytes));
			ECKeyPair mnemonicKeyPair = ECKeyPair.create(mnemonicSeedBytes);
			WalletFile walletFile = Wallet.createLight(password, mnemonicKeyPair);
			ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
			//이 키 저장소를 저장하고 사용 후 삭제합니다.
			String jsonStr = objectMapper.writeValueAsString(walletFile);
			System.out.println("mnemonic keystore " + jsonStr);
			//확인하다
			WalletFile checkWalletFile = objectMapper.readValue(jsonStr, WalletFile.class);
			ECKeyPair ecKeyPair = Wallet.decrypt(password, checkWalletFile);
			byte[] checkMnemonicSeedBytes = Numeric.hexStringToByteArray(ecKeyPair.getPrivateKey().toString(16));
			System.out.println("니모닉 시드 확인"
					+ Arrays.toString(checkMnemonicSeedBytes));
			List<String> checkMnemonic = MnemonicCode.INSTANCE.toMnemonic(checkMnemonicSeedBytes);
			System.out.println("니모닉 확인" + Arrays.toString(checkMnemonic.toArray()));

		} catch (MnemonicException.MnemonicLengthException | MnemonicException.MnemonicWordException | MnemonicException.MnemonicChecksumException | CipherException | IOException e) {
			e.printStackTrace();
		}

		if (seedBytes == null)
			return null;
		DeterministicKey dkKey = HDKeyDerivation.createMasterPrivateKey(seedBytes);
		for (int i = 1; i < pathArray.length; i++) {
			ChildNumber childNumber;
			if (pathArray[i].endsWith("'")) {
				int number = Integer.parseInt(pathArray[i].substring(0,
						pathArray[i].length() - 1));
				childNumber = new ChildNumber(number, true);
			} else {
				int number = Integer.parseInt(pathArray[i]);
				childNumber = new ChildNumber(number, false);
			}
			dkKey = HDKeyDerivation.deriveChildKey(dkKey, childNumber);
		}
		System.out.println("path " + dkKey.getPathAsString());

		ECKeyPair keyPair = ECKeyPair.create(dkKey.getPrivKeyBytes());
		System.out.println("eth privateKey " + keyPair.getPrivateKey().toString(16));
		System.out.println("eth publicKey " + keyPair.getPublicKey().toString(16));

		EthHDWallet ethHDWallet = null;
		try {
			WalletFile walletFile = Wallet.createLight(password, keyPair);
			System.out.println("eth address " + "0x" + walletFile.getAddress());
			ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
			//存
			String jsonStr = objectMapper.writeValueAsString(walletFile);
			System.out.println("eth keystore " + jsonStr);

			ethHDWallet = new EthHDWallet(keyPair.getPrivateKey().toString(16),
					keyPair.getPublicKey().toString(16),
					mnemonic, dkKey.getPathAsString(),
					"0x" + walletFile.getAddress(), jsonStr);
		} catch (CipherException | JsonProcessingException e) {
			e.printStackTrace();
		}

		return ethHDWallet;
	}

	public static class EthHDWallet {
		String privateKey;
		String publicKey;
		List<String> mnemonic;
		String mnemonicPath;
		String Address;
		String keystore;

		public EthHDWallet(String privateKey, String publicKey, List<String> mnemonic, String mnemonicPath, String address, String keystore) {
			this.privateKey = privateKey;
			this.publicKey = publicKey;
			this.mnemonic = mnemonic;
			this.mnemonicPath = mnemonicPath;
			this.Address = address;
			this.keystore = keystore;
		}
	}

}
