package com.ethjava;

import com.ethjava.utils.Environment;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;

public class EthInfo {
	private static Web3j web3j;

	public static void main(String[] args) {
		web3j = Web3j.build(new HttpService(Environment.RPC_URL));
		getEthInfo();
	}

	/**
	 * 블록체인 정보 요청
	 */
	private static void getEthInfo() {

		Web3ClientVersion web3ClientVersion = null;
		try {
			//클라이언트 버전
			web3ClientVersion = web3j.web3ClientVersion().send();
			String clientVersion = web3ClientVersion.getWeb3ClientVersion();
			System.out.println("clientVersion " + clientVersion);
			//블록 넘버
			EthBlockNumber ethBlockNumber = web3j.ethBlockNumber().send();
			BigInteger blockNumber = ethBlockNumber.getBlockNumber();
			System.out.println(blockNumber);
			//마이닝 보상 계정
			EthCoinbase ethCoinbase = web3j.ethCoinbase().send();
			String coinbaseAddress = ethCoinbase.getAddress();
			System.out.println(coinbaseAddress);
			//블록 동기화 여부
			EthSyncing ethSyncing = web3j.ethSyncing().send();
			boolean isSyncing = ethSyncing.isSyncing();
			System.out.println(isSyncing);
			//마이닝 여부
			EthMining ethMining = web3j.ethMining().send();
			boolean isMining = ethMining.isMining();
			System.out.println(isMining);
			//현재 가스 가격
			EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
			BigInteger gasPrice = ethGasPrice.getGasPrice();
			System.out.println(gasPrice);
			//채굴 속도
			EthHashrate ethHashrate = web3j.ethHashrate().send();
			BigInteger hashRate = ethHashrate.getHashrate();
			System.out.println(hashRate);
			//프로토콜 버전
			EthProtocolVersion ethProtocolVersion = web3j.ethProtocolVersion().send();
			String protocolVersion = ethProtocolVersion.getProtocolVersion();
			System.out.println(protocolVersion);
			//연결된 노드 (피어) 수
			NetPeerCount netPeerCount = web3j.netPeerCount().send();
			BigInteger peerCount = netPeerCount.getQuantity();
			System.out.println(peerCount);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
