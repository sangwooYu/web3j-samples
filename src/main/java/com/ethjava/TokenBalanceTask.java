package com.ethjava;

import com.ethjava.utils.Environment;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 일괄 쿼리 토큰 잔액
 */
public class TokenBalanceTask {

	public class Token {
		public String contractAddress;
		public int decimals;
		public String name;

		public Token(String contractAddress) {
			this.contractAddress = contractAddress;
			this.decimals = 0;
		}

		public Token(String contractAddress, int decimals) {
			this.contractAddress = contractAddress;
			this.decimals = decimals;
		}
	}

	private static Web3j web3j;

	//조회할 토큰 컨트랙트 주소
	private static List<Token> tokenList;

	//조회할 지갑 주소
	private static List<String> addressList;

	public static void main(String[] args) {
		web3j = Web3j.build(new HttpService(Environment.RPC_URL));
		loadData();
		//소수점이 없으면 요청해야 합니다. (소수점 자리수가 정확하지 않을 때 그대로 진행하면 잘못된 Balance로 파악할 수 있으므로)
		requestDecimals();
		requestName();
		processTask();
	}


	private static void loadData() {
		tokenList = new ArrayList<>();
		// TODO: 2018/3/14 add...
		addressList = new ArrayList<>();
		// TODO: 2018/3/14 add...
	}

	private static void requestDecimals() {
		for (Token token : tokenList) {
			token.decimals = TokenClient.getTokenDecimals(web3j, token.contractAddress);
		}
	}

	private static void requestName() {
		for (Token token : tokenList) {
			token.name = TokenClient.getTokenName(web3j, token.contractAddress);
		}
	}

	private static void processTask() {
		for (String address : addressList) {
			for (Token token : tokenList) {
				BigDecimal balance = new BigDecimal(TokenClient.getTokenBalance(web3j, address, token.contractAddress));
				balance.divide(BigDecimal.TEN.pow(token.decimals));
				System.out.println("address " + address + " name " + token.name + " balance " + balance);
			}
		}
	}
}
