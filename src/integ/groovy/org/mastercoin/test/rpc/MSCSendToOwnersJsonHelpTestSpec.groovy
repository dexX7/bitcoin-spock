package org.mastercoin.test.rpc

import com.google.bitcoin.core.Address
import com.msgilligan.bitcoin.BTC
import groovy.json.JsonSlurper
import org.mastercoin.BaseRegTestSpec
import org.mastercoin.CurrencyID
import org.mastercoin.Ecosystem
import org.mastercoin.PropertyType
import spock.lang.Shared
import spock.lang.Unroll


/**
 *
 */
class MSCSendToOwnersJsonHelpTestSpec extends BaseRegTestSpec {
    final static BigDecimal stoFeePerAddress = 0.00000001

    @Shared
    def testdata

    def setupSpec() {
        def path = "src/integ/groovy/org/mastercoin/test/rpc/data_v2.json"
        def file = new File(path)
        def jsonSlurper = new JsonSlurper()
        def data = jsonSlurper.parse(file)
        testdata = data
    }

    @Unroll
    def "Row #data.rowNum: In Ecosystem #data.ecosystem send #data.stoAmountSP to #data.inputOwnerSP.size owners"() {
        setup:
        assert data.inputOwnerSP.size() == data.expectedOwnerSP.size()

        def actorAddress = createFundedAddress(data.inputBTC, data.inputMSC)
        def currencyMSC = new CurrencyID(data.currencyMSC)
        def currencySP = createStoProperty(actorAddress, data)
        def owners = [] as List<Address>
        def ownerIds = 0..<data.inputOwnerSP.size()

        when: "the owners are funded"
        ownerIds.each { owners << newAddress }
        owners = owners.sort { it.toString() }
        ownerIds.each { send_MP(actorAddress, owners[it], currencySP, data.inputOwnerSP[it]) }
        generateBlock()

        then: "the actor has a balance of #inputMSC and #inputSP"
        getbalance_MP(actorAddress, currencyMSC).balance == data.inputMSC
        getbalance_MP(actorAddress, currencySP).balance == data.inputSP

        and: "all owners have their starting balances"
        for (it in ownerIds) {
            getbalance_MP(owners[it], currencySP).balance == data.inputOwnerSP[it]
        }

        when: "#stoAmountSP is sent to owners of #currencySP"
        sendToOwnersMP(actorAddress, currencySP, data.stoAmountSP)
        generateBlock()

        then: "the sender ends up with #expectedMSC and #expectedSP"
        getbalance_MP(actorAddress, currencyMSC).balance == data.expectedMSC
        getbalance_MP(actorAddress, currencySP).balance == data.expectedSP

        and: "every owner has the expected balances"
        for (it in ownerIds) {
            getbalance_MP(owners[it], currencySP).balance == data.expectedOwnerSP[it]
        }

        where:
        data << testdata
    }

    def createStoProperty(Address actorAddress, Map data) { 
        def ecosystem = new Ecosystem(data.ecosystem)
        def divisibility = new PropertyType(data.divisibility)
        def numberOfTokens = data.inputOwnerSP.sum() + data.inputSP

        if (divisibility == PropertyType.DIVISIBLE) {
            numberOfTokens = BTC.btcToSatoshis(numberOfTokens)
        }

        def txid = createProperty(actorAddress, ecosystem, divisibility, numberOfTokens)
        generateBlock()

        def transaction = getTransactionMP(txid)
        assert transaction.valid == true
        assert transaction.confirmations == 1

        def currencyID = new CurrencyID(transaction.propertyid)
        return currencyID
    }

}
