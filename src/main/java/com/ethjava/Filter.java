package com.ethjava;

import com.ethjava.utils.Environment;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.http.HttpService;
import rx.Subscription;

import java.math.BigInteger;

/**
 * 필터 관련
 * 블록 및 트랜잭션 모니터링
 * 모든 리스너는 Web3jRx에 있음
 */
public class Filter {
	private static Web3j web3j;

	public static void main(String[] args) {
		web3j = Web3j.build(new HttpService(Environment.RPC_URL));
		/**
		 * 새로운 블록 모니터링
		 */
		newBlockFilter(web3j);
		/**
		 * 새로운 트랜잭션 모니터링
		 */
		newTransactionFilter(web3j);
		/**
		 * 오래된 블록, 트랜잭션 트래버스
		 */
		replayFilter(web3j);
		/**
		 * 특정 블록부터 최신 블록까지, 트랜잭션
		 */
		catchUpFilter(web3j);

		/**
		 * 듣기 취소
		 */
		//subscription.unsubscribe();
	}

	private static void newBlockFilter(Web3j web3j) {
		Subscription subscription = web3j.
				blockObservable(false).
				subscribe(block -> {
					System.out.println("new block come in");
					System.out.println("block number" + block.getBlock().getNumber());
				});
	}

	private static void newTransactionFilter(Web3j web3j) {
		Subscription subscription = web3j.
				transactionObservable().
				subscribe(transaction -> {
					System.out.println("transaction come in");
					System.out.println("transaction txHash " + transaction.getHash());
				});
	}

	private static void replayFilter(Web3j web3j) {
		BigInteger startBlock = BigInteger.valueOf(2000000);
		BigInteger endBlock = BigInteger.valueOf(2010000);
		/**
		 * 이전 블록 통과
		 */
		Subscription subscription = web3j.
				replayBlocksObservable(
						DefaultBlockParameter.valueOf(startBlock),
						DefaultBlockParameter.valueOf(endBlock),
						false).
				subscribe(ethBlock -> {
					System.out.println("replay block");
					System.out.println(ethBlock.getBlock().getNumber());
				});

		/**
		 * 오래된 트랜잭션 반복
		 */
		Subscription subscription1 = web3j.
				replayTransactionsObservable(
						DefaultBlockParameter.valueOf(startBlock),
						DefaultBlockParameter.valueOf(endBlock)).
				subscribe(transaction -> {
					System.out.println("replay transaction");
					System.out.println("txHash " + transaction.getHash());
				});
	}

	private static void catchUpFilter(Web3j web3j) {
		BigInteger startBlock = BigInteger.valueOf(2000000);

		/**
		 * 이전 블록 탐색 및 새 블록 듣기
		 */
		Subscription subscription = web3j.catchUpToLatestAndSubscribeToNewBlocksObservable(
				DefaultBlockParameter.valueOf(startBlock), false)
				.subscribe(block -> {
					System.out.println("block");
					System.out.println(block.getBlock().getNumber());
				});

		/**
		 * 이전 트랜잭션을 탐색하고 새 트랜잭션을 수신합니다.
		 */
		Subscription subscription2 = web3j.catchUpToLatestAndSubscribeToNewTransactionsObservable(
				DefaultBlockParameter.valueOf(startBlock))
				.subscribe(tx -> {
					System.out.println("transaction");
					System.out.println(tx.getHash());
				});
	}
}
