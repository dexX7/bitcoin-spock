package foundation.omni.test.rpc.misc

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID

class CrowdsaleSpec extends BaseRegTestSpec {

    final static BigDecimal startBTC = 0.1
    final static BigDecimal startMSC = 100.0

    def "Crowdsale with 0.00000001 DivSPT for 0.00000001 MSC, invest 0.00000001 MSC"() {
        /*
            {
                "version" : 0,
                "type" : 51,
                "ecosystem" : 1,
                "property_type" : 2,
                "previous_property_id" : 0,
                "category" : "",
                "subcategory" : "",
                "name" : "MDiv",
                "url" : "",
                "data" : "",
                "property_desired" : 1,
                "token_per_unit_vested" : 1,
                "deadline" : 7731414000,
                "issuer_bonus" : 0
            }
        */
        def rawTx = "000000330100020000000000004d44697600000000000001000000000000000100000001ccd403f00000"
        def issuerAddress = createFundedAddress(startBTC, startMSC)
        def investorAddress = createFundedAddress(startBTC, startMSC)
        def currencyMSC = CurrencyID.MSC

        when: "creating a new crowdsale with 0.00000001 DivisibleSPT for 0.00000001 MSC"
        def crowdsaleTxid = sendrawtx_MP(issuerAddress, rawTx)
        generateBlock()

        then: "the crowdsale is active"
        def crowdsaleTx = getTransactionMP(crowdsaleTxid)
        crowdsaleTx.confirmations == 1
        crowdsaleTx.valid == true
        def propertyId = new CurrencyID(crowdsaleTx.propertyid as Long)
        def crowdsaleInfo = getCrowdsale(propertyId)
        crowdsaleInfo.active == true

        when: "participant invests 0.00000001 MSC"
        def sendTxid = send_MP(investorAddress, issuerAddress, currencyMSC, 0.00000001)
        generateBlock()

        then: "the investor should get 0.00000001 token"
        def sendTx = getTransactionMP(sendTxid)
        sendTx.valid == true
        getbalance_MP(investorAddress, propertyId).balance == 0.00000001
    }

    def "Crowdsale with 0.00000001 DivSPT for 0.00000001 MSC, invest 1.00000000 MSC"() {
        /*
            {
                "version" : 0,
                "type" : 51,
                "ecosystem" : 1,
                "property_type" : 2,
                "previous_property_id" : 0,
                "category" : "",
                "subcategory" : "",
                "name" : "MDiv",
                "url" : "",
                "data" : "",
                "property_desired" : 1,
                "token_per_unit_vested" : 1,
                "deadline" : 7731414000,
                "issuer_bonus" : 0
            }
        */
        def rawTx = "000000330100020000000000004d44697600000000000001000000000000000100000001ccd403f00000"
        def issuerAddress = createFundedAddress(startBTC, startMSC)
        def investorAddress = createFundedAddress(startBTC, startMSC)
        def currencyMSC = CurrencyID.MSC

        when: "creating a new crowdsale with 0.00000001 DivisibleSPT for 0.00000001 MSC"
        def crowdsaleTxid = sendrawtx_MP(issuerAddress, rawTx)
        generateBlock()

        then: "the crowdsale is active"
        def crowdsaleTx = getTransactionMP(crowdsaleTxid)
        crowdsaleTx.confirmations == 1
        crowdsaleTx.valid == true
        def propertyId = new CurrencyID(crowdsaleTx.propertyid as Long)
        def crowdsaleInfo = getCrowdsale(propertyId)
        crowdsaleInfo.active == true

        when: "participant invests 1.00000000 MSC"
        def sendTxid = send_MP(investorAddress, issuerAddress, currencyMSC, 1.00000000)
        generateBlock()

        then: "the investor should get 1.00000000 token"
        def sendTx = getTransactionMP(sendTxid)
        sendTx.valid == true
        getbalance_MP(investorAddress, propertyId).balance == 1.00000000
    }

    def "Crowdsale with 1 IndivSPT for 0.00000001 MSC, invest 0.00000001 MSC"() {
        /*
            {
                "version" : 0,
                "type" : 51,
                "ecosystem" : 1,
                "property_type" : 1,
                "previous_property_id" : 0,
                "category" : "",
                "subcategory" : "",
                "name" : "MIndiv",
                "url" : "",
                "data" : "",
                "property_desired" : 1,
                "token_per_unit_vested" : 1,
                "deadline" : 7731414000,
                "issuer_bonus" : 0
            }
        */
        def rawTx = "000000330100010000000000004d496e64697600000000000001000000000000000100000001ccd403f00000"
        def issuerAddress = createFundedAddress(startBTC, startMSC)
        def investorAddress = createFundedAddress(startBTC, startMSC)
        def currencyMSC = CurrencyID.MSC

        when: "creating a new crowdsale with 1 IndivisibleSPT for 0.00000001 MSC"
        def crowdsaleTxid = sendrawtx_MP(issuerAddress, rawTx)
        generateBlock()

        then: "the crowdsale is active"
        def crowdsaleTx = getTransactionMP(crowdsaleTxid)
        crowdsaleTx.confirmations == 1
        crowdsaleTx.valid == true
        def propertyId = new CurrencyID(crowdsaleTx.propertyid as Long)
        def crowdsaleInfo = getCrowdsale(propertyId)
        crowdsaleInfo.active == true

        when: "participant invests 0.00000001 MSC"
        def sendTxid = send_MP(investorAddress, issuerAddress, currencyMSC, 0.00000001)
        generateBlock()

        then: "the investor should get 1 token"
        def sendTx = getTransactionMP(sendTxid)
        sendTx.valid == true
        getbalance_MP(investorAddress, propertyId).balance == 1
    }

    def "Crowdsale with 1 IndivSPT for 0.00000001 MSC, invest 1.00000000 MSC"() {
        /*
            {
                "version" : 0,
                "type" : 51,
                "ecosystem" : 1,
                "property_type" : 1,
                "previous_property_id" : 0,
                "category" : "",
                "subcategory" : "",
                "name" : "MIndiv",
                "url" : "",
                "data" : "",
                "property_desired" : 1,
                "token_per_unit_vested" : 1,
                "deadline" : 7731414000,
                "issuer_bonus" : 0
            }
        */
        def rawTx = "000000330100010000000000004d496e64697600000000000001000000000000000100000001ccd403f00000"
        def issuerAddress = createFundedAddress(startBTC, startMSC)
        def investorAddress = createFundedAddress(startBTC, startMSC)
        def currencyMSC = CurrencyID.MSC

        when: "creating a new crowdsale with 1 IndivisibleSPT for 0.00000001 MSC"
        def crowdsaleTxid = sendrawtx_MP(issuerAddress, rawTx)
        generateBlock()

        then: "the crowdsale is active"
        def crowdsaleTx = getTransactionMP(crowdsaleTxid)
        crowdsaleTx.confirmations == 1
        crowdsaleTx.valid == true
        def propertyId = new CurrencyID(crowdsaleTx.propertyid as Long)
        def crowdsaleInfo = getCrowdsale(propertyId)
        crowdsaleInfo.active == true

        when: "participant invests 1.00000000 MSC"
        def sendTxid = send_MP(investorAddress, issuerAddress, currencyMSC, 1.00000000)
        generateBlock()

        then: "the investor should get 100000000 token"
        def sendTx = getTransactionMP(sendTxid)
        sendTx.valid == true
        getbalance_MP(investorAddress, propertyId).balance == 100000000
    }

    def getCrowdsale(CurrencyID propertyId) {
        def crowdsaleInfoResponse = client.send("getcrowdsale_MP", [propertyId.longValue()])
        def crowdsaleInfo = crowdsaleInfoResponse.result as Map<String,Object>
        return crowdsaleInfo
    }

}
