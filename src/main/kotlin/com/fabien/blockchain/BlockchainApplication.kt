package com.fabien.blockchain

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.hash.Funnel
import com.google.common.hash.Hashing
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom


//@SpringBootApplication
//class BlockchainApplication

/*
                        ### NOTES ###
1. What is the current bitcoin leading zeros complexity ?
2. Is adding one zero of complexity really double up the time in bitcoin ? (difficulty -> avg hashes : 2 -> 7,3 -> 15, 4->32, 5->65)
3. my laptop hash capacity: 127960366 hashes in 66 sec. 1_938_793 hash/sec ~ 1.9MHash/sec
4. What is the timestamp protocol used in bitcoin ?
5. Is transaction in blockchain only about user give to another user (owing/giving money)
6. Is the minor/contributor put in the block ? Do we keep track of the minor in the hash (and is it being hashed?)
*/


object Blockchain {
    const val proofDifficulty = 6

    private val chain = mutableListOf<Block>()

    val transactions = mutableListOf<SignedTransaction>()

    fun addTransaction(transaction: SignedTransaction): Boolean {
        if (!transaction.verifySignature())
            return false

        return transactions.add(transaction)
    }

    data class MiningResult(val guess: Long, val counter: Long)

    fun mineTransactions(contributor: PublicKey, counterStatus: Int = 0, displayStatus: ((counter: Long) -> Unit) = {}): MiningResult {
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

    fun hashBlock(contributor: PublicKey, proof: Long): ByteArray {
        val transactionsFunnel = Funnel<List<SignedTransaction>> { transactions, sink ->
            for (t in transactions) {
                sink
                        ?.putBytes(t?.publicKeySender.encoded)
                        ?.putBytes(t?.pubicKeyReceiver.encoded)
                        ?.putDouble(t?.amount!!)
                        ?.putBytes(t?.signature)
            }
        }
        return Hashing.sha256().newHasher()
                .putObject(transactions, transactionsFunnel)
                .putBytes(contributor.encoded)
                .putLong(proof)
//                .putBytes(chain[chain.lastIndex].previousHash) // TODO:Check if previous hash is part of hashing (actually the whole block)
                .hash().asBytes()
    }

    fun addBlock(index: Long, contributor: PublicKey, proof: Long): Boolean {
        val hash = hashBlock(contributor, proof)
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

open class Transaction(val publicKeySender: PublicKey, val pubicKeyReceiver: PublicKey, val amount: Double) {
    fun toJsonByteArray() : ByteArray {
        val mapper = jacksonObjectMapper()
        val node = JsonNodeFactory.instance.objectNode()
        node.put("sender",publicKeySender.encoded)
        node.put("receiver",pubicKeyReceiver.encoded)
        node.put("amount",amount)
        return mapper.writeValueAsBytes(node)
    }
}

fun signTransaction(transaction: Transaction, privateKey: PrivateKey, random: Long): ByteArray {
    val signature = Signature.getInstance("SHA256withRSA")
    signature.initSign(privateKey)
    signature.update(transaction.toJsonByteArray())
    return signature.sign()
}

class SignedTransaction(transaction: Transaction, val signature: ByteArray,val random: Long) : Transaction(transaction.publicKeySender, transaction.pubicKeyReceiver, transaction.amount) {
    fun verifySignature(): Boolean {
        val signatureInst = Signature.getInstance("SHA256withRSA")
        signatureInst.initVerify(this.publicKeySender)
        signatureInst.update(this as ByteArray)
        return signatureInst.verify(this.signature)
    }
}

class Block(val index: Long,
            val transactions: List<Transaction>,
            val contributor: PublicKey, // TODO: check if not in bitcoin blockchain ?
            val proof: Long,
            val previousHash: ByteArray) {

    val timestamp: LocalDateTime = LocalDateTime.now()
}

fun ofUnsigned(int: Int): Byte {
    return if (int > Byte.MAX_VALUE) {
        (Byte.MIN_VALUE + (int and 0b0111_1111)).toByte()
    } else
        int.toByte()
}