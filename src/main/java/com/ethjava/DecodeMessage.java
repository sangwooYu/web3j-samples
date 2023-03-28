package com.ethjava;

import org.web3j.crypto.*;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.List;

public class DecodeMessage {

	/**
	 * 서명 후 암호화된 문자열을 얻습니다.
	 * 이 클래스는 이 문자열을 구문 분석합니다.
	 */
	public static void main(String[] args) {
		String signedData = "0xf8ac8201518506fc23ac00830493e094fda023cea60a9f421d74ac49f9a015880a77dd7280b844a9059cbb000000000000000000000000b5dbd2e4093a501f1d1e645f04cef5815a1581d7000000000000000000000000000000000000000000000004c53ecdc18a6000001ca03d710f3c5aabde2733938c44c0b1448f96e760c030205562f59889557397faa4a007110abbcfa343381a2f713d6339d3fa751200f82cc2f06a4d1967b4eaf61d50";
		decodeMessage(signedData);
		decodeMessageV340(signedData);
	}

	private static void decodeMessage(String signedData) {
		//样例 https://ropsten.etherscan.io/tx/0xfd8acd10d72127f29f0a01d8bcaf0165665b5598781fe01ca4bceaa6ab9f2cb0
		try {
			System.out.println(signedData);
			System.out.println("해독 start " + System.currentTimeMillis());
			RlpList rlpList = RlpDecoder.decode(Numeric.hexStringToByteArray(signedData));
			List<RlpType> values = ((RlpList) rlpList.getValues().get(0)).getValues();
			BigInteger nonce = Numeric.toBigInt(((RlpString) values.get(0)).getBytes());
			BigInteger gasPrice = Numeric.toBigInt(((RlpString) values.get(1)).getBytes());
			BigInteger gasLimit = Numeric.toBigInt(((RlpString) values.get(2)).getBytes());
			String to = Numeric.toHexString(((RlpString) values.get(3)).getBytes());
			BigInteger value = Numeric.toBigInt(((RlpString) values.get(4)).getBytes());
			String data = Numeric.toHexString(((RlpString) values.get(5)).getBytes());
			RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, value, data);
			RlpString v = (RlpString) values.get(6);
			RlpString r = (RlpString) values.get(7);
			RlpString s = (RlpString) values.get(8);
			Sign.SignatureData signatureData = new Sign.SignatureData(
					v.getBytes()[0],
					Numeric.toBytesPadded(Numeric.toBigInt(r.getBytes()), 32),
					Numeric.toBytesPadded(Numeric.toBigInt(s.getBytes()), 32));
			BigInteger pubKey = Sign.signedMessageToKey(TransactionEncoder.encode(rawTransaction), signatureData);
			System.out.println("publicKey " + pubKey.toString(16));
			String address = Numeric.prependHexPrefix(Keys.getAddress(pubKey));
			System.out.println("address " + address);
			System.out.println("해독 end " + System.currentTimeMillis());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 거래 데이터 자체가 암호화되지 않고 직접적으로 얻을 수 있음을 알 수 있습니다.
	 * v rs는 개인키로 암호화된 데이터이며, v rs에 트랜잭션 데이터를 추가하면 개인키에 해당하는 공개키와 주소를 얻을 수 있다.
	 * 따라서 RawTransaction에는 fromAddress 매개변수가 없습니다.
	 * 복호화된 주소는 트랜잭션을 보낸 주소입니다. 이로써 확인이 완료됩니다.
	 */
	private static void decodeMessageV340(String signedData) {
		System.out.println("해독 start " + System.currentTimeMillis());
		RawTransaction rawTransaction = TransactionDecoder.decode(signedData);
		if (rawTransaction instanceof SignedRawTransaction) {
			try {
				String from = ((SignedRawTransaction) rawTransaction).getFrom();
				System.out.println("address " + from);
			} catch (SignatureException e) {
				e.printStackTrace();
			}
		}
		System.out.println("해독 end " + System.currentTimeMillis());
	}
}
