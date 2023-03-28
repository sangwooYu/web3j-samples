package com.ethjava;

import org.web3j.crypto.Hash;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.utils.Numeric;

import java.util.Arrays;

public class Calculate {
	public static void main(String[] args) {
		System.out.println(calculateContractAddress("0x6c0f49aF552F2326DD851b68832730CB7b6C0DaF".toLowerCase(), 294));

		String signedData = "0xf8ac8201518506fc23ac00830493e094fda023cea60a9f421d74ac49f9a015880a77dd7280b844a9059cbb000000000000000000000000b5dbd2e4093a501f1d1e645f04cef5815a1581d7000000000000000000000000000000000000000000000004c53ecdc18a6000001ca03d710f3c5aabde2733938c44c0b1448f96e760c030205562f59889557397faa4a007110abbcfa343381a2f713d6339d3fa751200f82cc2f06a4d1967b4eaf61d50";
		System.out.println(caculateTransactionHash(signedData));
	}

	/**
	 * 릴리스 전에 컨트랙트 주소 계산
	 */
	private static String calculateContractAddress(String address, long nonce) {
		// address
		byte[] addressAsBytes = Numeric.hexStringToByteArray(address);

		// String 형태의 address를 hexAddress로 변경
		// hexaddress 형태의 address를 직렬화 시키고 해쉬로 감싼게 연산된 컨트랙트 주소
		byte[] calculatedAddressAsBytes =
				Hash.sha3(RlpEncoder.encode(
						new RlpList(
								RlpString.create(addressAsBytes),
								RlpString.create((nonce)))));

		calculatedAddressAsBytes = Arrays.copyOfRange(calculatedAddressAsBytes,
				12, calculatedAddressAsBytes.length);
		String calculatedAddressAsHex = Numeric.toHexString(calculatedAddressAsBytes);
		// Hex형태의 Address를 리턴
		return calculatedAddressAsHex;
	}

	/**
	 * 제출하기 전에 트랜잭션 해시 계산
	 */
	private static String caculateTransactionHash(String signedData) {
		// signedData를 바탕으로 TransactionHash를 리턴
		String txHash = Hash.sha3(signedData);
		return txHash;
	}
}
