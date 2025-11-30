package com.community.cart.snapshot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class CartSnapshotConfig {

    private final Clients clients = new Clients();

    public Clients getClients() {
        return clients;
    }

    public static class Clients {
        private final CartCrud cartCrud = new CartCrud();

        public CartCrud getCartCrud() {
            return cartCrud;
        }

        public static class CartCrud {
            private String url;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }
    }
}
