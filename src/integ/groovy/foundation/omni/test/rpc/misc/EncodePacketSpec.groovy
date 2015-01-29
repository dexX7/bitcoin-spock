package foundation.omni.test.rpc.misc

import foundation.omni.BaseRegTestSpec
import groovy.json.JsonOutput
import spock.lang.Unroll

class EncodePacketSpec extends BaseRegTestSpec {

    @Unroll
    def "encode_simple_send: #property #amount"() {
        when:
        def payload = sendRPC('encode_simple_send', [property, amount])
        def txinfo = decodePacket(payload)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 0
        txinfo.property == property
        txinfo.amount == amount

        where:
        property | amount
        1        | 1
        2        | 115000000L
        3        | 9223372036854775807L
    }

    @Unroll
    def "encode_send_to_owners: #property #amount"() {
        when:
        def payload = sendRPC('encode_send_to_owners', [property, amount])
        def txinfo = decodePacket(payload)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 3
        txinfo.property == property
        txinfo.amount == amount

        where:
        property | amount
        6        | 100000000000L
    }

    @Unroll
    def "encode_offer_tokens: #property #amount_for_sale #amount_desired #block_time_limit #commitment_fee #subaction"() {
        when:
        def payload = sendRPC('encode_offer_tokens',
                              [property, amount_for_sale, amount_desired, block_time_limit, commitment_fee, subaction])
        def txinfo = decodePacket(payload)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 1
        txinfo.type == 20
        txinfo.property == property
        txinfo.amount_for_sale == amount_for_sale
        txinfo.amount_desired == amount_desired
        txinfo.block_time_limit == block_time_limit
        txinfo.commitment_fee == commitment_fee

        where:
        property | amount_for_sale | amount_desired | block_time_limit | commitment_fee | subaction
        1        | 150000000L      | 100000000000L  | 10               | 10000000L      | 1
    }

    @Unroll
    def "encode_trade_tokens: #property #amount_for_sale #property_desired #amount_desired #subaction"() {
        when:
        def payload = sendRPC('encode_trade_tokens',
                              [property, amount_for_sale, property_desired, amount_desired, subaction])
        def txinfo = decodePacket(payload)

        then:
        txinfo.hex.startsWith(payload)
        txinfo.version == 0
        txinfo.type == 21
        txinfo.property == property
        txinfo.amount_for_sale == amount_for_sale
        txinfo.property_desired == property_desired
        txinfo.amount_desired == amount_desired
        txinfo.subaction == subaction

        where:
        property | amount_for_sale | property_desired | amount_desired | subaction
        1        | 250000000L      | 3                | 5000000000L    | 1
    }


    def decodePacket(String packet) {
        def response = client.send('decodepacket_MP', [packet]) as Map<String,Object>
        def result = response.result as Map<String,String>

        prettyPrint(result)

        return result
    }

    def sendRPC(String command, List<Object> params) {
        def response = client.send(command, params) as Map<String,Object>
        def result = response.result as String

        return result
    }

    def prettyPrint(def data) {
        def json = JsonOutput.toJson(data)
        def output = JsonOutput.prettyPrint(json)

        println(output)
    }

}
