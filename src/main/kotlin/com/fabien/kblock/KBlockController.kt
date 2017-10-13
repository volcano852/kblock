package com.fabien.kblock

import com.fabien.kblock.SignedTransactionDto.Companion.toSignedTransactionDto
import org.springframework.web.bind.annotation.*
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.time.LocalDateTime
import java.util.*

data class SignedTransactionDto(val sender:String,val receiver: String, val amount:Double, val signature : String, val random : Long) {

    companion object {
        fun toSignedTransactionDto(signedTransaction: SignedTransaction): SignedTransactionDto {
            val publicKeySenderBase64 = Base64.getEncoder().encodeToString(signedTransaction.publicKeyReceiver.encoded)
            val publicKeyReceiverBase64 = Base64.getEncoder().encodeToString(signedTransaction.publicKeySender.encoded)
            val signatureBase64 = Base64.getEncoder().encodeToString(signedTransaction.signature)
            return SignedTransactionDto(publicKeySenderBase64,publicKeyReceiverBase64,signedTransaction.amount,signatureBase64,signedTransaction.random)
        }
    }

    fun toSignedTransaction() : SignedTransaction {
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
        return Blockchain.addTransaction(signedTransactionDto.toSignedTransaction())
    }
}

class BlockDto(val index: Long,
            val transactions: List<SignedTransactionDto>,
            val contributor: String, // TODO: check if not in bitcoin blockchain ?
            val proof: Long,
            val previousHash: String,
            val timestamp: LocalDateTime ) {

    companion object {
        fun toBlockDto(block: Block) : BlockDto {
            val transactionsDto = block.transactions.map { toSignedTransactionDto(it) }
            val contributorBase64 = Base64.getEncoder().encodeToString(block.contributor.encoded)
            val previousHashBase64 = Base64.getEncoder().encodeToString(block.previousHash)
            return BlockDto(block.index, transactionsDto, contributorBase64, block.proof, previousHashBase64, block.timestamp)
        }
    }
}


@RestController
@RequestMapping("/chain")
class ChainController {
    @GetMapping()
    fun getBlockchain() : List<BlockDto> {
        return Blockchain.chain.map { BlockDto.toBlockDto(it) }
    }
}