/**
 * <h1>Arrowhead Kalix Encoding and Decoding Utilities</h1>
 * <p>
 * This package provides various facilities for dealing with the encoding and
 * decoding of Java objects. It most significantly contains the {@link
 * se.arkalix.codec.CodecType CodecType} and {@link se.arkalix.codec.MediaType
 * MediaType} classes, which are used to signify what codecs are used, as well
 * as the {@link se.arkalix.codec.Decoder Decoder} and {@link
 * se.arkalix.codec.Encodable Encodable} interfaces, which allow for
 * implementing classes to provide encoding and decoding routines. Furthermore,
 * the {@link se.arkalix.codec.MultiDecoder MultiDecoder} and {@link
 * se.arkalix.codec.MultiEncodable MultiEncodable} classes provides for having
 * certain classes implement several encoding and decoding routines.
 * <p>
 * While this package can be used as-is, it can take advantage of the Kalix
 * Data Transmission Object (DTO) utilities and processor, which can be used to
 * generate DTOs from specifications. Please refer to
 * <a href="https://arkalix.se">https://arkalix.se</a> for more details about
 * the {@code se.arkalix.dto} package, which provides these facilities.
 */
package se.arkalix.codec;