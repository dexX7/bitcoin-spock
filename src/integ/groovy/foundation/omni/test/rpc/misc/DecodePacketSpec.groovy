package foundation.omni.test.rpc.misc

import com.msgilligan.bitcoin.rpc.JsonRPCStatusException
import foundation.omni.BaseRegTestSpec
import groovy.json.JsonOutput

class DecodePacketSpec extends BaseRegTestSpec {

    def "decodepacket_MP: creates no error for valid input"() {
        given:
        def payloadReference = '00000000000000010000000006dac2c0'

        when:
        def response = client.send('decodepacket_MP', [payloadReference]) as Map<String,Object>

        then:
        response != null
        response['error'] == null
        response['result'] != null

        when:
        def packet = decodePacket(payloadReference)

        then:
        packet != null
        packet == response['result']
    }

    def "decodepacket_MP: throws an exception for invalid hex"() {
        given:
        def invalidHex = '111zq'

        when:
        decodePacket(invalidHex)

        then:
        JsonRPCStatusException e = thrown()
        e.message == "Input is not a valid hex"
    }

    def "decodepacket_MP: decodes reference: type 0, version 0 - simple send"() {
        given:
        def payload = '00000000000000010000000006dac2c0'

        when:
        def txinfo = decodePacket(payload)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 0
        txinfo.property == 1
        txinfo.amount == 115000000L
    }

    def "decodepacket_MP: decodes reference: type 3, version 0 - send to owners"() {
        given:
        def payload = '0000000300000006000000174876e800'

        when:
        def txinfo = decodePacket(payload)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 3
        txinfo.property == 6
        txinfo.amount == 100000000000L
    }

    def "decodepacket_MP: decodes reference: type 20, version 0 - sell tokens for bitcoin"() {
        given:
        def payload = '00000014000000010000000008f0d180000000174876e8000a0000000000989680'

        when:
        def txinfo = decodePacket(payload)

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

    def "decodepacket_MP: decodes reference: type 20, version 1 - sell tokens for bitcoin"() {
        given:
        def payload = '00010014000000010000000008f0d180000000174876e8000a000000000098968001'

        when:
        def txinfo = decodePacket(payload)

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

    def "decodepacket_MP: decodes reference: type 21, version 0 - sell tokens for tokens"() {
        given:
        def payload = '0000001500000001000000000ee6b28000000003000000012a05f20001'

        when:
        def txinfo = decodePacket(payload)

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

    def "decodepacket_MP: decodes reference: type 22, version 0 - purchase tokens with bitcoin"() {
        given:
        def payload = '00000016000000010000000007bfa480'

        when:
        def txinfo = decodePacket(payload)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 22
        txinfo.property == 1
        txinfo.amount == 130000000L
    }

    def "decodepacket_MP: decodes reference: type 50, version 0 - create property"() {
        given:
        def payload = '0000003201000100000000436f6d70616e69657300426974636f696e204d696e696e67005175616e74756d204d' +
                '696e6572006275696c6465722e62697477617463682e636f000000000000000f4240'

        when:
        def txinfo = decodePacket(payload)

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

    def "decodepacket_MP: decodes reference: type 51, version 0 - create crowdsale"() {
        given:
        def payload = '0000003301000100000000436f6d70616e69657300426974636f696e204d696e696e67005175616e74756d204d' +
                '696e6572006275696c6465722e62697477617463682e636f000000000001000000000000006400000001ccd403f00a0c'

        when:
        def txinfo = decodePacket(payload)

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

    def "decodepacket_MP: decodes reference: type 51, version 1 - create crowdsale"() {
        given:
        def payload = '0001003301000100000000436f6d70616e69657300426974636f696e204d696e696e67005175616e74756d204d' +
                '696e6572006275696c6465722e62697477617463682e636f000000000001000000000000006400000001ccd403f00a0c'

        when:
        def txinfo = decodePacket(payload)

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

    def "decodepacket_MP: decodes reference: type 53, version 0 - close crowdsale"() {
        given:
        def payload = '0000003500000009'

        when:
        def txinfo = decodePacket(payload)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 53
        txinfo.property == 9
    }

    def "decodepacket_MP: decodes reference: type 54, version 0 - create managed property"() {
        given:
        def payload = '0000003601000100000000436f6d70616e69657300426974636f696e204d696e696e67005175616e74756d' +
                '204d696e6572006275696c6465722e62697477617463682e636f0000'

        when:
        def txinfo = decodePacket(payload)

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

    def "decodepacket_MP: decodes reference: type 55, version 0 - grant token"() {
        given:
        def payload = '000000370000000800000000000003e84669727374204d696c6573746f6e6520526561636865642100'

        when:
        def txinfo = decodePacket(payload)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 55
        txinfo.property == 8
        txinfo.amount == 1000
        txinfo.memo == 'First Milestone Reached!'
    }

    def "decodepacket_MP: decodes reference: type 56, version 0 - revoke token"() {
        given:
        def payload = '000000380000000800000000000003e8526564656d7074696f6e206f6620746f6b656e7320666f7220426f' +
                '622c205468616e6b7320426f622100'

        when:
        def txinfo = decodePacket(payload)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 56
        txinfo.property == 8
        txinfo.amount == 1000
        txinfo.memo == 'Redemption of tokens for Bob, Thanks Bob!'
    }

    def decodePacket(String packet) {
        def response = client.send('decodepacket_MP', [packet]) as Map<String,Object>
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
