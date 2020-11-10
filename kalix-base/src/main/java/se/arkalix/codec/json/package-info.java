/**
 * <h1>JSON Value Types</h1>
 * This package contains classes useful for manually constructing JSON values.
 * The {@link se.arkalix.codec.json.JsonArray JsonArray} and {@link
 * se.arkalix.codec.json.JsonObject JsonObject} classes may, in particular,
 * be used as-is as {@link se.arkalix.dto DTOs}. The other classes may be used
 * as members in {@link se.arkalix.dto DTO interfaces} that only need to ever
 * deal with JSON input.
 * <p>
 * The following is an example meant to illustrate how to construct the JSON
 * object {@code `{"x": 1}`}:
 * <pre>
 *     final var object = new JsonObject(
 *         new JsonPair("x", new JsonNumber(1))
 * </pre>
 *
 * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
 */
package se.arkalix.codec.json;