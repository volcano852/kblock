package com.fabien.kblock.domain.model

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.*



class Transaction(val publicKeySender: PublicKey, val publicKeyReceiver: PublicKey, val amount: Double) {
    companion object {
        fun sign(transaction: Transaction, privateKey: PrivateKey): ByteArray {
            //TODO: Need to integrate random element to avoid double spend ?
            val signatureInst = Signature.getInstance("SHA256withRSA")
            signatureInst.initSign(privateKey)
            signatureInst.update(transaction.toJsonByteArray())
            return signatureInst.sign()
        }

        fun fromFile(filename: String): Transaction {
            val node = jacksonObjectMapper().readTree(File(filename))

            val sender = Base64.getDecoder().decode(node.get("sender").textValue())
            val senderKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(sender))

            val receiver = Base64.getDecoder().decode(node.get("receiver").textValue())
            val receiverKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(receiver))

            val amount = node.get("amount").doubleValue()

            return Transaction(senderKey,receiverKey,amount)
        }
    }

    fun toJsonNode(): ObjectNode {
        val node = JsonNodeFactory.instance.objectNode()
        node.put("sender", Base64.getEncoder().encodeToString(publicKeySender.encoded))
        node.put("receiver", Base64.getEncoder().encodeToString(publicKeyReceiver.encoded))
        node.put("amount", amount)
        return node
    }

    fun toJsonByteArray(): ByteArray {
        return jacksonObjectMapper().writeValueAsBytes(toJsonNode())
    }

    override fun toString(): String {
        return jacksonObjectMapper().writeValueAsString(toJsonNode())
    }
}

class SignedTransaction(val transaction: Transaction, val signature: ByteArray) {

    fun verifySignature(): Boolean {
        val signatureInst = Signature.getInstance("SHA256withRSA")
        signatureInst.initVerify(transaction.publicKeySender)
        signatureInst.update(transaction.toJsonByteArray())
        return signatureInst.verify(signature)
    }

    fun toJsonNode(): ObjectNode {
        val signatureBase64 = Base64.getEncoder().encodeToString(signature)
        return transaction.toJsonNode().put("signature", signatureBase64)
    }

    override fun toString(): String {
        return jacksonObjectMapper().writeValueAsString(toJsonNode())
    }
}

class Transactions {
    private val transactions = mutableListOf<SignedTransaction>()

    fun size() = transactions.size

    fun add(transaction: SignedTransaction): Boolean {
        if (!transaction.verifySignature())
            throw IllegalArgumentException("$transaction signature could not be verified")

        return transactions.add(transaction)
    }

    fun addAll(transactions: Transactions) {
        this.transactions.addAll(transactions.transactions)
    }

    fun toJsonNode() : ArrayNode {
        val node = JsonNodeFactory.instance.arrayNode()
        transactions.map { node.add(it.toJsonNode()) }
        return node
    }

    override fun toString(): String {
        return jacksonObjectMapper().writeValueAsString(toJsonNode())
    }
}

class RewardTransaction(val publicKeyReceiver: PublicKey, val amount: Double) {
    fun toJsonNode() : ObjectNode {
        val node = JsonNodeFactory.instance.objectNode()
        node.put("receiver",Base64.getEncoder().encodeToString(publicKeyReceiver.encoded))
            .put("amount",amount)
        return node
    }

    override fun toString(): String {
        return jacksonObjectMapper().writeValueAsString(toJsonNode())
    }
}
