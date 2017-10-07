package com.fabien.blockchain

import com.google.common.hash.Funnel
import com.google.common.hash.Hashing
import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom


//@SpringBootApplication
//class BlockchainApplication

/*
                        ### NOTES ###
1. What is the current bitcoin leading zeros complexity ?
2. Is adding one zero of complexity really double up the time in bitcoin ? (difficulty -> avg hashes : 2 -> 7,3 -> 15, 4->32, 5->65)
3. my laptop hash capacity: 127960366 hashes in 66 sec. 1_938_793 hash/sec ~ 1.9MHash/sec
*/


object Blockchain {
    const val proofDifficulty = 6

    private val chain = mutableListOf<Block>()

    val transactions = mutableListOf<Transaction>()

    fun addTransaction(transaction: Transaction): Boolean {
        return transactions.add(transaction)
    }

    data class MiningResult(val guess:Long, val counter:Long)

    fun mineTransactions(contributor: ByteArray, counterStatus: Int = 0, displayStatus: ((counter: Long) -> Unit) = {}): MiningResult {
        var counter: Long = 0
        var guess: Long
        do {
            guess = ThreadLocalRandom.current().nextLong()
            val hash = Blockchain.hashBlock(contributor, guess)
            counter++
            if ((counterStatus > 0) && (counter % counterStatus == 0L)) {
                displayStatus(counter)
            }
        } while (Blockchain.leadingZeroBits(hash) <= Blockchain.proofDifficulty)

        return MiningResult(guess, counter)
    }

    fun hashBlock(contributor: ByteArray, proof: Long): ByteArray {
        val transactionsFunnel = Funnel<List<Transaction>> { transactions, sink ->
            for (t in transactions) {
                sink
                        ?.putBytes(t?.publicKeySender)
                        ?.putBytes(t?.pubicKeyReceiver)
                        ?.putDouble(t?.amount!!)
            }
        }
        return Hashing.sha256().newHasher()
                .putObject(transactions, transactionsFunnel)
                .putBytes(contributor)
                .putLong(proof)
//                .putBytes(chain[chain.lastIndex].previousHash) // TODO:Check if previous hash is part of hashing (actually the whole block)
                .hash().asBytes()
    }

    fun addBlock(index: Long, contributor: ByteArray, proof: Long): Boolean {
        val hash = hashBlock(contributor,proof)
        if (leadingZeroBits(hash) < proofDifficulty) {
            return false
        }

        val block = Block(index, transactions, contributor, proof, chain[chain.lastIndex].previousHash)
        chain.add(block)
        transactions.clear()
        return true
    }

    fun leadingZeroBits(byteArray: ByteArray): Int {

        fun leadingZeroBits(byte: Byte): Int {
            var counter = 0
            for (i in 7 downTo 0) {
                if (byte.toInt() ushr i == 0) counter += 1
                else break
            }
            return counter
        }

        var arrayZerosCounter = 0
        for (byte in byteArray) {
            val byteZerosCounter = leadingZeroBits(byte)
            arrayZerosCounter += byteZerosCounter
            if (byteZerosCounter < 8) {
                break
            }
        }
        return arrayZerosCounter
    }
}

class Transaction(val publicKeySender: ByteArray, val pubicKeyReceiver: ByteArray, val amount: Double)

class Block(val index: Long,
            val transactions: List<Transaction>,
            val contributor: ByteArray, // TODO: check if not in bitcoin blockchain ?
            val proof: Long,
            val previousHash: ByteArray) {

    val timestamp: LocalDateTime = LocalDateTime.now()
}

class RSAKeys(val publicKey: ByteArray, private val privateKey: ByteArray)

fun MutableList<Int>.swap(index1: Int, index2: Int) {
    val tmp = this[index1] // 'this' corresponds to the list this[index1] = this[index2]
    this[index2] = tmp
}


fun ofUnsigned(int: Int): Byte {
    return if (int > Byte.MAX_VALUE) {
        (Byte.MIN_VALUE + (int and 0b0111_1111)).toByte()
    }
    else
        int.toByte()
}