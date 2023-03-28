package com.ethjava;

import com.ethjava.utils.Environment;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.http.HttpService;

import java.util.Arrays;
import java.util.List;

/**
 * 이벤트 로그 관련
 * ContractEvent 모니터링
 */
public class ContractEvent {
	private static String contractAddress = "0x4c1ae77bc2df45fb68b13fa1b4f000305209b0cb";
	private static Web3j web3j;

	public static void main(String[] args) {
		web3j = Web3j.build(new HttpService(Environment.RPC_URL));
		/**
		 * ERC20 토큰 트랜잭션 모니터링
		 */
		// contractAddress와 BlockParameter 기반 필터 생성
		EthFilter filter = new EthFilter(
				DefaultBlockParameterName.EARLIEST,
				DefaultBlockParameterName.LATEST,
				contractAddress);
		// Transfer 이벤트 객체 생성
		Event event = new Event("Transfer",
				Arrays.<TypeReference<?>>asList(
						new TypeReference<Address>(true) {
						},
						new TypeReference<Address>(true) {
						}, new TypeReference<Uint256>(false) {
						}
				)
		);
		// 이벤트 객체를 인코딩하여 topic Data로 변경
		String topicData = EventEncoder.encode(event);
		filter.addSingleTopic(topicData);
		System.out.println(topicData);

		// filter subscribe
		web3j.ethLogObservable(filter).subscribe(log -> {
			System.out.println(log.getBlockNumber());
			System.out.println(log.getTransactionHash());
			List<String> topics = log.getTopics();
			for (String topic : topics) {
				System.out.println(topic);
			}
		});
	}
}
