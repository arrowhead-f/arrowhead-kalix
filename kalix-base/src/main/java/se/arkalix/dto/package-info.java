/**
 * <h1>Data Transfer Object Utilities</h1>
 * <p>
 * This package contains various utilities useful for constructing so-called
 * Data Transfer Objects (DTOs). In essence, a DTO is a well-defined set of
 * data that needs to be represented on an external medium, such as a hard
 * drive or a network wire. As such, a DTO must be readable and/or writable
 * from/to a particular machine-independent representation, or <i>encoding</i>.
 * <p>
 * While there are many libraries and other solutions for dealing with the
 * problem of encoding and decoding what practically amounts to Plain-Old Java
 * Objects (POJOs), this particular package distinguishes itself from many of
 * them by centering around the idea that as much as possible of the cost of
 * converting POJOs to and from their encoded forms should be paid at compile-
 * time rather than at runtime. For this reason this package depends on the
 * availability of the <i>kalix-processors</i> package, which concretely looks
 * up the annotations of this package and uses whatever is annotated as input
 * for generating the code required for reading and/or writing POJOs with
 * certain encodings.
 * <p>
 * Using the DTO code generation capabilities entails defining so-called,
 * <i>DTO interface types</i> and then using the DTO interface (1)
 * <i>classes</i> and (2) <i>builders</i> generated from those DTO interface
 * types.
 * <h2>DTO Interface Types</h2>
 * A DTO interface type is a plain Java interface that satisfies the following
 * constraints:
 * <ol>
 *     <li>It has no generic type parameters.</li>
 *     <li>Its name does not end with "Dto".</li>
 *     <li>It is annotated with either {@link se.arkalix.dto.DtoReadableAs
 *         &#64;DtoReadableAs} or {@link se.arkalix.dto.DtoWritableAs
 *         &#64;DtoWritableAs}, and whichever of those annotations are present
 *         are given at least one {@link se.arkalix.dto.DtoEncoding DtoEncoding}
 *         as arguments.</li>
 *     <li>It contains only static, default and getter methods, where a getter
 *         is a method that takes no arguments and returns a type that is not
 *         {@code void}.</li>
 *     <li>The return type of each getter method belongs to one of the
 *         following categories:
 *         <ol type="a">
 *             <li>Primitives, such as {@code int} or {@code boolean}.</li>
 *             <li>Boxed primitives, such as {@code Integer} or {@code Boolean}.
 *                 </li>
 *             <li>{@code BigInteger} and {@code BigDecimal}.</li>
 *             <li>{@code String}.</li>
 *             <li>Arrays, such as {@code new int[]{1,2,3}}.</li>
 *             <li>{@code List<T>}, where T is any type mentioned in this
 *                 list except for {@code Optional<T>}.</li>
 *             <li>{@code Map<K, V>}, where K is a primitive, boxed primitive,
 *                 {@code String}, enum, enum-like or a java.time temporal
 *                 class and V is any type mentioned in this list except for
 *                 {@code Optional<T>}.</li>
 *             <li>Java enums.</li>
 *             <li>So-called <i>enum-likes</i>, which is are normal classes
 *                 that override equals(), hashCode() and toString(), as well
 *                 as having public static valueOf(String) methods.</li>
 *             <li>Other DTO interfaces.</li>
 *             <li>{@code Optional<T>}, where T is any type mentioned in this
 *                 list except for {@code Optional<T>}.</li>
 *             <li>Any non-local java.time temporal type, such as {@code
 *                 Instant} and {@code Duration}. Note that {@code Date} is
 *                 <i>not</i> supported.</li>
 *             <li>So-called custom types, which typically are exclusive to a
 *                 particular encoding, such as {@link
 *                 se.arkalix.dto.json.value.JsonObject JsonObject}, which may
 *                 only be used by DTO interfaces that can only be read/written
 *                 from/to {@link se.arkalix.dto.DtoEncoding#JSON JSON}.</li>
 *         </ol>
 *     </li>
 * </ol>
 * The following is an example of a valid DTO interface declaration:
 * <pre>
 *     &#64;DtoReadableAs(DtoEncoding.JSON)
 *     public interface Message {
 *         String title();
 *         List&lt;String&gt; texts();
 *         Instant sentAt();
 *     }
 * </pre>
 * <h2>DTO Interface Classes and Builders</h2>
 * When a DTO interface has been successfully processed by the
 * <i>kalix-processors</i> package and compilation succeeded, two new generated
 * classes will exist. Please make sure that whatever folder they end up in is
 * included in the Java classpath to make them usable from within your
 * application. The names of the generated classes will end with "Dto" and
 * "Builder". In the case of the above example, the names of the generated
 * classes would be "MessageDto" and "MessageBuilder".
 * <p>
 * The "Dto" class concretely holds the data specified in the DTO interface in
 * the form of getters, while the "Builder" class is used for creating "Dto"
 * instances. "Dto" classes are always immutable, meaning that once constructed
 * their contents can never change. Additionally, whenever a "Builder" attempts
 * to create a "Dto" instance, the inputs it were given are validated to make
 * sure that no non-optional data is missing.
 * <p>
 * Here follows an example of using the builder associated with the above
 * example to construct a valid "Dto" class:
 * <pre>
 *     final var message = new MessageBuilder()
 *         .title("Hello, Arrowhead!")
 *         .texts("Text 1", "Text 2", "Text 3")
 *         .sentAt(Instant.now())
 *         .build();
 *
 *     // Print message title to see that it worked.
 *     System.out.println(message.title());
 * </pre>
 */
package se.arkalix.dto;