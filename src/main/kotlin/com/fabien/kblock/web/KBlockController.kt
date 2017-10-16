package com.fabien.kblock.web

import com.fabien.kblock.domain.model.SignedTransaction
import com.fabien.kblock.domain.model.Transaction
import com.fabien.kblock.domain.services.KBlockService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*

data class SignedTransactionDto(val sender: String, val receiver: String, val amount: Double, val signature: String) {
    fun toSignedTransaction(): SignedTransaction {
        val senderKey = X509EncodedKeySpec(Base64.getDecoder().decode(sender))
        val receiverKey = X509EncodedKeySpec(Base64.getDecoder().decode(receiver))
        val signatureBytes = Base64.getDecoder().decode(signature)
        val keyFactory = KeyFactory.getInstance("RSA")
        return SignedTransaction(Transaction(keyFactory.generatePublic(senderKey), keyFactory.generatePublic(receiverKey), amount), signatureBytes)
    }
}

@RestController
@RequestMapping("/transactions")
class TransactionController(@Autowired val kblockService: KBlockService) {

    @GetMapping()
    fun getTransactions(): String {
        return kblockService.kblock.toString()
    }

    @PostMapping()
    fun addTransaction(@RequestBody signedTransactionDto: SignedTransactionDto): Boolean {
        return kblockService.kblock.transactions.add(signedTransactionDto.toSignedTransaction())
    }
}

@RestController
@RequestMapping("/chain")
class ChainController(@Autowired val kblockService: KBlockService) {
    @GetMapping()
    fun getBlockchain(): String {
        return kblockService.kblock.toString()
    }
}