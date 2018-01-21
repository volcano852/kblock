package com.fabien.kblock

import com.fabien.kblock.domain.model.*
import com.google.common.base.Stopwatch
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.concurrent.TimeUnit


//@RunWith(SpringRunner::class)
//@SpringBootTest
class KBlockAppTest {
    lateinit var fabien: KeyPair
    lateinit var virginie: KeyPair
    lateinit var t1Signed: SignedTransaction
    lateinit var t2Signed: SignedTransaction
    lateinit var kblock : KBlock

    @Before
    fun `create keys, transactions and kblock object`() {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        fabien = keyPairGenerator.genKeyPair()
        virginie = keyPairGenerator.genKeyPair()

        kblock = KBlock(fabien.public, "KBlock Test", 3)

        val t1 = Transaction(fabien.public, virginie.public, 100.0)
        t1Signed = SignedTransaction(t1, Transaction.sign(t1, fabien.private))
        kblock.transactions.add(t1Signed)

        val t2 = Transaction(virginie.public, fabien.public, 25.0)
        t2Signed = SignedTransaction(t2, Transaction.sign(t2, virginie.private))
        kblock.transactions.add(t2Signed)
    }

    @Test
    fun `test unsigned transaction`() {
//        val publicKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(bytes))
    }

    @Test
    fun `Test leadingZerosBits functions works correctly`() {
        assertEquals(0, byteArrayOf(ofUnsigned(0b1111_1111), ofUnsigned(0b0000_1111)).leadingZeroBits())
        assertEquals(4, byteArrayOf(ofUnsigned(0b0000_1111), ofUnsigned(0b0000_0000)).leadingZeroBits())
        assertEquals(8, byteArrayOf(ofUnsigned(0b0000_0000), ofUnsigned(0b1111_1111)).leadingZeroBits())
        assertEquals(12, byteArrayOf(ofUnsigned(0b0000_0000), ofUnsigned(0b0000_1111)).leadingZeroBits())
        assertEquals(16, byteArrayOf(ofUnsigned(0b0000_0000), ofUnsigned(0b0000_0000)).leadingZeroBits())
    }

    @Test
    fun `Mining transactions should result in new block that can be added to the kblock chain`() {
        val (block,_)= kblock.mine(fabien.public,counterStatus = 100_000,displayStatus = { _ -> print("#")})
        kblock.add(block)
        assertThat(block.hash().leadingZeroBits(),greaterThanOrEqualTo(kblock.difficulty))
        assertThat(kblock.transactions.size(), Matchers.`is`(0))
        assertThat(kblock.chain.lastBlockIndex(),`is`(1))
        assertThat(kblock.chain.lastBlock().transactions.size(),`is`(2))
    }

    @Test
    @Ignore
    fun measure_hashing_performance() {
        val sw = Stopwatch.createStarted()

        var totalCounter: Long = 0
        for (i in 1..500_000) {
            val (_, counter) = kblock.mine(fabien.public, counterStatus = 1_000, displayStatus = { _ -> print("#") })
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