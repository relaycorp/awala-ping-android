package tech.relaycorp.ping.awala

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset
import javax.inject.Inject

class PingSerialization
@Inject constructor() {

    fun serialize(
        pingId: String,
        pdaPathSerialized: ByteArray,
        internetAddress: String? = null,
    ): ByteArray {
        val pingJSON = JSONObject()
        pingJSON.put("id", pingId)
        pingJSON.put("pda_path", base64Encode(pdaPathSerialized))
        internetAddress?.let {
            pingJSON.put("endpoint_internet_address", it)
        }
        val pingJSONString = pingJSON.toString()
        return pingJSONString.toByteArray()
    }

    private fun base64Encode(input: ByteArray): String =
        Base64.encodeToString(input, Base64.DEFAULT)

    fun extractPingIdFromPong(pongMessageSerialized: ByteArray): String {
        return pongMessageSerialized.toString(Charset.defaultCharset())
    }
}
