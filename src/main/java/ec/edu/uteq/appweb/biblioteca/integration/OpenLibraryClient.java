package ec.edu.uteq.appweb.biblioteca.integration;

import ec.edu.uteq.appweb.biblioteca.config.CacheConfig;
import ec.edu.uteq.appweb.biblioteca.exception.ServicioExternoException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OpenLibraryClient {

    private final RestClient restClient;

    public OpenLibraryClient(RestClient restClientExterno) {
        this.restClient = restClientExterno;
    }

    @Cacheable(
            cacheNames = CacheConfig.CACHE_OPENLIBRARY,
            key = "#p0",
            unless = "#result == null")
    public OpenLibraryResponse consultarPorIsbn(String isbn) {

        try {
            OpenLibraryResponse respuesta = restClient.get()
                    .uri("/isbn/{isbn}.json", isbn)
                    .retrieve()
                    .body(OpenLibraryResponse.class);

            if (respuesta == null) {
                throw new ServicioExternoException(
                        "Open Library devolvio una respuesta vacia");
            }

            return respuesta;

        } catch (HttpClientErrorException.NotFound ex) {
            return null;

        } catch (RestClientException ex) {
            throw new ServicioExternoException(
                    "No fue posible consultar Open Library", ex);
        }
    }
}