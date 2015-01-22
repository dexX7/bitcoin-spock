package foundation.omni.test.rpc.sto

import com.google.bitcoin.core.Address
import com.msgilligan.bitcoin.BTC
import com.xlson.groovycsv.CsvParser
import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.PropertyType
import spock.lang.Shared
import spock.lang.Unroll
/**
 * Data driven tests for the "send to owners" transaction type
 */
class MSCSendToOwnersTestPlanSpec extends BaseRegTestSpec {
    final static BigDecimal startBTC = 0.1

    @Shared
    def testdata

    def setupSpec() {
        def file = new File(getTestPlanPath())
        def tsv = file.text
        def data = new CsvParser().parse(tsv, separator: '\t')
        testdata = data
    }

    def getTestPlanPath() {
        return "src/integ/groovy/foundation/omni/test/rpc/sto/sto-testplan.tsv"
    }

    @Unroll
    def "#description"() {
        assert numOwners == amountAvailableOwners.size()
        assert numOwners == amountReservedOwners.size()
        assert numOwners == expectedAmountAvailableOwners.size()
        assert numOwners == expectedAmountReservedOwners.size()

        maybeSkipInvalidationTests(expectedValidity)
        maybeSkipReservedAmountsTests(amountReserved, amountReservedOwners)

        // Fund actor
        def startMSC = mscAvailable + mscReserved
        def currencyMSC = new CurrencyID(ecosystem.longValue())
        def actorAddress = createFundedAddress(startBTC, startMSC)

        // Create a DEx offer to reserve an amount
        if (mscReserved > 0) {
            reserveAmountMSC(actorAddress, currencyMSC, mscReserved)
        }

        // Create property
        def currencySPT = createStoProperty(actorAddress, data)
        def owners = [] as List<Address>
        def ownerIds = 0..<numOwners

        when: "the owners are funded"
        ownerIds.each { owners << newAddress }
        owners = owners.sort { it.toString() }
        ownerIds.each { send_MP(actorAddress, owners[it], currencySPT, amountAvailableOwners[it]) }
        generateBlock()

        then: "the actor starts with the correct #currencySPT and #currencyMSC balance"
        def balanceActorMSC = getbalance_MP(actorAddress, currencyMSC)
        def balanceActorSPT = getbalance_MP(actorAddress, currencySPT)
        balanceActorMSC.balance == mscAvailable
        balanceActorMSC.reserved == mscReserved
        balanceActorSPT.balance == amountAvailable
        balanceActorSPT.reserved == amountReserved

        and: "every owner starts with the correct #currencySPT balance"
        for (id in ownerIds) {
            def balanceOwner = getbalance_MP(owners[id], currencySPT)
            balanceOwner.balance == amountAvailableOwners[id]
            balanceOwner.reserved == amountReservedOwners[id]
        }

        when: "#amountSTO is sent to owners of #currencySPT"
        def txid = executeSendToOwners(actorAddress, currencySPT, propertyType, amountSTO)
        generateBlock()

        then: "the transaction validity is #expectedValidity"
        def transaction = getTransactionMP(txid)
        transaction.valid == expectedValidity
        transaction.confirmations == 1

        and: "the sender ends up with the expected #currencySPT and #currencyMSC balance"
        def balanceActorFinalMSC = getbalance_MP(actorAddress, currencyMSC)
        def balanceActorFinalSPT = getbalance_MP(actorAddress, currencySPT)
        balanceActorFinalMSC.balance == expectedMSCAvailable
        balanceActorFinalMSC.reserved == expectedMSCReserved
        balanceActorFinalSPT.balance == expectedAmountAvailable
        balanceActorFinalSPT.reserved == expectedAmountReserved

        and: "every owner ends up with the expected #currencySPT balance"
        for (id in ownerIds) {
            def balanceOwnerFinal = getbalance_MP(owners[id], currencySPT)
            balanceOwnerFinal.balance == expectedAmountAvailableOwners[id]
            balanceOwnerFinal.reserved == expectedAmountReservedOwners[id]
        }

        where:
        data << testdata
        description = new String(data.Description)
        ecosystem = new Ecosystem(Short.valueOf(data.Ecosystem))
        propertyType = new PropertyType(Integer.valueOf(data.PropertyType))
        propertyName = new String(data.PropertyName)
        amountAvailable = Eval.me(data.AmountAvailable)
        amountReserved = Eval.me(data.AmountReserved)
        amountSTO = Eval.me(data.AmountSTO)
        mscAvailable = new BigDecimal(data.MSCAvailable)
        mscReserved = new BigDecimal(data.MSCReserved)
        numOwners = new Integer(data.NumOwners)
        amountAvailableOwners = Eval.me(data.AmountAvailableOwners)
        amountReservedOwners = Eval.me(data.AmountReservedOwners)
        expectedValidity = new Boolean(data.ExpectedValidity)
        expectedAmountAvailable = Eval.me(data.ExpectedAmountAvailable)
        expectedAmountReserved = Eval.me(data.ExpectedAmountReserved)
        expectedMSCAvailable = new BigDecimal(data.ExpectedMSCAvailable)
        expectedMSCReserved = new BigDecimal(data.ExpectedMSCReserved)
        expectedAmountAvailableOwners = Eval.me(data.ExpectedAmountAvailableOwners)
        expectedAmountReservedOwners = Eval.me(data.ExpectedAmountReservedOwners)
    }

    /**
     * Creates a new property and returns it's identifier.
     */
    def createStoProperty(Address actorAddress, def data) {
        def amountAvailableOwners = Eval.me(data.AmountAvailableOwners)
        def amountAvailable = Eval.me(data.AmountAvailable)
        def ecosystem = new Ecosystem(Short.valueOf(data.Ecosystem))
        def propertyType = new PropertyType(Integer.valueOf(data.PropertyType))

        def numberOfTokens = amountAvailableOwners.sum() + amountAvailable

        if (propertyType == PropertyType.DIVISIBLE) {
            numberOfTokens = BTC.btcToSatoshis(numberOfTokens)
        }

        def txid = createProperty(actorAddress, ecosystem, propertyType, numberOfTokens.longValue())
        generateBlock()

        def transaction = getTransactionMP(txid)
        assert transaction.valid == true
        assert transaction.confirmations == 1

        def currencyID = new CurrencyID(transaction.propertyid)
        return currencyID
    }

    /**
     * Creates an offer on the distributed exchange to reserve an amount.
     */
    def reserveAmountMSC(Address actorAddress, CurrencyID currency, BigDecimal reservedAmount) {
        def desiredBTC = 1.0
        def blockSpan = 100
        def commitFee = 0.0001
        def action = 1 // new offer

        def txid = createDexSellOffer(actorAddress, currency, reservedAmount, desiredBTC, blockSpan, commitFee, action)
        generateBlock()

        def transaction = getTransactionMP(txid)
        assert transaction.valid == true
        assert transaction.confirmations == 1
    }

    /**
     * Base method
     */
    def executeSendToOwners(Address actorAddress, CurrencyID currencyId, PropertyType propertyType, def amount) {
        def txid = sendToOwnersMP(actorAddress, currencyId, amount)
        return txid
    }

    /**
     * Base method
     */
    def maybeSkipInvalidationTests(Boolean expectedValidity) {
        if (expectedValidity != true) {
            throw new org.junit.internal.AssumptionViolatedException("skipped")
        }
    }

    /**
     * Base method
     */
    def maybeSkipReservedAmountsTests(def amountReserved, def amountReservedOwners) {
        if (amountReserved > 0) {
            throw new org.junit.internal.AssumptionViolatedException("skipped")
        }
        if (amountReservedOwners.sum() > 0) {
            throw new org.junit.internal.AssumptionViolatedException("skipped")
        }
    }
}
