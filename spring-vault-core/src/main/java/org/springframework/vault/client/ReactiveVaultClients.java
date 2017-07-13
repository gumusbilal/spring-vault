/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.vault.client;

import org.springframework.core.codec.ByteArrayDecoder;
import org.springframework.core.codec.ByteArrayEncoder;
import org.springframework.core.codec.StringDecoder;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.codec.CodecConfigurer.CustomCodecs;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilderFactory;

/**
 * Vault Client factory to create {@link WebClient} configured to the needs of accessing
 * Vault.
 *
 * @author Mark Paluch
 * @since 2.0
 */
public class ReactiveVaultClients {

	/**
	 * Create a {@link WebClient} configured with {@link VaultEndpoint} and
	 * {@link ClientHttpConnector}. The client accepts relative URIs without a leading
	 * slash that are expanded to use {@link VaultEndpoint}.
	 * <p>
	 * Requires Jackson 2 for Object-to-JSON mapping.
	 *
	 * @param endpoint must not be {@literal null}.
	 * @param connector must not be {@literal null}.
	 * @return the configured {@link WebClient}.
	 */
	public static WebClient createWebClient(VaultEndpoint endpoint,
			ClientHttpConnector connector) {
		return createWebClient(SimpleVaultEndpointProvider.of(endpoint), connector);
	}

	/**
	 * Create a {@link WebClient} configured with {@link VaultEndpoint} and
	 * {@link ClientHttpConnector}. The client accepts relative URIs without a leading
	 * slash that are expanded to use {@link VaultEndpoint}.
	 * <p>
	 * Requires Jackson 2 for Object-to-JSON mapping.
	 *
	 * @param endpointProvider must not be {@literal null}.
	 * @param connector must not be {@literal null}.
	 * @return the configured {@link WebClient}.
	 */
	public static WebClient createWebClient(VaultEndpointProvider endpointProvider,
			ClientHttpConnector connector) {

		Assert.notNull(endpointProvider, "VaultEndpointProvider must not be null");
		Assert.notNull(connector, "ClientHttpConnector must not be null");

		UriBuilderFactory uriBuilderFactory = VaultClients
				.createUriBuilderFactory(endpointProvider);

		ExchangeStrategies strategies = ExchangeStrategies.builder()
				.codecs(configurer -> {

					CustomCodecs cc = configurer.customCodecs();

					cc.decoder(new ByteArrayDecoder());
					cc.decoder(new Jackson2JsonDecoder());
					cc.decoder(StringDecoder.allMimeTypes(false));

					cc.encoder(new ByteArrayEncoder());
					cc.encoder(new Jackson2JsonEncoder());

				}).build();

		return WebClient.builder().uriBuilderFactory(uriBuilderFactory)
				.exchangeStrategies(strategies).clientConnector(connector).build();
	}
}
