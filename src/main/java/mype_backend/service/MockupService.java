package mype_backend.service;

import mype_backend.dto.MockupProductoBaseRequestDTO;
import mype_backend.dto.MockupProductoBaseResponseDTO;
import mype_backend.dto.MockupProyectoRequestDTO;
import mype_backend.dto.MockupProyectoResponseDTO;
import mype_backend.dto.MockupResultadoResponseDTO;
import mype_backend.entity.MockupProductoBase;
import mype_backend.entity.MockupProyecto;
import mype_backend.entity.Usuario;
import mype_backend.model.TipoProductoMockup;
import mype_backend.repository.MockupProductoBaseRepository;
import mype_backend.repository.MockupProyectoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class MockupService {

    private static final String FOLDER_BASE = "mockupBase";
    private static final String FOLDER_CLIENTE = "mockupCliente";
    private static final String FOLDER_RESULTADO = "mockupProcesada";
    private static final String COLOR_VARIABLE = "VARIABLE";
    private static final long MAX_IMAGE_BYTES = 10 * 1024 * 1024;

    private final MockupProductoBaseRepository productoBaseRepository;
    private final MockupProyectoRepository proyectoRepository;
    private final StorageService storageService;
    private final UsuarioService usuarioService;
    private final MockupImageProcessor imageProcessor;

    public MockupService(
            MockupProductoBaseRepository productoBaseRepository,
            MockupProyectoRepository proyectoRepository,
            StorageService storageService,
            UsuarioService usuarioService,
            MockupImageProcessor imageProcessor) {
        this.productoBaseRepository = productoBaseRepository;
        this.proyectoRepository = proyectoRepository;
        this.storageService = storageService;
        this.usuarioService = usuarioService;
        this.imageProcessor = imageProcessor;
    }

    @Transactional(readOnly = true)
    public List<MockupProductoBaseResponseDTO> listarProductosBaseActivos(Jwt jwt) {
        validarAccesoMarketing(jwt);
        return productoBaseRepository.findByActivoTrueOrderByTipoProductoAscColorAscNombreAsc()
                .stream()
                .map(this::toProductoBaseResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MockupProductoBaseResponseDTO> listarProductosBase(Jwt jwt) {
        validarAccesoMarketing(jwt);
        return productoBaseRepository.findAllByOrderByTipoProductoAscColorAscNombreAsc()
                .stream()
                .map(this::toProductoBaseResponse)
                .toList();
    }

    @Transactional
    public MockupProductoBaseResponseDTO crearProductoBase(
            Jwt jwt,
            MockupProductoBaseRequestDTO request,
            MultipartFile imagenBase) {
        validarAccesoMarketing(jwt);
        validarProductoBaseRequest(request);
        validarArchivoImagen(imagenBase, "La imagen base es obligatoria.");

        MultipartFile imagenNormalizada = imageProcessor.normalizarProductoBase(imagenBase);
        String imagenUrl = storageService.uploadFile(imagenNormalizada, FOLDER_BASE);
        MockupProductoBase producto = MockupProductoBase.builder()
                .nombre(request.nombre().trim())
                .tipoProducto(TipoProductoMockup.fromCodigo(request.tipoProducto()).getCodigo())
                .color(COLOR_VARIABLE)
                .coloresDisponibles(serializarColores(request.coloresDisponibles()))
                .imagenBasePath(imagenUrl)
                .areaX(request.areaX())
                .areaY(request.areaY())
                .areaWidth(request.areaWidth())
                .areaHeight(request.areaHeight())
                .activo(request.activo() == null || request.activo())
                .build();

        return toProductoBaseResponse(productoBaseRepository.save(producto));
    }

    @Transactional
    public MockupProductoBaseResponseDTO actualizarProductoBase(
            Jwt jwt,
            Long id,
            MockupProductoBaseRequestDTO request) {
        validarAccesoMarketing(jwt);
        validarProductoBaseRequest(request);

        MockupProductoBase producto = obtenerProductoBase(id);
        producto.setNombre(request.nombre().trim());
        producto.setTipoProducto(TipoProductoMockup.fromCodigo(request.tipoProducto()).getCodigo());
        producto.setColor(COLOR_VARIABLE);
        producto.setColoresDisponibles(serializarColores(request.coloresDisponibles()));
        producto.setAreaX(request.areaX());
        producto.setAreaY(request.areaY());
        producto.setAreaWidth(request.areaWidth());
        producto.setAreaHeight(request.areaHeight());
        producto.setActivo(request.activo() == null || request.activo());

        return toProductoBaseResponse(productoBaseRepository.save(producto));
    }

    @Transactional
    public MockupProductoBaseResponseDTO actualizarImagenBase(Jwt jwt, Long id, MultipartFile imagenBase) {
        validarAccesoMarketing(jwt);
        validarArchivoImagen(imagenBase, "La imagen base es obligatoria.");

        MockupProductoBase producto = obtenerProductoBase(id);
        String imagenAnterior = producto.getImagenBasePath();
        MultipartFile imagenNormalizada = imageProcessor.normalizarProductoBase(imagenBase);
        String nuevaImagen = storageService.uploadFile(imagenNormalizada, FOLDER_BASE);
        producto.setImagenBasePath(nuevaImagen);

        if (imagenAnterior != null && !imagenAnterior.isBlank()) {
            storageService.deleteFile(imagenAnterior);
        }

        return toProductoBaseResponse(productoBaseRepository.save(producto));
    }

    @Transactional
    public MockupProductoBaseResponseDTO cambiarEstadoProductoBase(Jwt jwt, Long id, Boolean activo) {
        validarAccesoMarketing(jwt);
        if (activo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado activo es obligatorio.");
        }

        MockupProductoBase producto = obtenerProductoBase(id);
        producto.setActivo(activo);
        return toProductoBaseResponse(productoBaseRepository.save(producto));
    }

    @Transactional(readOnly = true)
    public List<MockupProyectoResponseDTO> listarProyectos(Jwt jwt) {
        Usuario usuario = validarAccesoMarketing(jwt);
        boolean puedeVerTodos = esAdminOMarketing(usuario);
        List<MockupProyecto> proyectos = puedeVerTodos
                ? proyectoRepository.findAllByOrderByCreadoEnDesc()
                : proyectoRepository.findByCreadoPorUsuarioIdOrderByCreadoEnDesc(usuario.getId());

        return proyectos.stream()
                .map(this::toProyectoResponse)
                .toList();
    }

    @Transactional
    public MockupProyectoResponseDTO crearProyecto(
            Jwt jwt,
            MockupProyectoRequestDTO request,
            MultipartFile imagenCliente) {
        Usuario usuario = validarAccesoMarketing(jwt);
        validarProyectoRequest(request);
        validarArchivoImagen(imagenCliente, "La imagen del cliente es obligatoria.");

        MockupProductoBase productoBase = obtenerProductoBase(request.productoBaseId());
        if (!Boolean.TRUE.equals(productoBase.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El producto base seleccionado está inactivo.");
        }

        String colorSeleccionado = normalizarOpcional(request.colorSeleccionado());
        if (colorSeleccionado != null && !deserializarColores(productoBase).contains(colorSeleccionado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El color seleccionado no está disponible para este producto base.");
        }

        String imagenClienteUrl = storageService.uploadFile(imagenCliente, FOLDER_CLIENTE);
        MockupProyecto proyecto = MockupProyecto.builder()
                .nombre(request.nombre().trim())
                .productoBase(productoBase)
                .imagenClientePath(imagenClienteUrl)
                .colorSeleccionado(colorSeleccionado)
                .creadoPorUsuario(usuario)
                .build();

        return toProyectoResponse(proyectoRepository.save(proyecto));
    }

    @Transactional
    public MockupResultadoResponseDTO guardarResultado(Jwt jwt, Long proyectoId, MultipartFile resultado) {
        Usuario usuario = validarAccesoMarketing(jwt);
        validarArchivoImagen(resultado, "La imagen final del mockup es obligatoria.");

        MockupProyecto proyecto = obtenerProyecto(proyectoId);
        if (!esAdminOMarketing(usuario) && !proyecto.getCreadoPorUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes modificar un mockup de otro usuario.");
        }

        String resultadoAnterior = proyecto.getResultadoPath();
        String resultadoUrl = storageService.uploadFile(resultado, FOLDER_RESULTADO);
        proyecto.setResultadoPath(resultadoUrl);
        proyectoRepository.save(proyecto);

        if (resultadoAnterior != null && !resultadoAnterior.isBlank()) {
            storageService.deleteFile(resultadoAnterior);
        }

        return new MockupResultadoResponseDTO(proyecto.getId(), resultadoUrl);
    }

    private Usuario validarAccesoMarketing(Jwt jwt) {
        Usuario usuario = usuarioService.obtenerUsuarioAutenticado(jwt);
        if (!esAdminOMarketing(usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo ADMIN o MARKETING puede usar el módulo de mockups.");
        }
        return usuario;
    }

    private boolean esAdminOMarketing(Usuario usuario) {
        if (usuario.getRol() == null || usuario.getRol().getCodigo() == null) {
            return false;
        }
        String codigo = usuario.getRol().getCodigo();
        return "ADMIN".equals(codigo) || "MARKETING".equals(codigo);
    }

    private void validarProductoBaseRequest(MockupProductoBaseRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos del producto base son obligatorios.");
        }
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del producto base es obligatorio.");
        }
        try {
            TipoProductoMockup.fromCodigo(request.tipoProducto());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        if (normalizarColores(request.coloresDisponibles()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agrega al menos un color disponible.");
        }
        validarArea(request.areaX(), "areaX");
        validarArea(request.areaY(), "areaY");
        validarArea(request.areaWidth(), "areaWidth");
        validarArea(request.areaHeight(), "areaHeight");
        if (request.areaWidth().compareTo(BigDecimal.ZERO) <= 0 || request.areaHeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El área imprimible debe tener ancho y alto mayores a cero.");
        }
    }

    private void validarProyectoRequest(MockupProyectoRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos del proyecto son obligatorios.");
        }
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del mockup es obligatorio.");
        }
        if (request.productoBaseId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El producto base es obligatorio.");
        }
    }

    private void validarArea(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El campo " + fieldName + " es obligatorio.");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El campo " + fieldName + " no puede ser negativo.");
        }
    }

    private void validarArchivoImagen(MultipartFile file, String message) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen no debe superar 10 MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permiten archivos de imagen.");
        }
    }

    private MockupProductoBase obtenerProductoBase(Long id) {
        return productoBaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto base no encontrado."));
    }

    private MockupProyecto obtenerProyecto(Long id) {
        return proyectoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mockup no encontrado."));
    }

    private String normalizarOpcional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String serializarColores(List<String> colores) {
        return String.join(",", normalizarColores(colores));
    }

    private List<String> normalizarColores(List<String> colores) {
        if (colores == null) {
            return List.of();
        }
        return colores.stream()
                .filter(color -> color != null && !color.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private List<String> deserializarColores(MockupProductoBase producto) {
        if (producto.getColoresDisponibles() != null && !producto.getColoresDisponibles().isBlank()) {
            return Arrays.stream(producto.getColoresDisponibles().split(","))
                    .map(String::trim)
                    .filter(color -> !color.isBlank())
                    .distinct()
                    .toList();
        }
        if (producto.getColor() != null && !producto.getColor().isBlank()) {
            return List.of(producto.getColor().trim());
        }
        return List.of();
    }

    public MockupProductoBaseResponseDTO toProductoBaseResponse(MockupProductoBase producto) {
        return new MockupProductoBaseResponseDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getTipoProducto(),
                producto.getColor(),
                deserializarColores(producto),
                producto.getImagenBasePath(),
                producto.getAreaX(),
                producto.getAreaY(),
                producto.getAreaWidth(),
                producto.getAreaHeight(),
                producto.getActivo(),
                producto.getCreadoEn());
    }

    private MockupProyectoResponseDTO toProyectoResponse(MockupProyecto proyecto) {
        Usuario usuario = proyecto.getCreadoPorUsuario();
        return new MockupProyectoResponseDTO(
                proyecto.getId(),
                proyecto.getNombre(),
                toProductoBaseResponse(proyecto.getProductoBase()),
                proyecto.getImagenClientePath(),
                proyecto.getResultadoPath(),
                proyecto.getColorSeleccionado(),
                usuario.getId(),
                usuario.getNombre(),
                proyecto.getCreadoEn());
    }
}
