package com.fabien.kblock.interfaces.cli

import com.fabien.kblock.domain.services.KBlockService
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.security.PublicKey
import kotlin.system.exitProcess

class KBlockCLI : CommandLine(listOf(VerifyCommand(KBlockService()),SignCommand(KBlockService()))) {
    override val name = "kblock"
    override val description = "sign or verify a KBlock transaction"
}

class SignCommand(val kblockService: KBlockService) : Command() {
    override val name = "sign"
    override val description = "sign a transaction with a private key"
    override val options = listOf(TransactionFileNameOption,PrivateKeyFileNameOption)

    object TransactionFileNameOption : Option {
        override val name = "transaction"
        override val shortName = "t"
        override val description = "unsigned transaction file"
        override lateinit var value: String
    }

    object PrivateKeyFileNameOption : Option {
        override val name = "private"
        override val shortName = "p"
        override val description = "public key file"
        override lateinit var value: String
    }

    object OutputFileNameOption : Option {
        override val name = "output"
        override val shortName = "o"
        override val description = "output signed transaction file"
        override lateinit var value: String
    }

    override fun execute() {
        val transaction = kblockService.readTransactionFromFile(TransactionFileNameOption.value)
        val privateKey = kblockService.readPrivateKeyFromFile(PrivateKeyFileNameOption.value)
        Files.write(Paths.get(OutputFileNameOption.value),kblockService.sign(transaction.toJsonByteArray(),privateKey))
    }
}

class VerifyCommand(val kblockService: KBlockService) : Command() {
    override val name = "verify"
    override val description = "verify the authenticity of a signed transaction"
    override val options = listOf(SignedTransactionFileNameOption)

    object SignedTransactionFileNameOption : Option {
        override val name = "transaction"
        override val description = "signed transaction file name"
        override val shortName = "t"
        override lateinit var value: String
    }

    data class Transaction(val publicKeySender: PublicKey, val publicKeyReceiver: PublicKey, val amount: Double)
    data class SignedTransaction(val transaction: Transaction, val signature: ByteArray) {
        companion object {
            fun fromLines() : SignedTransaction {
                TODO()
            }
        }
    }

    override fun execute() {
        val signedTransactionLines = Files.readAllLines(File(SignedTransactionFileNameOption.value).toPath())
        val signedTransaction = SignedTransaction.fromLines()
//        println(verify(signedTransaction as ByteArray, signedTransaction.transaction.publicKeySender, signedTransaction.signature))
    }
}

fun main(args : Array<String>) {
    val kblockCLI = KBlockCLI()

    try {
        kblockCLI.execute(args.toList())
    }
    catch (e : Exception) {
        e.printStackTrace()
        exitProcess(1)
    }
}