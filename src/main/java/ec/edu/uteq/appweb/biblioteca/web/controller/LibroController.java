package ec.edu.uteq.appweb.biblioteca.web.controller;

import ec.edu.uteq.appweb.biblioteca.domain.Libro;
import ec.edu.uteq.appweb.biblioteca.service.LibroService;
import ec.edu.uteq.appweb.biblioteca.web.dto.ApiResponse;
import ec.edu.uteq.appweb.biblioteca.web.dto.LibroResponse;
import ec.edu.uteq.appweb.biblioteca.web.dto.PageMeta;
import ec.edu.uteq.appweb.biblioteca.web.mapper.LibroMapper;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/libros")
public class LibroController {

    private final LibroService servicio;
    private final LibroMapper mapper;

    public LibroController(LibroService servicio, LibroMapper mapper) {
        this.servicio = servicio;
        this.mapper = mapper;
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
}