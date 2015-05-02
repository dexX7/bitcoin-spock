package foundation.omni.test.rpc.mdex

import com.msgilligan.bitcoin.BTC
import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.PropertyType
import org.bitcoinj.core.Address
import org.junit.internal.AssumptionViolatedException
import spock.lang.Unroll

/**
 *
 */
class OneStepTradeSpec extends BaseRegTestSpec {
    final static BigDecimal startBTC = 0.1
    final static BigDecimal startMSC = 0.1
    final static BigDecimal zeroAmount = 0.0
    final static Byte actionNew = 1

    @Unroll
    def "#amountSPX #propertyType trade for #amountMSC #propertyMSC"() {

        when: "Setup"
        def actorA1 = createFundedAddress(startBTC, zeroAmount)
        def actorA2 = createFundedAddress(startBTC, amountMSC)

        def propertySPX = fundNewProperty(actorA1, amountSPX, propertyType, propertyMSC.ecosystem)

        then:
        getbalance_MP(actorA1, propertyMSC).balance == zeroAmount
        getbalance_MP(actorA1, propertySPX).balance == amountSPX
        getbalance_MP(actorA2, propertyMSC).balance == amountMSC
        getbalance_MP(actorA2, propertySPX).balance == zeroAmount

        and:
        getbalance_MP(actorA1, propertyMSC).reserved == zeroAmount
        getbalance_MP(actorA1, propertySPX).reserved == zeroAmount
        getbalance_MP(actorA2, propertyMSC).reserved == zeroAmount
        getbalance_MP(actorA2, propertySPX).reserved == zeroAmount

        and:
        getorderbook_MP(propertySPX).empty

        when: "Offer A for B"
        def tradeTxidA1 = trade_MP(actorA1, amountSPX, propertySPX, amountMSC, propertyMSC, actionNew)
        generateBlock()

        then:
        gettrade_MP(tradeTxidA1).valid

        and:
        getorderbook_MP(propertySPX).size() == 1

        and:
        getbalance_MP(actorA1, propertySPX).balance == zeroAmount
        getbalance_MP(actorA1, propertySPX).reserved == amountSPX

        when: "Offer B for A"
        def tradeTxidA2 = trade_MP(actorA2, amountMSC, propertyMSC, amountSPX, propertySPX, actionNew)
        generateBlock()

        then:
        gettrade_MP(tradeTxidA2).valid

        and:
        getorderbook_MP(propertySPX).empty

        and:
        getbalance_MP(actorA1, propertyMSC).balance == amountMSC
        getbalance_MP(actorA1, propertySPX).balance == zeroAmount
        getbalance_MP(actorA2, propertyMSC).balance == zeroAmount
        getbalance_MP(actorA2, propertySPX).balance == amountSPX

        and:
        getbalance_MP(actorA1, propertyMSC).reserved == zeroAmount
        getbalance_MP(actorA1, propertySPX).reserved == zeroAmount
        getbalance_MP(actorA2, propertyMSC).reserved == zeroAmount
        getbalance_MP(actorA2, propertySPX).reserved == zeroAmount

        where:
        propertyType           | amountSPX            | propertyMSC     | amountMSC
        PropertyType.DIVISIBLE |           0.00000001 | CurrencyID.TMSC | 0.00000001
        PropertyType.DIVISIBLE |           0.00000003 | CurrencyID.TMSC | 0.00000001
        PropertyType.DIVISIBLE |           0.00000005 | CurrencyID.TMSC | 0.00000001
        PropertyType.DIVISIBLE |           0.00000007 | CurrencyID.TMSC | 0.00000001
        PropertyType.DIVISIBLE |           0.00000010 | CurrencyID.TMSC | 0.00000001
        PropertyType.DIVISIBLE |           0.00000049 | CurrencyID.TMSC | 0.00000001
        PropertyType.DIVISIBLE |           0.00000501 | CurrencyID.TMSC | 0.00000001
        PropertyType.DIVISIBLE |           0.00001000 | CurrencyID.TMSC | 0.00000001
        PropertyType.DIVISIBLE |           0.00050000 | CurrencyID.TMSC | 0.00000001
        PropertyType.DIVISIBLE |           0.33333333 | CurrencyID.TMSC | 0.00000001
        PropertyType.DIVISIBLE |           0.50000000 | CurrencyID.TMSC | 0.00000001
        PropertyType.DIVISIBLE |           0.10000000 | CurrencyID.TMSC | 0.00000001
        PropertyType.DIVISIBLE |           1.00000001 | CurrencyID.TMSC | 0.00000001
        PropertyType.DIVISIBLE |           1.25000000 | CurrencyID.TMSC | 0.00000001
        PropertyType.DIVISIBLE |           7.37500000 | CurrencyID.TMSC | 0.00000001
        PropertyType.DIVISIBLE | 92233720368.54775807 | CurrencyID.TMSC | 0.00000001
        PropertyType.DIVISIBLE |           1.00000000 | CurrencyID.MSC  | 1.00000000
        PropertyType.DIVISIBLE |           2.00000000 | CurrencyID.MSC  | 1.00000000
        PropertyType.DIVISIBLE |           3.00000000 | CurrencyID.MSC  | 1.00000000
        PropertyType.DIVISIBLE |           4.00000000 | CurrencyID.MSC  | 2.00000000
        PropertyType.DIVISIBLE |           6.00000000 | CurrencyID.MSC  | 2.00000000
        PropertyType.DIVISIBLE |           6.00000000 | CurrencyID.MSC  | 3.00000000
        PropertyType.DIVISIBLE |           9.00000000 | CurrencyID.MSC  | 3.00000000
        PropertyType.DIVISIBLE |           8.00000000 | CurrencyID.MSC  | 4.00000000
        PropertyType.DIVISIBLE |          12.00000000 | CurrencyID.MSC  | 4.00000000
        PropertyType.DIVISIBLE |          15.00000000 | CurrencyID.MSC  | 5.00000000
        PropertyType.DIVISIBLE |           2.00000000 | CurrencyID.MSC  | 6.00000000
    }

    CurrencyID fundNewProperty(Address address, BigDecimal amount, PropertyType type, Ecosystem ecosystem) {
        if (type == PropertyType.DIVISIBLE) {
            amount = BTC.btcToSatoshis(amount)
        }
        def txidCreation = createProperty(address, ecosystem, type, amount.longValue())
        generateBlock()
        def txCreation = getTransactionMP(txidCreation)
        assert txCreation.valid == true
        assert txCreation.confirmations == 1
        return new CurrencyID(txCreation.propertyid as long)
    }

    def setupSpec() {
        if (!commandExists("gettrade_MP")) {
            throw new AssumptionViolatedException('The client has no "gettrade_MP" command')
        }
        if (!commandExists("getorderbook_MP")) {
            throw new AssumptionViolatedException('The client has no "getorderbook_MP" command')
        }
    }

}
