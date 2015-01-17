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

import static org.mastercoin.CurrencyID.TMSC


/**
 *
 */
class MSCSendToOwnersJsonRowTestSpec extends BaseRegTestSpec {
    final static BigDecimal stoFeePerAddress = 0.00000001

    @Shared
    def testdata

    def setupSpec() {
        def path = "src/integ/groovy/org/mastercoin/test/rpc/data_rows.json"
        def file = new File(path)
        def jsonSlurper = new JsonSlurper()
        def data = jsonSlurper.parse(file)
        testdata = data
    }

    @Unroll
    def "Row #rowNum: In Ecosystem 2 send #stoAmountSP to #inputOwnerSP.size owners"() {

        setup:
        def inputBTC = 0.1
        def currencyMSC = TMSC
        def propertyEcosystem = Ecosystem.TMSC
        def propertyDivisibility = PropertyType.DIVISIBLE

        // sanity check
        assert inputOwnerSP.size() == expectedOwnerSP.size()

        def owners = [] as List<Address>
        def ownerIds = 0..<inputOwnerSP.size()
        def setupSP = inputOwnerSP.sum() + inputSP
        if (propertyDivisibility == PropertyType.DIVISIBLE) {
            setupSP = BTC.btcToSatoshis(setupSP)
        }

        // create actor
        def actorAddress = createFundedAddress(inputBTC, inputMSC)

        /// create property
        def setupTxid = createProperty(actorAddress, propertyEcosystem, propertyDivisibility, setupSP)
        generateBlock()

        // get identifier
        def setupTx = client.getTransactionMP(setupTxid)
        assert setupTx.valid == true
        assert setupTx.confirmations == 1
        def currencySP = new CurrencyID(setupTx.propertyid)


        when: "the owners are funded"
        ownerIds.each { owners << newAddress }
        owners = owners.sort { it.toString() }
        ownerIds.each { send_MP(actorAddress, owners[it], currencySP, inputOwnerSP[it]) }
        generateBlock()

        then: "the actor has a balance of #inputMSC and #inputSP"
        getbalance_MP(actorAddress, currencyMSC).balance == inputMSC
        getbalance_MP(actorAddress, currencySP).balance == inputSP

        and: "all owners have their starting balances"
        for (it in ownerIds) {
            getbalance_MP(owners[it], currencySP).balance == inputOwnerSP[it]
        }


        when: "#stoAmountSP is sent to owners of #currencySP"
        sendToOwnersMP(actorAddress, currencySP, stoAmountSP)
        generateBlock()

        then: "the sender ends up with #expectedMSC and #expectedSP"
        getbalance_MP(actorAddress, currencyMSC).balance == expectedMSC
        getbalance_MP(actorAddress, currencySP).balance == expectedSP

        and: "every owner has the expected balances"
        for (it in ownerIds) {
            getbalance_MP(owners[it], currencySP).balance == expectedOwnerSP[it]
        }

        where:
        [rowNum, inputSP, stoAmountSP, inputMSC, inputOwnerSP, expectedSP, expectedMSC, expectedOwnerSP] << testdata
    }

}
