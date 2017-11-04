// Copyright (c) 2017. Allen Frise

import com.google.bitcoin.core.*
import com.google.bitcoin.params.MainNetParams
import com.google.gson.JsonParser
import org.spongycastle.util.encoders.Hex
import java.math.BigInteger
import java.security.MessageDigest
import java.net.URL
import java.util.*

val apikey="your_blocktrail_api_key_here"

fun main(args: Array<String>){
    var i = "2DF79856217FF6D2EACFC7887F1E6A8AE9C279DE940932A398EC994F962952ED" //INITIAL ADDRESS SEED, TODO: RNG maybe?
    while (true){
        val bal = getBalance(i)
        print("\r${Base58.encode(genSecretKey(i).hexStringToByteArray())} :: ${genPublicKey(i)} :: $bal")
        if (bal>0) print("\n")
        Thread.sleep(200)
        i = (BigInteger(i,16)+BigInteger.ONE).toString(16)
    }
}
fun genSecretKey(i: String): String = ("80" + i + (MessageDigest.getInstance("SHA-256").digest(MessageDigest.getInstance("SHA-256").digest(("80" + i).hexStringToByteArray())).toHex().substring(0,8)))
fun genPublicKey(i: String): String =  Address(MainNetParams.get() , Hex.decode(ECKey(BigInteger(genSecretKey(i),16)).pubKeyHash.toHex())).toString()
fun getBalance(i: String): Double {
    try { Scanner(URL("https://api.blocktrail.com/v1/btc/address/${genPublicKey(i)}?api_key=$apikey").openStream()).use({ scanner ->
        return JsonParser().parse(scanner.useDelimiter("\\A").next()).asJsonObject.get("balance").asDouble })
    } catch (e: Exception){ return -1.0 }
}
fun ByteArray.toHex() : String{
    val result = StringBuffer()
    forEach {
        val octet = it.toInt()
        val firstIndex = (octet and 0xF0).ushr(4)
        val secondIndex = octet and 0x0F
        result.append("0123456789ABCDEF".toCharArray()[firstIndex])
        result.append("0123456789ABCDEF".toCharArray()[secondIndex])
    }
    return result.toString()
}
fun String.hexStringToByteArray() : ByteArray {
    val result = ByteArray(length / 2)
    for (i in 0 until length step 2) {
        val firstIndex = "0123456789ABCDEF".toCharArray().indexOf(this[i])
        val secondIndex = "0123456789ABCDEF".toCharArray().indexOf(this[i + 1])
        val octet = firstIndex.shl(4).or(secondIndex)
        result[i.shr(1)] = octet.toByte()
    }
    return result
}
