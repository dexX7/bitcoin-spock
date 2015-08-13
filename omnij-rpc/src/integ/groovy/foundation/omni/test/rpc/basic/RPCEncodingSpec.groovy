package foundation.omni.test.rpc.basic

import com.fasterxml.jackson.core.JsonParseException
import foundation.omni.BaseMainNetSpec
import foundation.omni.CurrencyID

/**
 * Note: this is a test for mainnet, and not regtest!
 */
class RPCEncodingSpec extends BaseMainNetSpec {

    def "Can handle UTF8 compliant data via RPC"() {
        when:
        try {
            client.omniGetProperty(new CurrencyID(3))
        } catch (JsonParseException e) {
            log.info e.toString()
            throw e
        }

        then:
        notThrown(JsonParseException)
    }

    def "Can handle non-UTF8 compliant data via RPC"() {
        when:
        try {
            client.omniGetProperty(new CurrencyID(2147483662))
        } catch (JsonParseException e) {
            log.info e.toString()
            throw e
        }

        then:
        notThrown(JsonParseException)
    }

}
