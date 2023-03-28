package com.ethjava;

import com.ethjava.utils.Environment;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.admin.methods.response.PersonalListAccounts;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * 계정 관리 관련
 */
public class AccountManager {
	private static Admin admin;

	public static void main(String[] args) {
		admin = Admin.build(new HttpService(Environment.RPC_URL));
		createNewAccount();
		getAccountList();
		unlockAccount();

//		admin.personalSendTransaction();
// 위 메소드는 web3j.sendTransaction과 동일하므로 여기에 예제를 작성하지 마십시오.
	}

	private static void createNewAccount() {
		String password = "123456789";
		try {
			// password 값 만을 이용하여, 새로운 account고유생성자를 만듭니다.
			NewAccountIdentifier newAccountIdentifier = admin.personalNewAccount(password).send();
			String address = newAccountIdentifier.getAccountId();
			// 고유생성자 객체에서 getAccountId() 를 사용하면 지갑주소를 얻을 수 있습니다.
			System.out.println("new account address " + address);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void getAccountList() {
		try {
			// admin 객체에서 계정들을 가져옵니다.
			PersonalListAccounts personalListAccounts = admin.personalListAccounts().send();
			List<String> addressList;
			addressList = personalListAccounts.getAccountIds();
			System.out.println("account size " + addressList.size());
			for (String address : addressList) {
				System.out.println(address);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 账号解锁
	 */
	private static void unlockAccount() {
		String address = "0x05f50cd5a97d9b3fec35df3d0c6c8234e6793bdf";
		String password = "123456789";
		//账号解锁持续时间 单位秒 缺省值300秒
		BigInteger unlockDuration = BigInteger.valueOf(60L);
		try {
			PersonalUnlockAccount personalUnlockAccount = admin.personalUnlockAccount(address, password, unlockDuration).send();
			Boolean isUnlocked = personalUnlockAccount.accountUnlocked();
			System.out.println("account unlock " + isUnlocked);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
