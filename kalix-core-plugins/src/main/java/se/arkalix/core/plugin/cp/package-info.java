/**
 * <h1>Contract Proxy Plugins and Utilities</h1>
 * The types provided by this package are meant to reduce the amount of work
 * required to correctly integrate against services that can be provided by
 * an Arrowhead Contract Proxy system.
 * <p>
 * An Arrowhead Contract Proxy helps authorized Arrowhead systems send,
 * receive, accept and reject offers to enter into legally binding digital
 * contracts. How it concretely makes accepted offers legally binding is out of
 * the scope of this package. However, it can always be assumed that each
 * Contract Proxy <i>owns</i> at least one legal identity and <i>trusts</i>
 * and is able to <i>communicate with</i> at least one other legal identity.
 * When formulating {@link se.arkalix.core.plugin.cp.TrustedContractOffer
 * offers}, one owned and one trusted legal identity must be <i>named</i> in
 * that offer. This package currently provides no means for those names to be
 * learned. This means that they must be established some other way before this
 * package can be meaningfully used.
 *
 * @see se.arkalix.core.plugin.cp.ArContractNegotiationTrusted ArContractNegotiationTrusted
 * @see se.arkalix.core.plugin.cp.ArContractNegotiationTrustedSession ArContractNegotiationTrustedSession
 */
package se.arkalix.core.plugin.cp;