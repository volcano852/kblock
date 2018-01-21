package com.fabien.kblock.domain.services

import com.fabien.kblock.domain.model.KBlock
import com.fabien.kblock.domain.model.Transaction
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec


//fun signAndVerify() {
//    val private = readPrivateKeyFromFile("/Users/fabien/.ssh/id_rsa.p8")
//    val signature = sign("KBlock".toByteArray(Charset.defaultCharset()),private)
//    signature[0] = 0
//    val public = getPublic("/Users/fabien/.ssh/id_rsa.pub.p8")
//    val verified = verify("KBlock".toByteArray(),public,signature)
//    println(public.format)
//    println(public.algorithm)
//    println(verified)
//}

@Service
class KBlockService {

    lateinit var kblock : KBlock

    @Throws(Exception::class)
    fun readPrivateKeyFromFile(filename: String): PrivateKey {
        val keyBytes = Files.readAllBytes(File(filename).toPath())
        val spec = PKCS8EncodedKeySpec(keyBytes)

        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePrivate(spec)
    }

    @Throws(Exception::class)
    fun getPublic(filename: String): PublicKey {
        val keyBytes = Files.readAllBytes(File(filename).toPath())
        val spec = X509EncodedKeySpec(keyBytes)

        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePublic(spec)
    }

    fun sign(message: ByteArray, privateKey: PrivateKey): ByteArray {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(message)
        return signature.sign()
    }

    fun verify(message: ByteArray, publicKey: PublicKey, signature: ByteArray): Boolean {
        val signatureInst = Signature.getInstance("SHA256withRSA")
        signatureInst.initVerify(publicKey)
        signatureInst.update(message)
        return signatureInst.verify(signature)
    }

    fun readTransactionFromFile(filename: String): Transaction {
        val lines = Files.readAllLines(File(filename).toPath())
//        Transaction.fromLines(lines)
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}