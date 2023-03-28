pragma solidity ^0.4.16;

interface tokenRecipient { function receiveApproval(address _from, uint256 _value, address _token, bytes _extraData) public; }

contract TokenERC20 {
    // 토큰의 프로퍼티들
    string public name;
    string public symbol;
    uint8 public decimals = 18;
    // 소수점 이하 18자리가 권장되는 기본값이므로 변경하지 말 것.
    uint256 public totalSupply;

    // 아래 2줄의 코드를 통해 모든 잔액이 있는 배열이 생성됩니다.
    mapping (address => uint256) public balanceOf;
    mapping (address => mapping (address => uint256)) public allowance;

    // 아래 1줄의 코드를 통해 블록체인에서 클라이언트에게 알릴 공개 이벤트가 생성됩니다.
    event Transfer(address indexed from, address indexed to, uint256 value);

    // 소각된 양에 대해 클라이언트에게 알리는 burn 함수
    event Burn(address indexed from, uint256 value);

    // TokenERC20 함수는 계약 생성자에게 초기 공급 토큰으로 계약을 초기화합니다.
    function TokenERC20(
        uint256 initialSupply,
        string tokenName,
        string tokenSymbol
    ) public {
        totalSupply = initialSupply * 10 ** uint256(decimals);  // Update total supply with the decimal amount
        balanceOf[msg.sender] = totalSupply;                // Give the creator all initial tokens
        name = tokenName;                                   // Set the name for display purposes
        symbol = tokenSymbol;                               // Set the symbol for display purposes
    }

    /* 내부 이체, 본 계약으로만 호출 가능 */
    function _transfer(address _from, address _to, uint _value) internal {
        // Prevent transfer to 0x0 address. Use burn() instead
        require(_to != 0x0);
        // Check if the sender has enough
        require(balanceOf[_from] >= _value);
        // Check for overflows
        require(balanceOf[_to] + _value > balanceOf[_to]);
        // Save this for an assertion in the future
        uint previousBalances = balanceOf[_from] + balanceOf[_to];
        // Subtract from the sender
        balanceOf[_from] -= _value;
        // Add the same to the recipient
        balanceOf[_to] += _value;
        Transfer(_from, _to, _value);
        // Asserts are used to use static analysis to find bugs in your code. They should never fail
        assert(balanceOf[_from] + balanceOf[_to] == previousBalances);
    }

    /**
     * 토큰을 보냅니다. (현 address로부터)
     *
     * Send `_value` tokens to `_to` from your account
     *
     * @param _to 는 수신자의 주소입니다.
     * @param _value 는 보낼 금액입니다.
     */
    function transfer(address _to, uint256 _value) public {
        _transfer(msg.sender, _to, _value);
    }

    /**
     * 토큰을 보냅니다. (다른 address로부터)
     *
     * @param _from 은 송신자의 주소입니다.
     * @param _to 는 수신자의 주소입니다.
     * @param _value 는 보낼 금액입니다.
     */
    function transferFrom(address _from, address _to, uint256 _value) public returns (bool success) {
        require(_value <= allowance[_from][msg.sender]);     // Check allowance
        allowance[_from][msg.sender] -= _value;
        _transfer(_from, _to, _value);
        return true;
    }

    /**
     * 다른 주소의 allowance 를 설정
     *
     * `_spender`가 귀하를 대신하여 `_value` 토큰 이상을 사용하지 않도록 허용합니다.
     *
     * @param _spender 지출 승인 주소
     * @param _value 지출할 수 있는 최대 금액
     */
    function approve(address _spender, uint256 _value) public
        returns (bool success) {
        allowance[msg.sender][_spender] = _value;
        return true;
    }

    /**
     * 다른 주소에 allowance 설정 및 알림
     *
     * `_spender`가 귀하를 대신하여 `_value` 토큰 이상을 사용하지 않도록 허용하고 이에 대한 계약을 알립니다.
     *
     * @param _spender 지출 승인된 주소
     * @param _value 지출할 수 있는 최대 금액
     * @param _extraData 승인된 계약서에 보낼 추가 정보
     */
    function approveAndCall(address _spender, uint256 _value, bytes _extraData)
        public
        returns (bool success) {
        tokenRecipient spender = tokenRecipient(_spender);
        if (approve(_spender, _value)) {
            spender.receiveApproval(msg.sender, _value, this, _extraData);
            return true;
        }
    }

    /**
     * 토큰을 파괴합니다 (태웁니다)
     *
     * 되돌릴 수 없도록 시스템에서 `_value` 만큼의 토큰을 제거합니다.
     *
     * @param _소각할 돈의 가치
     */
    function burn(uint256 _value) public returns (bool success) {
        require(balanceOf[msg.sender] >= _value);   // Check if the sender has enough
        balanceOf[msg.sender] -= _value;            // Subtract from the sender
        totalSupply -= _value;                      // Updates totalSupply
        Burn(msg.sender, _value);
        return true;
    }

    /**
     * 다른 계정의 토큰 파기
     *
     * `_from`을 대신하여 되돌릴 수 없도록 시스템에서 `_value` 토큰을 제거합니다.
     *
     * @param _from 발신자 주소
     * @param _value 소각 금액
     */
    function burnFrom(address _from, uint256 _value) public returns (bool success) {
        require(balanceOf[_from] >= _value);                // Check if the targeted balance is enough
        require(_value <= allowance[_from][msg.sender]);    // Check allowance
        balanceOf[_from] -= _value;                         // Subtract from the targeted balance
        allowance[_from][msg.sender] -= _value;             // Subtract from the sender's allowance
        totalSupply -= _value;                              // Update totalSupply
        Burn(_from, _value);
        return true;
    } //
}
