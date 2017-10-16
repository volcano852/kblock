package com.fabien.kblock.domain.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.nio.charset.Charset
import java.security.PublicKey
import java.util.concurrent.ThreadLocalRandom

class KBlock(genesisReceiver: PublicKey, message: String, val difficulty: Int) {
    val chain = Blocks()

    var transactions : Transactions = Transactions()

    init {
        val messageBytes = message.toByteArray(Charset.defaultCharset())
        val (block, _) = mine(genesisReceiver, previousHash = messageBytes)
        add(block)
    }

    fun toJsonNode() : JsonNode {
        val node = JsonNodeFactory.instance.objectNode()
        node["chain"] = chain.toJsonNode()
        node["transactions"] = transactions.toJsonNode()
        return node
    }

    override fun toString(): String {
        return jacksonObjectMapper().writeValueAsString(toJsonNode())
    }

    fun add(block: Block) {
        chain.add(block,difficulty)
        transactions = Transactions()
    }

    data class MiningResult(val block: Block, val counter: Long)

    //TODO: Should be in the service ? This is object service but this is used at init phase to hash genesis
    fun mine(miner: PublicKey, previousHash : ByteArray = chain.lastBlock().hash(), counterStatus: Int = 0, displayStatus: ((counter: Long) -> Unit) = {}): MiningResult {
        var counter: Long = 0
        var guess: Long
        var block: Block

        do {
            guess = ThreadLocalRandom.current().nextLong()
            val reward = RewardTransaction(miner, 1.0)
            block = Block(chain.lastBlockIndex(), previousHash, transactions, reward, guess)
            val hash = block.hash()
            counter++
            if ((counterStatus > 0) && (counter % counterStatus == 0L)) {
                displayStatus(counter)
            }
        } while (hash.leadingZeroBits() <= difficulty)

        return MiningResult(block, counter)
    }
}

// TODO: Would be in UByte if I define one or leave it in extension of ByteArray but UByte extends Byte ? Or Number ?
fun ByteArray.leadingZeroBits(): Int {

    fun leadingZeroBits(byte: Byte): Int {
        var counter = 0
        for (i in 7 downTo 0) {
            if (byte.toInt() ushr i == 0) counter += 1
            else break
        }
        return counter
    }

    var arrayZerosCounter = 0
    for (byte in this) {
        val byteZerosCounter = leadingZeroBits(byte)
        arrayZerosCounter += byteZerosCounter
        if (byteZerosCounter < 8) {
            break
        }
    }
    return arrayZerosCounter
}

//TODO : Use Ubyte library ? Or define UByte class ? Would need to define mutableUByteListOf()
fun ofUnsigned(int: Int): Byte {
    return if (int > Byte.MAX_VALUE) {
        (Byte.MIN_VALUE + (int and 0b0111_1111)).toByte()
    } else
        int.toByte()
}