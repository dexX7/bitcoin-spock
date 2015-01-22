package foundation.omni.test.rpc.sto

import com.google.bitcoin.core.Address
import com.msgilligan.bitcoin.BTC
import foundation.omni.CurrencyID
import foundation.omni.PropertyType

/**
 * Data driven tests for the "send to owners" transaction type, whereby a raw transaction
 * is created and broadcasted to bypass the RPC interface
 */
class MSCSendToOwnersTestPlanRawSpec extends MSCSendToOwnersTestPlanSpec {

    @Override
    def executeSendToOwners(Address actorAddress, CurrencyID currencyId, PropertyType propertyType, def amount) {
        BigDecimal numberOfTokens = amount

        if (propertyType == PropertyType.DIVISIBLE) {
            numberOfTokens = BTC.btcToSatoshis(numberOfTokens)
        }

        def rawTxHex = createSendToOwnersHex(currencyId, numberOfTokens.longValue());
        def txid = sendrawtx_MP(actorAddress, rawTxHex)
        return txid
    }

    @Override
    def maybeSkipInvalidationTests(Boolean expectedValidity) {
        // don't skip invalidation tests
    }

}
