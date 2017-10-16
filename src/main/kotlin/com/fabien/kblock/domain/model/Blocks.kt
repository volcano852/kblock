package com.fabien.kblock.domain.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.hash.Hashing
import java.util.*

class Block(val index: Int, val previousHash: ByteArray, transactions: Transactions, val rewardTransaction: RewardTransaction, val proof: Long) {
    val transactions : Transactions = Transactions()

    init {
        this.transactions.addAll(transactions)
    }

    fun toJsonNode() : ObjectNode {
        val node = JsonNodeFactory.instance.objectNode()
        node.put("index", index)
        node.put("previousHash", Base64.getEncoder().encodeToString(previousHash))
        node["transactions"] = transactions.toJsonNode()
        node["reward"] = rewardTransaction.toJsonNode()
        node.put("proof", proof)
        return node
    }

    private fun toJsonByteArray(): ByteArray {
        return jacksonObjectMapper().writeValueAsBytes(toJsonNode())
    }

    fun hash(): ByteArray {
        return Hashing.sha256().newHasher().putBytes(toJsonByteArray()).hash().asBytes()
    }

    override fun toString(): String {
        return jacksonObjectMapper().writeValueAsString(toJsonNode())
    }
}

class Blocks {
    private val blocks = mutableListOf<Block>()

    fun add(block: Block, difficulty: Int): Boolean {
        val hash = block.hash()
        if (hash.leadingZeroBits() < difficulty) {
            return false
        }

        return blocks.add(block)
    }

    fun lastBlockIndex() = blocks.lastIndex

    fun lastBlock() = blocks.last()

    fun toJsonNode() : JsonNode {
        val node = JsonNodeFactory.instance.arrayNode()
        blocks.map { node.add(it.toJsonNode()) }
        return node
    }

    override fun toString(): String {
        return jacksonObjectMapper().writeValueAsString(toJsonNode())
    }
}
