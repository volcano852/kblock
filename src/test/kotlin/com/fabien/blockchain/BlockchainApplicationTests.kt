package com.fabien.blockchain

import com.google.common.base.Stopwatch
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

//@RunWith(SpringRunner::class)
//@SpringBootTest
class BlockchainApplicationTests {

	@Test
	fun test_leading_zeros() {
		assertEquals(0,Blockchain.leadingZeroBits(byteArrayOf(ofUnsigned(0b1111_1111), ofUnsigned(0b0000_1111))))
		assertEquals(4,Blockchain.leadingZeroBits(byteArrayOf(ofUnsigned(0b0000_1111), ofUnsigned(0b0000_0000))))
		assertEquals(8,Blockchain.leadingZeroBits(byteArrayOf(ofUnsigned(0b0000_0000), ofUnsigned(0b1111_1111))))
		assertEquals(12,Blockchain.leadingZeroBits(byteArrayOf(ofUnsigned(0b0000_0000), ofUnsigned(0b0000_1111))))
		assertEquals(16,Blockchain.leadingZeroBits(byteArrayOf(ofUnsigned(0b0000_0000), ofUnsigned(0b0000_0000))))
	}

    @Test
    fun test_correct_guess_results_in_leading_zeros() {
        val fabien = RSAKeys(byteArrayOf(0x7F,0x7F), byteArrayOf(0x70,0x70))
        val virginie = RSAKeys(byteArrayOf(0x6F,0x6F), byteArrayOf(0x60,0x60))

        val t1 = Transaction(fabien.publicKey, virginie.publicKey, 100.0)
        val t2 = Transaction(virginie.publicKey, fabien.publicKey, 25.0)

        Blockchain.addTransaction(t1)
        Blockchain.addTransaction(t2)
        val hash = Blockchain.hashBlock(fabien.publicKey,1612657619380781199)
        val actualZeros = Blockchain.leadingZeroBits(hash)
        assertEquals(6,actualZeros)
    }

	@Test
	fun measure_hashing_performance() {
        val fabien = RSAKeys(byteArrayOf(0x7F,0x7F), byteArrayOf(0x70,0x70))
        val virginie = RSAKeys(byteArrayOf(0x6F,0x6F), byteArrayOf(0x60,0x60))

        val t1 = Transaction(fabien.publicKey, virginie.publicKey, 100.0)
        val t2 = Transaction(virginie.publicKey, fabien.publicKey, 25.0)

        Blockchain.addTransaction(t1)
        Blockchain.addTransaction(t2)

        val sw = Stopwatch.createStarted()

        var totalCounter :Long = 0
        for (i in 1..500_000) {
            val (_,counter) = Blockchain.mineTransactions(fabien.publicKey,1_000,{ _ -> print("#")})
            totalCounter += counter
        }
        println()
        sw.stop()

        val duration = sw.elapsed(TimeUnit.SECONDS)
        println("$totalCounter hashes in $duration sec")
        println("${totalCounter / duration /1_000_000.0} Mhashes/sec")
        println("Average guess ${totalCounter / 1000}")
	}

}