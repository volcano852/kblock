package com.fabien.kblock

import org.springframework.web.bind.annotation.*
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*

data class SignedTransactionDto(val sender:String,val receiver: String, val amount:Double, val signature : String, val random : Long) {

    fun toTransaction() : SignedTransaction {
        val senderKey = X509EncodedKeySpec(Base64.getDecoder().decode(sender))
        val receiverKey = X509EncodedKeySpec(Base64.getDecoder().decode(receiver))
        val signatureBytes = Base64.getDecoder().decode(signature)

        val keyFactory = KeyFactory.getInstance("RSA")
        return SignedTransaction(keyFactory.generatePublic(senderKey),keyFactory.generatePublic(receiverKey),amount,signatureBytes,random)
    }
}

@RestController
@RequestMapping("/transactions")
class TransactionController{
    @GetMapping()
    fun getTransaction() : List<Transaction> {
        return Blockchain.transactions
    }

    @PostMapping()
    fun addTransaction(@RequestBody signedTransactionDto: SignedTransactionDto) : Boolean {
        return Blockchain.addTransaction(signedTransactionDto.toTransaction())
    }
}

@RestController
@RequestMapping("/chain")
class ChainController {
    @GetMapping()
    fun getBlockchain() : List<Block> {
        return Blockchain.chain
    }
}