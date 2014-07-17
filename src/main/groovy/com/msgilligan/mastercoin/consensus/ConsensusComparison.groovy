package com.msgilligan.mastercoin.consensus

import org.mastercoin.Mastercoin

/**
 * User: sean
 * Date: 7/12/14
 * Time: 3:40 PM
 */
class ConsensusComparison {
    ConsensusSnapshot c1
    ConsensusSnapshot c2

    List<String> addressIntersection() {
        List<String> intersect = c1.entries.keySet().intersect(c2.entries.keySet()).toList()
        intersect.remove(Mastercoin.ExodusAddress)
        return intersect
    }

    List<String> extraAddressesInFirst() {
        List<String> extra =  (c1.entries.keySet() - c2.entries.keySet()).toList()
        return extra
    }

    List<String> extraAddressesInSecond() {
        List<String> extra =  (c2.entries.keySet() - c1.entries.keySet()).toList()
        return extra
    }
}
