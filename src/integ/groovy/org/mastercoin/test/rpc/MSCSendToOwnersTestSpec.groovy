package org.mastercoin.test.rpc

import com.google.bitcoin.core.Address
import org.mastercoin.BaseRegTestSpec
import org.mastercoin.CurrencyID
import org.mastercoin.Ecosystem
import org.mastercoin.PropertyType
import spock.lang.Unroll

import static org.mastercoin.CurrencyID.TMSC

/**
 *
 */
class MSCSendToOwnersTestSpec extends BaseRegTestSpec {
    final static BigDecimal stoFeePerAddress = 0.00000001
    final static BigInteger COIN = 100000000

    @Unroll
    def "In test ecosystem send a divisible amount of #stoAmountSP to #inputOwnerSP.size owners"() {

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
            setupSP *= COIN
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
        ownerIds.each {
            owners << newAddress
            send_MP(actorAddress, owners[it], currencySP, inputOwnerSP[it])
        }
        generateBlock()

        then: "the actor has a balance of #inputMSC and #inputSP"
        getbalance_MP(actorAddress, currencyMSC).balance == inputMSC
        getbalance_MP(actorAddress, currencySP).balance == inputSP

        and: "all owners have their starting balances"
        ownerIds.every { getbalance_MP(owners[it], currencySP).balance == inputOwnerSP[it] }


        when: "#stoAmountSP is sent to owners of #currencySP"
        sendToOwnersMP(actorAddress, currencySP, stoAmountSP)
        generateBlock()

        then: "the sender ends up with #expectedMSC and #expectedSP"
        getbalance_MP(actorAddress, currencyMSC).balance == expectedMSC
        getbalance_MP(actorAddress, currencySP).balance == expectedSP

        and: "every owner has the expected balances"
        ownerIds.every { getbalance_MP(owners[it], currencySP).balance == expectedOwnerSP[it] }


        where:
        inputSP              | stoAmountSP | inputMSC  | inputOwnerSP                         || expectedSP           | expectedMSC | expectedOwnerSP
        92233720368.54775806 |  0.00000001 | 0.1       | [0.00000001]                         || 92233720368.54775805 | 0.09999999  | [0.00000002]
                100.0        |  0.5        | 0.1       | [2.0, 3.0]                           ||          99.5        | 0.09999998  | [2.2, 3.3]
                 99.5        | 60.0        | 0.0000001 | [0.00000002, 0.00000001, 0.00000003] ||          39.5        | 0.00000007  | [20.00000002, 10.00000001, 30.00000003]
    }
}