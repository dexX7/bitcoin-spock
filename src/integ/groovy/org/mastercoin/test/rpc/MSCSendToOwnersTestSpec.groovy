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
    def "Row #rowNum: In test ecosystem send a divisible amount of #stoAmountSP to #inputOwnerSP.size owners"() {

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
        rowNum | inputSP              | stoAmountSP          | inputMSC   | inputOwnerSP                         || expectedSP           | expectedMSC | expectedOwnerSP
        16     | 92233720368.54775806 |           0.00000001 | 0.1        | [0.00000001                        ] || 92233720368.54775805 | 0.09999999  | [  0.00000002                                           ]
        17     | 90000000000.0        | 10000000000.0        | 0.1        | [1.0,        1.0                   ] || 80000000000.0        | 0.09999998  | [5000000001.0,      5000000001.0                        ]
        18     | 80000000000.0        |         333.3        | 0.1        | [1.0,        1.0,        1.0       ] || 79999999666.7        | 0.09999997  | [112.1,       112.1,       112.1                        ]
        19     |         100.0        |           0.5        | 0.1        | [2.0,        3.0                   ] ||          99.5        | 0.09999998  | [  2.2,         3.3                                     ]
        20     |          99.5        |          60.0        | 0.0000001  | [0.00000002, 0.00000001, 0.00000003] ||          39.5        | 0.00000007  | [ 20.00000002, 10.00000001, 30.00000003                 ]
        34     | 90000000000.0        |           0.22222221 | 0.1        | [1.0,        1.0                   ] || 89999999999.77777779 | 0.09999998  | [  1.11111111,  1.11111110                              ]
        35     | 89999999999.77777779 |           1.0        | 0.1        | [1.0,        1.0,        1.0       ] || 89999999998.77777779 | 0.09999997  | [  1.33333334,  1.33333334,  1.33333332                 ]
        36     |         100.0        |           0.50000001 | 0.1        | [2.0,        3.0                   ] ||          99.49999999 | 0.09999998  | [  2.2,         3.30000001                              ]
        37     |          99.49999999 |          59.99999997 | 0.0000001  | [0.00000002, 0.00000001, 0.00000003] ||          39.50000002 | 0.00000007  | [ 20.00000001,  9.99999994, 30.00000002                 ]
        39     |           0.00000001 |           0.00000001 | 0.00000001 | [1.0,        1.0                   ] ||           0.00000000 | 0.0         | [  1.00000001,  1.0                                     ]
        40     |           0.00000002 |           0.00000002 | 0.00000002 | [1.0,        1.0,        1.0       ] ||           0.00000000 | 0.0         | [  1.00000001,  1.00000001,  1.0                        ]
        42     |          10.00000010 |           0.00000010 | 0.00000002 | [0.1, 900000000.0, 100000000.0     ] ||          10.00000000 | 0.0         | [  0.1, 900000000.00000009,      100000000.00000001     ]
        44     |           1.0        |           0.00000004 | 1.0        | [1.0,  1.0,  1.0,  1.0,  1.0       ] ||           0.99999996 | 0.99999996  | [  1.00000001,  1.00000001,  1.00000001, 1.00000001, 1.0]
        45     |           1.0        |           0.00000006 | 1.0        | [1.0,  1.0,  1.0,  1.0,  1.0       ] ||           0.99999994 | 0.99999997  | [  1.00000002,  1.00000002,  1.00000002, 1.0,        1.0]
        46     |          10.00000010 |           0.00000011 | 0.00000002 | [0.1, 900000000.0, 100000000.0     ] ||           9.99999999 | 0.0         | [  0.1, 900000000.00000010,      100000000.00000001     ]

    }
}