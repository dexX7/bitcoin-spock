package foundation.omni.test.rpc.misc

import com.google.bitcoin.core.Address
import com.google.bitcoin.core.Sha256Hash
import com.msgilligan.bitcoin.rpc.JsonRPCStatusException
import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import groovy.json.JsonOutput
import spock.lang.Shared

class GetPayloadSpec extends BaseRegTestSpec {

    final static BigDecimal startBTC = 5.0
    final static BigDecimal startMSC = 10.0

    @Shared
    Address fundedAddress

    def setup() {
        fundedAddress = createFundedAddress(startBTC, startMSC)
    }

    def "getpayload_MP: it creates no error for valid input"() {
        def txid = send_MP(fundedAddress, newAddress, CurrencyID.MSC, 0.1)
        generateBlock()

        when:
        def response = client.send('getpayload_MP', [txid.toString()]) as Map<String,Object>

        then:
        response != null
        response['error'] == null
        response['result'] != null

        when:
        def txinfo = getPayload(txid)

        then:
        txinfo != null
        txinfo == response['result']
    }

    def "getpayload_MP: the payload of an unconfirmed transaction be extracted"() {
        when:
        def txid = send_MP(fundedAddress, newAddress, CurrencyID.TMSC, 0.15)
        def sendTxUnconfirmed = getRawTransaction(txid, true) as Map
        def unspentOutputs = listUnspent(0, 0, [fundedAddress])

        then:
        !sendTxUnconfirmed.containsKey('confirmations')
        unspentOutputs.size() == 1

        when:
        def txinfo = getPayload(txid)

        then:
        txinfo != null

        when:
        generateBlock()

        then:
        def sendTxConfirmed = getRawTransaction(txid, true) as Map
        def unspentOutputsNow = listUnspent(0, 0, [fundedAddress])
        sendTxConfirmed.containsKey('confirmations')
        sendTxConfirmed.confirmations == 1
        unspentOutputsNow.size() == 0
    }

    def "getpayload_MP: throws an exception for a non-existing transaction"() {
        given:
        def invalidTxid = new Sha256Hash('d2193d7776c01b6aa17746774f7294223d4e7301917fa4d6a66d44af20f3bfbe')

        when:
        getPayload(invalidTxid)

        then:
        JsonRPCStatusException e = thrown()
        e.message == "No information available about transaction"
    }

    def "getpayload_MP: throws an exception for a regular transaction without payload"() {
        given:
        def txid = sendBitcoin(fundedAddress, newAddress, 0.0001)
        generateBlock()

        when:
        getPayload(txid)

        then:
        JsonRPCStatusException e = thrown()
        e.message == "Not an Omni transaction"
    }

    def "getpayload_MP: a payload has at least the fields \"version\" and \"type\""() {
        given:
        def payload = '00000007'
        def txid = sendrawtx_MP(fundedAddress, payload)
        generateBlock()

        when:
        def txinfo = getPayload(txid)

        then:
        txinfo.containsKey('version')
        txinfo.containsKey('type')
        txinfo.version == 0
        txinfo.type == 7
    }

    def "getpayload_MP: only \"version\" and \"type\" are returned for unknown transaction types"() {
        given:
        def payload = '00010005000000030000000005f5e100'
        def txid = sendrawtx_MP(fundedAddress, payload)
        generateBlock()

        when:
        def txinfo = getPayload(txid)

        then: "there is a payload and base fields"
        txinfo.hex.startsWith(payload)
        txinfo.containsKey('version')
        txinfo.containsKey('type')
        txinfo.version == 1
        txinfo.type == 5

        and: "it does not have other fields"
        !txinfo.containsKey('property')
        !txinfo.containsKey('amount')
    }

    def "getpayload_MP: decodes reference: type 0, version 0 - simple send"() {
        given:
        def payload = '00000000000000010000000006dac2c0'

        when:
        def txid = sendrawtx_MP(fundedAddress, payload)
        def txinfo = getPayload(txid)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 0
        txinfo.property == 1
        txinfo.amount == 115000000L
    }

    def "getpayload_MP: decodes reference: type 3, version 0 - send to owners"() {
        given:
        def payload = '0000000300000006000000174876e800'

        when:
        def txid = sendrawtx_MP(fundedAddress, payload)
        def txinfo = getPayload(txid)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 3
        txinfo.property == 6
        txinfo.amount == 100000000000L
    }

    def "getpayload_MP: decodes reference: type 20, version 0 - sell tokens for bitcoin"() {
        given:
        def payload = '00000014000000010000000008f0d180000000174876e8000a0000000000989680'

        when:
        def txid = sendrawtx_MP(fundedAddress, payload)
        def txinfo = getPayload(txid)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 20
        txinfo.property == 1
        txinfo.amount_for_sale == 150000000L
        txinfo.amount_desired == 100000000000L
        txinfo.block_time_limit == 10
        txinfo.commitment_fee == 10000000L
    }

    def "getpayload_MP: decodes reference: type 20, version 1 - sell tokens for bitcoin"() {
        given:
        def payload = '00010014000000010000000008f0d180000000174876e8000a000000000098968001'

        when:
        def txid = sendrawtx_MP(fundedAddress, payload)
        def txinfo = getPayload(txid)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 1
        txinfo.type == 20
        txinfo.property == 1
        txinfo.amount_for_sale == 150000000L
        txinfo.amount_desired == 100000000000L
        txinfo.block_time_limit == 10
        txinfo.commitment_fee == 10000000L
        txinfo.subaction == 1
    }

    def "getpayload_MP: decodes reference: type 21, version 0 - sell tokens for tokens"() {
        given:
        def payload = '0000001500000001000000000ee6b28000000003000000012a05f20001'

        when:
        def txid = sendrawtx_MP(fundedAddress, payload)
        def txinfo = getPayload(txid)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 21
        txinfo.property == 1
        txinfo.amount_for_sale == 250000000L
        txinfo.property_desired == 3
        txinfo.amount_desired == 5000000000
        txinfo.subaction == 1
    }

    def "getpayload_MP: decodes reference: type 22, version 0 - purchase tokens with bitcoin"() {
        given:
        def payload = '00000016000000010000000007bfa480'

        when:
        def txid = sendrawtx_MP(fundedAddress, payload)
        def txinfo = getPayload(txid)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 22
        txinfo.property == 1
        txinfo.amount == 130000000L
    }

    def "getpayload_MP: decodes reference: type 50, version 0 - create property"() {
        given:
        def payload = '0000003201000100000000436f6d70616e69657300426974636f696e204d696e696e67005175616e74756d204d' +
                '696e6572006275696c6465722e62697477617463682e636f000000000000000f4240'

        when:
        def txid = sendrawtx_MP(fundedAddress, payload)
        def txinfo = getPayload(txid)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 50
        txinfo.ecosystem == 1
        txinfo.property_type == 1
        txinfo.previous_property_id == 0
        txinfo.category == 'Companies'
        txinfo.subcategory == 'Bitcoin Mining'
        txinfo.name == 'Quantum Miner'
        txinfo.url == 'builder.bitwatch.co'
        txinfo.data == ''
        txinfo.amount == 1000000
    }

    def "getpayload_MP: decodes reference: type 51, version 0 - create crowdsale"() {
        given:
        def payload = '0000003301000100000000436f6d70616e69657300426974636f696e204d696e696e67005175616e74756d204d' +
                '696e6572006275696c6465722e62697477617463682e636f000000000001000000000000006400000001ccd403f00a0c'

        when:
        def txid = sendrawtx_MP(fundedAddress, payload)
        def txinfo = getPayload(txid)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 51
        txinfo.ecosystem == 1
        txinfo.property_type == 1
        txinfo.previous_property_id == 0
        txinfo.category == 'Companies'
        txinfo.subcategory == 'Bitcoin Mining'
        txinfo.name == 'Quantum Miner'
        txinfo.url == 'builder.bitwatch.co'
        txinfo.data == ''
        txinfo.property_desired == 1
        txinfo.token_per_unit_vested == 100
        txinfo.deadline == 7731414000L
        txinfo.early_bird_bonus == 10
        txinfo.issuer_bonus == 12
    }

    def "getpayload_MP: decodes reference: type 51, version 1 - create crowdsale"() {
        given:
        def payload = '0001003301000100000000436f6d70616e69657300426974636f696e204d696e696e67005175616e74756d204d' +
                '696e6572006275696c6465722e62697477617463682e636f000000000001000000000000006400000001ccd403f00a0c'

        when:
        def txid = sendrawtx_MP(fundedAddress, payload)
        def txinfo = getPayload(txid)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 1
        txinfo.type == 51
        txinfo.ecosystem == 1
        txinfo.property_type == 1
        txinfo.previous_property_id == 0
        txinfo.category == 'Companies'
        txinfo.subcategory == 'Bitcoin Mining'
        txinfo.name == 'Quantum Miner'
        txinfo.url == 'builder.bitwatch.co'
        txinfo.data == ''
        txinfo.property_desired == 1
        txinfo.token_per_unit_vested == 100
        txinfo.deadline == 7731414000L
        txinfo.early_bird_bonus == 10
        txinfo.issuer_bonus == 12
    }

    def "getpayload_MP: decodes reference: type 53, version 0 - close crowdsale"() {
        given:
        def payload = '0000003500000009'

        when:
        def txid = sendrawtx_MP(fundedAddress, payload)
        def txinfo = getPayload(txid)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 53
        txinfo.property == 9
    }

    def "getpayload_MP: decodes reference: type 54, version 0 - create managed property"() {
        given:
        def payload = '0000003601000100000000436f6d70616e69657300426974636f696e204d696e696e67005175616e74756d' +
                '204d696e6572006275696c6465722e62697477617463682e636f0000'

        when:
        def txid = sendrawtx_MP(fundedAddress, payload)
        def txinfo = getPayload(txid)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 54
        txinfo.ecosystem == 1
        txinfo.property_type == 1
        txinfo.previous_property_id == 0
        txinfo.category == 'Companies'
        txinfo.subcategory == 'Bitcoin Mining'
        txinfo.name == 'Quantum Miner'
        txinfo.url == 'builder.bitwatch.co'
        txinfo.data == ''
    }

    def "getpayload_MP: decodes reference: type 55, version 0 - grant token"() {
        given:
        def payload = '000000370000000800000000000003e84669727374204d696c6573746f6e6520526561636865642100'

        when:
        def txid = sendrawtx_MP(fundedAddress, payload)
        def txinfo = getPayload(txid)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 55
        txinfo.property == 8
        txinfo.amount == 1000
        txinfo.memo == 'First Milestone Reached!'
    }

    def "getpayload_MP: decodes reference: type 56, version 0 - revoke token"() {
        given:
        def payload = '000000380000000800000000000003e8526564656d7074696f6e206f6620746f6b656e7320666f7220426f' +
                '622c205468616e6b7320426f622100'

        when:
        def txid = sendrawtx_MP(fundedAddress, payload)
        def txinfo = getPayload(txid)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 56
        txinfo.property == 8
        txinfo.amount == 1000
        txinfo.memo == 'Redemption of tokens for Bob, Thanks Bob!'
    }

    def "getpayload_MP: decodes reference: type 70, version 0 - change issuer"() {
        given:
        def payload = '000000460000000d'

        when:
        def txid = sendrawtx_MP(fundedAddress, payload)
        def txinfo = getPayload(txid)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 70
        txinfo.property == 13
    }

    def getPayload(Sha256Hash txid) {
        def response = client.send('getpayload_MP', [txid.toString()]) as Map<String,Object>
        def result = response.result as Map<String,String>

        prettyPrint(result)

        return result
    }

    def prettyPrint(def data) {
        def json = JsonOutput.toJson(data)
        def output = JsonOutput.prettyPrint(json)

        println(output)
    }

}
