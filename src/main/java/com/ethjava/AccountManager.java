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
				// 가져온 지갑 주소들을 반복문 돌려가며 출력합니다.
				System.out.println(address);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 계정 잠금 해제
	 */
	private static void unlockAccount() {
		String address = "0x05f50cd5a97d9b3fec35df3d0c6c8234e6793bdf";
		String password = "123456789";
		// 계정 잠금 해제 기간, 단위 초, 기본값은 300초
		// 기본적으로 Geth의 계정은 "잠겨" 있으며, 이는 해당 계정에서 트랜잭션을 보낼 수 없음을 의미합니다. Geth를 통해 직접 또는 RPC를 통해 거래를 보내려면 계정을 잠금 해제해야 합니다(web3는 이를 지원하지 않음). 계정 잠금을 해제하려면 계정과 연결된 개인 키를 해독하는 데 사용되는 암호를 제공해야 거래에 서명할 수 있습니다.
		// https://ethereum.stackexchange.com/questions/4157/how-to-unlock-the-account-with-geth
		// 사고를 방지하기 위해서(?) 각각의 주소는 통상적으로 열려있지 않은게 바람직
		BigInteger unlockDuration = BigInteger.valueOf(60L);
		try {
			// 주소값과 비밀번호, 언락할 시간을 받아 account를 언락처리합니다.
			PersonalUnlockAccount personalUnlockAccount = admin.personalUnlockAccount(address, password, unlockDuration).send();
			Boolean isUnlocked = personalUnlockAccount.accountUnlocked();
			System.out.println("account unlock " + isUnlocked);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
