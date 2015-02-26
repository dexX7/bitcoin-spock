package foundation.omni.test.rpc.misc
import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import groovy.json.JsonOutput
import org.bitcoinj.core.Address
import org.bitcoinj.core.Sha256Hash
import spock.lang.Unroll

class TxPositionSpec extends BaseRegTestSpec {

    def "Check transaction position and position within block"() {
        given:
        def actorAddress = newAddress
        requestBitcoin(actorAddress, 20.0)
        generateBlock()

        when:
        def txid_1 = sendBitcoin(actorAddress, newAddress, 1.0)
        def txid_2 = sendBitcoin(actorAddress, newAddress, 2.0)
        def txid_3 = sendBitcoin(actorAddress, newAddress, 3.0)
        def txid_4 = sendBitcoin(actorAddress, newAddress, 4.0)
        def txid_5 = sendBitcoin(actorAddress, newAddress, 5.0)

        and:
        generateBlock()

        then:
        def tx_1 = getTransaction(txid_1)
        def tx_2 = getTransaction(txid_2)
        def tx_3 = getTransaction(txid_3)
        def tx_4 = getTransaction(txid_4)
        def tx_5 = getTransaction(txid_5)

        tx_1.confirmations == 1
        tx_2.confirmations == 1
        tx_3.confirmations == 1
        tx_4.confirmations == 1
        tx_5.confirmations == 1

        and:
        getPosition(txid_1) != null
        getPosition(txid_2) != null
        getPosition(txid_3) != null
        getPosition(txid_4) != null
        getPosition(txid_5) != null
    }

    def getPosition(Sha256Hash hash) {
        def response = client.send('mscrpc', ['9', hash.toString()]) as Map<String,Object>
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
