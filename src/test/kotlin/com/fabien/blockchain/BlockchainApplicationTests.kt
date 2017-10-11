package com.fabien.blockchain

import com.google.common.base.Stopwatch
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.concurrent.TimeUnit

//@RunWith(SpringRunner::class)
//@SpringBootTest
class BlockchainApplicationTests {

    val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
    var fabien: KeyPair = keyPairGenerator.genKeyPair()
    var virginie: KeyPair = keyPairGenerator.genKeyPair()
    lateinit var t1Signed: SignedTransaction
    lateinit var t2Signed: SignedTransaction

    @Before
    fun generateKeysPairsAndTransactions() {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        fabien = keyPairGenerator.genKeyPair()
        virginie = keyPairGenerator.genKeyPair()

        val t1 = Transaction(fabien.public, virginie.public, 100.0)
        val t1Random = 1L
        t1Signed = SignedTransaction(t1, signTransaction(t1, fabien.private, t1Random), t1Random)

        val t2 = Transaction(virginie.public, fabien.public, 25.0)
        val t2Random = 2L
        t2Signed = SignedTransaction(t1, signTransaction(t2, virginie.private, t2Random), t2Random)
    }

    @Test
    fun test_leading_zeros() {
        assertEquals(0, Blockchain.leadingZeroBits(byteArrayOf(ofUnsigned(0b1111_1111), ofUnsigned(0b0000_1111))))
        assertEquals(4, Blockchain.leadingZeroBits(byteArrayOf(ofUnsigned(0b0000_1111), ofUnsigned(0b0000_0000))))
        assertEquals(8, Blockchain.leadingZeroBits(byteArrayOf(ofUnsigned(0b0000_0000), ofUnsigned(0b1111_1111))))
        assertEquals(12, Blockchain.leadingZeroBits(byteArrayOf(ofUnsigned(0b0000_0000), ofUnsigned(0b0000_1111))))
        assertEquals(16, Blockchain.leadingZeroBits(byteArrayOf(ofUnsigned(0b0000_0000), ofUnsigned(0b0000_0000))))
    }

    @Test
    fun test_correct_guess_results_in_leading_zeros() {
        Blockchain.addTransaction(t1Signed)
        Blockchain.addTransaction(t2Signed)
        val hash = Blockchain.hashBlock(fabien.public, 1612657619380781199)
        val actualZeros = Blockchain.leadingZeroBits(hash)
        assertEquals(6, actualZeros)
    }

    @Test
    @Ignore
    fun measure_hashing_performance() {
        val sw = Stopwatch.createStarted()

        var totalCounter: Long = 0
        for (i in 1..500_000) {
            val (_, counter) = Blockchain.mineTransactions(fabien.public, 1_000, { _ -> print("#") })
            totalCounter += counter
        }
        println()
        sw.stop()

        val duration = sw.elapsed(TimeUnit.SECONDS)
        println("$totalCounter hashes in $duration sec")
        println("${totalCounter / duration / 1_000_000.0} Mhashes/sec")
        println("Average guess ${totalCounter / 1000}")
    }
}