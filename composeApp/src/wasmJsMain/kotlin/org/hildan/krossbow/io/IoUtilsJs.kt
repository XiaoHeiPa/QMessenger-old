package org.hildan.krossbow.io

import kotlinx.io.bytestring.*
import kotlinx.io.bytestring.unsafe.*
import org.khronos.webgl.*

/**
 * Creates a new [ArrayBuffer] containing the data copied from this [ByteString].
 */
@InternalKrossbowIoApi
fun ByteString.toArrayBuffer(): ArrayBuffer = toInt8Array().buffer

private fun ByteString.toInt8Array(): Int8Array {
    // Convert ByteArray to ArrayBuffer
    val arrayBuffer = ArrayBuffer(toArrayOfBytes().size)
    val uint8Array = Uint8Array(arrayBuffer)

    // Copy bytes to Uint8Array
    toArrayOfBytes().forEachIndexed { index, byte ->
        uint8Array[index] = byte // Convert byte to unsigned
    }

    // Create Int8Array from ArrayBuffer
    return Int8Array(arrayBuffer)
}

private fun ByteString.toArrayOfBytes() = Array(size) { this[it] }

/**
 * Creates a new [ByteString] containing the data copied from this [ArrayBuffer].
 */
@OptIn(UnsafeByteStringApi::class)
@InternalKrossbowIoApi
fun ArrayBuffer.toByteString(): ByteString = toByteArray().asByteString()

private fun ArrayBuffer.toByteArray(): ByteArray = Int8Array(this).toByteArray()

private fun Int8Array.toByteArray() = ByteArray(length) { this[it] }
