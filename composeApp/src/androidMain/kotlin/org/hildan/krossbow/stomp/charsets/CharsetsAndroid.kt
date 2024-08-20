package org.hildan.krossbow.stomp.charsets

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.unsafe.UnsafeByteStringApi
import org.hildan.krossbow.io.InternalKrossbowIoApi
import org.hildan.krossbow.io.asByteString
import org.hildan.krossbow.io.unsafeBackingByteArray

@OptIn(UnsafeByteStringApi::class, InternalKrossbowIoApi::class)
@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
internal actual fun String.encodeToByteString(charset: Charset): ByteString =
    (this as java.lang.String).getBytes(charset.toJavaCharset()).asByteString()

@OptIn(UnsafeByteStringApi::class, InternalKrossbowIoApi::class)
internal actual fun ByteString.decodeToString(charset: Charset): String =
    String(unsafeBackingByteArray(), charset.toJavaCharset())

private fun Charset.toJavaCharset(): java.nio.charset.Charset = java.nio.charset.Charset.forName(name)