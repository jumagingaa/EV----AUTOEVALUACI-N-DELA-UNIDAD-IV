package ec.edu.uteq.appweb.biblioteca.web.controller;

import ec.edu.uteq.appweb.biblioteca.domain.Libro;
import ec.edu.uteq.appweb.biblioteca.integration.OpenLibraryClient;
import ec.edu.uteq.appweb.biblioteca.integration.OpenLibraryResponse;
import ec.edu.uteq.appweb.biblioteca.service.LibroService;
import ec.edu.uteq.appweb.biblioteca.web.dto.ApiResponse;
import ec.edu.uteq.appweb.biblioteca.web.dto.LibroEnriquecidoResponse;
import ec.edu.uteq.appweb.biblioteca.web.dto.LibroRequest;
import ec.edu.uteq.appweb.biblioteca.web.dto.LibroResponse;
import ec.edu.uteq.appweb.biblioteca.web.dto.PageMeta;
import ec.edu.uteq.appweb.biblioteca.web.mapper.LibroMapper;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/libros")
public class LibroController {

    private final LibroService servicio;
    private final LibroMapper mapper;
    private final OpenLibraryClient openLibraryClient;

    public LibroController(
            LibroService servicio,
            LibroMapper mapper,
            OpenLibraryClient openLibraryClient) {

        this.servicio = servicio;
        this.mapper = mapper;
        this.openLibraryClient = openLibraryClient;
    }

    @GetMapping
    public ApiResponse<List<LibroResponse>> listar(
            @RequestParam(name = "titulo", required = false) String titulo,
            @RequestParam(name = "categoriaId", required = false) Long categoriaId,
            @RequestParam(name = "anioDesde", required = false) Integer anioDesde,
            @PageableDefault(size = 20) Pageable paginacion) {

        Page<Libro> pagina = servicio.buscar(
                titulo, categoriaId, anioDesde, paginacion);

        List<LibroResponse> datos = pagina.getContent()
                .stream()
                .map(mapper::aRespuesta)
                .toList();

        return ApiResponse.ok(
                datos, "Libros listados", PageMeta.de(pagina));
    }

    @GetMapping("/{id}")
    public ApiResponse<LibroResponse> buscar(
            @PathVariable("id") Long id) {

        Libro libro = servicio.buscarPorId(id);

        return ApiResponse.ok(
                mapper.aRespuesta(libro),
                "Libro encontrado");
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LibroResponse>> crear(
            @Valid @RequestBody LibroRequest solicitud) {

        Libro creado = servicio.crear(solicitud);
        LibroResponse respuesta = mapper.aRespuesta(creado);

        return ResponseEntity
                .created(URI.create("/api/v1/libros/" + creado.getId()))
                .body(ApiResponse.ok(respuesta, "Libro creado"));
    }

    @GetMapping("/{id}/enriquecido")
    public ApiResponse<LibroEnriquecidoResponse> enriquecer(
            @PathVariable("id") Long id) {

        Libro libro = servicio.buscarPorId(id);
        LibroResponse local = mapper.aRespuesta(libro);

        OpenLibraryResponse externo =
                openLibraryClient.consultarPorIsbn(libro.getIsbn());

        LibroEnriquecidoResponse resultado =
                new LibroEnriquecidoResponse(
                        local,
                        externo == null ? null : externo.title(),
                        externo == null ? null : externo.urlPortada(),
                        externo == null ? null : externo.number_of_pages(),
                        externo == null ? null : externo.publish_date());

        return ApiResponse.ok(resultado, "Libro enriquecido");
    }
}