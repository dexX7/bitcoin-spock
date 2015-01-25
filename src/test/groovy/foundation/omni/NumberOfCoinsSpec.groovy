package foundation.omni

import spock.lang.Specification
import spock.lang.Unroll

class NumberOfCoinsSpec extends Specification {

    @Unroll
    def "Is equal: (#value.class) #value) == (#toCompare.class) #toCompare"() {
        expect:
        value == toCompare

        where:
        value                 | toCompare
        new BigDecimal("1")   | new BigDecimal("1")
        new BigDecimal("1")   | new Long("1")
        new BigDecimal("1")   | new Integer("1")
        new BigDecimal("1")   | new Short("1")
        new Long("1")         | new BigDecimal("1")
        new Integer("1")      | new BigDecimal("1")
        new Short("1")        | new BigDecimal("1")
        new Long("1")         | new Long("1")
        new Long("1")         | new Integer("1")
        new Long("1")         | new Short("1")
        new Integer("1")      | new Integer("1")
        new Integer("1")      | new Short("1")
        new Short("1")        | new Short("1")
    }

    @Unroll
    def "Is equal: #value == value [#value.class]"() {
        expect:
        value == value
        value == new NumberOfCoins(value)
        new NumberOfCoins(value) == value
        new NumberOfCoins(value) == new NumberOfCoins(value)

        where:
        value << [
                new NumberOfCoins("9223372036854775807"),
                new NumberOfCoins("9007199254740993"),
                new NumberOfCoins("2147483647"),
                new NumberOfCoins("16777217"),
                new NumberOfCoins("32767"),
                new NumberOfCoins("1"),
                new NumberOfCoins("0.00000001"),
                new BigDecimal("9223372036854775807.0"),
                new BigDecimal("9007199254740993.0"),
                new BigDecimal("2147483647.0"),
                new BigDecimal("16777217.0"),
                new BigDecimal("32767.0"),
                new BigDecimal("1.0"),
                new BigDecimal("0.00000001"),
                new Long("9223372036854775807"),
                new Long("9007199254740993"),
                new Long("2147483647"),
                new Long("16777217"),
                new Long("32767"),
                new Long("1"),
                new Integer("2147483647"),
                new Integer("16777217"),
                new Integer("32767"),
                new Integer("1"),
                new Short("32767"),
                new Short("1"),
                new Double("16777217.0"),
                new Double("32767.0"),
                new Double("1.0"),
                new Double("0.00000001"),
                new Float("32767.0"),
                new Float("1.0"),
                9223372036854775807.0,
                9007199254740993.0000,
                2147483647.0000000000,
                16777217.000000000000,
                32767.000000000000000,
                1.0000000000000000000,
                0.0000000100000000000,
                (long) 9223372036854775807L,
                (long) 9007199254740993L,
                (long) 2147483647L,
                (long) 16777217L,
                (long) 32767L,
                (long) 1L,
                (int) 2147483647,
                (int) 16777217,
                (int) 32767,
                (int) 1,
                (short) 32767,
                (short) 1,
                (double) 16777217.0,
                (double) 32767.0,
                (double) 1.0,
                (double) 0.00000001,
                (float) 32767.0,
                (float) 1.0,
        ]
    }

    @Unroll
    def "Can be compared: #lesser < #greater [#lesser.class, #greater.class]"() {
        expect:
        lesser != greater
        lesser  < greater
        greater > lesser

        lesser != new NumberOfCoins(greater)
        lesser  < new NumberOfCoins(greater)
        greater > new NumberOfCoins(lesser)

        new NumberOfCoins(lesser) != greater
        new NumberOfCoins(lesser)  < greater
        new NumberOfCoins(greater) > lesser

        where:
        lesser                          | greater
        9223372036854775806.99999999    | 9223372036854775807L
        9007199254740992L               | 9007199254740993.0
        new Integer("16777216")         | (double)16777217.0
        new Float("127")                | (short)128
    }

}
