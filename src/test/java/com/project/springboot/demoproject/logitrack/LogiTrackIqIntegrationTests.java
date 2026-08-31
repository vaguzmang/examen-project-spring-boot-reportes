package com.project.springboot.demoproject.logitrack;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.audit.ApplicationEventPublisherHolder;
import com.project.springboot.demoproject.entities.Bodega;
import com.project.springboot.demoproject.entities.Movimiento;
import com.project.springboot.demoproject.entities.OrdenCompra;
import com.project.springboot.demoproject.entities.Producto;
import com.project.springboot.demoproject.entities.Proveedor;
import com.project.springboot.demoproject.entities.Usuario;
import com.project.springboot.demoproject.enums.EstadoOrdenCompra;
import com.project.springboot.demoproject.enums.Rol;
import com.project.springboot.demoproject.enums.TipoMovimiento;
import com.project.springboot.demoproject.repositories.BodegaRepository;
import com.project.springboot.demoproject.repositories.MovimientoRepository;
import com.project.springboot.demoproject.repositories.OrdenCompraRepository;
import com.project.springboot.demoproject.repositories.ProductoRepository;
import com.project.springboot.demoproject.repositories.ProveedorRepository;
import com.project.springboot.demoproject.repositories.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LogiTrackIqIntegrationTests {

    private static final ZoneId BOGOTA =
            ZoneId.of("America/Bogota");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BodegaRepository bodegaRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private OrdenCompraRepository ordenCompraRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    /*
     * Evita que la auditoría interfiera con el sembrado de datos
     * de estas pruebas. La lógica funcional sigue siendo real.
     */
    @MockBean
    private ApplicationEventPublisherHolder publisherHolder;

    private Usuario admin;
    private Usuario agente;
    private Bodega bodega;
    private Proveedor proveedor;
    private Producto producto;

    @BeforeEach
    void prepararDatos() {

        admin = nuevoUsuario(
                "admin_test",
                "admin_test@logitrack.test",
                Rol.ADMIN);

        agente = nuevoUsuario(
                "agente_test",
                "agente_test@logitrack.test",
                Rol.AGENTE);

        proveedor = new Proveedor();
        proveedor.setNombre("Proveedor Test");
        proveedor.setContacto("proveedor@test.com");
        proveedor.setDiasEntrega(10);
        proveedor = proveedorRepository.saveAndFlush(proveedor);

        bodega = new Bodega();
        bodega.setNombre("Bodega Test");
        bodega.setUbicacion("Bucaramanga");
        bodega.setCapacidad(1000);
        bodega.setEncargado("Encargado Test");
        bodega = bodegaRepository.saveAndFlush(bodega);

        producto = new Producto();
        producto.setNombre("Producto Test");
        producto.setCategoria("Tecnologia");
        producto.setPrecio(new BigDecimal("2500000"));
        producto.setProveedorPrincipal(proveedor);
        producto = productoRepository.saveAndFlush(producto);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5})
    void cantidadCeroONegativaEnOrdenDebeResponder400(
            int cantidad) throws Exception {

        String json = """
                {
                  "productoId": %d,
                  "proveedorId": %d,
                  "bodegaDestinoId": %d,
                  "cantidad": %d,
                  "precioUnitario": 2500000
                }
                """.formatted(
                    producto.getId(),
                    proveedor.getId(),
                    bodega.getId(),
                    cantidad);

        mockMvc.perform(post("/ordenes")
                .with(user(agente.getUsername()).roles("AGENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void agenteIntentandoAprobarOrdenDebeResponder403()
            throws Exception {

        OrdenCompra orden =
                crearOrden(EstadoOrdenCompra.BORRADOR, 10);

        mockMvc.perform(patch(
                    "/ordenes/{id}/estado",
                    orden.getId())
                .with(user(agente.getUsername()).roles("AGENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"estado":"APROBADA"}
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void ordenCanceladaNoPuedeVolverAAprobada()
            throws Exception {

        OrdenCompra orden =
                crearOrden(EstadoOrdenCompra.CANCELADA, 10);

        mockMvc.perform(patch(
                    "/ordenes/{id}/estado",
                    orden.getId())
                .with(user(admin.getUsername()).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"estado":"APROBADA"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        containsString(
                            "CANCELADA -> APROBADA")));
    }

    @Test
    void aprobadaARecibidaDebeCrearMovimientoEntrada()
            throws Exception {

        OrdenCompra orden =
                crearOrden(EstadoOrdenCompra.APROBADA, 35);

        int entradasAntes =
                movimientoRepository
                .findByTipo(TipoMovimiento.ENTRADA)
                .size();

        mockMvc.perform(patch(
                    "/ordenes/{id}/estado",
                    orden.getId())
                .with(user(admin.getUsername()).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"estado":"RECIBIDA"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado")
                        .value("RECIBIDA"));

        List<Movimiento> entradas =
                movimientoRepository
                .findByTipo(TipoMovimiento.ENTRADA);

        assertEquals(
                entradasAntes + 1,
                entradas.size());

        boolean entradaEncontrada =
                entradas.stream().anyMatch(m ->
                    m.getBodegaDestino() != null
                    && m.getBodegaDestino().getId()
                        .equals(bodega.getId())
                    && m.getDetalles().stream().anyMatch(d ->
                        d.getProducto().getId()
                            .equals(producto.getId())
                        && d.getCantidad() == 35
                    )
                );

        assertTrue(
                entradaEncontrada,
                "La recepción debe crear una ENTRADA real");
    }

    @Test
    void panelConSeveridadInvalidaDebeResponder400YConservarAnterior()
            throws Exception {

        String narrativa =
                "Resumen valido que debe permanecer almacenado aunque llegue un resumen posterior invalido.";

        publicarResumenValido(narrativa);

        String hoy =
                LocalDate.now(BOGOTA).toString();

        String invalido = """
                {
                  "fecha":"%s",
                  "narrativa":"Este resumen invalido no debe reemplazar al anterior.",
                  "alertas":[
                    {
                      "severidad":"CRITICA",
                      "titulo":"Alerta incorrecta",
                      "detalle":"Severidad invalida para la prueba",
                      "productoId":%d
                    }
                  ],
                  "accionesSugeridas":[]
                }
                """.formatted(hoy, producto.getId());

        mockMvc.perform(post("/panel/resumen")
                .with(user(agente.getUsername()).roles("AGENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalido))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/panel/resumen")
                .with(user(agente.getUsername()).roles("AGENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.narrativa")
                        .value(narrativa));
    }

    @Test
    void panelConIdInexistenteDebeResponder400YConservarAnterior()
            throws Exception {

        String narrativa =
                "Resumen valido de inventario que debe conservarse frente a referencias inexistentes.";

        publicarResumenValido(narrativa);

        String hoy =
                LocalDate.now(BOGOTA).toString();

        String invalido = """
                {
                  "fecha":"%s",
                  "narrativa":"Resumen que referencia un producto que no existe en el sistema.",
                  "alertas":[
                    {
                      "severidad":"ALTA",
                      "titulo":"Producto inexistente",
                      "detalle":"Referencia inexistente para la prueba",
                      "productoId":999999999
                    }
                  ],
                  "accionesSugeridas":[]
                }
                """.formatted(hoy);

        mockMvc.perform(post("/panel/resumen")
                .with(user(agente.getUsername()).roles("AGENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        containsString(
                            "productoId inexistente")));

        mockMvc.perform(get("/panel/resumen")
                .with(user(agente.getUsername()).roles("AGENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.narrativa")
                        .value(narrativa));
    }

    @Test
    void pdfBorradorDebeGuardarseTenerMarcaYEliminarseAlCambiarEstado()
            throws Exception {

        OrdenCompra orden =
                crearOrden(EstadoOrdenCompra.BORRADOR, 15);

        byte[] pdf =
                mockMvc.perform(post(
                            "/ordenes/{id}/pdf",
                            orden.getId())
                        .with(user(agente.getUsername())
                                .roles("AGENTE")))
                        .andExpect(status().isOk())
                        .andExpect(content()
                                .contentTypeCompatibleWith(
                                    MediaType.APPLICATION_PDF))
                        .andReturn()
                        .getResponse()
                        .getContentAsByteArray();

        assertTrue(pdf.length > 0);

        OrdenCompra conPdf =
                ordenCompraRepository
                .findById(orden.getId())
                .orElseThrow();

        assertNotNull(conPdf.getPdf());
        assertTrue(conPdf.getPdf().length > 0);
        assertNotNull(conPdf.getFechaGeneracionPdf());

        try (PDDocument document =
                PDDocument.load(pdf)) {

            String texto =
                    new PDFTextStripper()
                    .getText(document);

            assertTrue(
                    texto.contains("BORRADOR"),
                    "El PDF BORRADOR debe contener la marca BORRADOR");
        }

        mockMvc.perform(patch(
                    "/ordenes/{id}/estado",
                    orden.getId())
                .with(user(admin.getUsername()).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"estado":"APROBADA"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado")
                        .value("APROBADA"));

        OrdenCompra despues =
                ordenCompraRepository
                .findById(orden.getId())
                .orElseThrow();

        assertNull(despues.getPdf());
        assertNull(despues.getFechaGeneracionPdf());

        mockMvc.perform(get(
                    "/ordenes/{id}/pdf",
                    orden.getId())
                .with(user(admin.getUsername()).roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchEstadoConCampoExtraDebeResponder400()
            throws Exception {

        OrdenCompra orden =
                crearOrden(EstadoOrdenCompra.BORRADOR, 10);

        mockMvc.perform(patch(
                    "/ordenes/{id}/estado",
                    orden.getId())
                .with(user(admin.getUsername()).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "estado":"APROBADA",
                          "campoExtra":"NO"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        containsString(
                            "exactamente el campo estado")));
    }

    private Usuario nuevoUsuario(
            String username,
            String email,
            Rol rol) {

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword("test-password");
        usuario.setEmail(email);
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setCreadoEn(LocalDateTime.now());

        return usuarioRepository.saveAndFlush(usuario);
    }

    private OrdenCompra crearOrden(
            EstadoOrdenCompra estado,
            int cantidad) {

        OrdenCompra orden = new OrdenCompra();
        orden.setProducto(producto);
        orden.setProveedor(proveedor);
        orden.setBodegaDestino(bodega);
        orden.setCantidad(cantidad);

        BigDecimal precio =
                new BigDecimal("2500000");

        orden.setPrecioUnitario(precio);
        orden.setTotal(
                precio.multiply(
                    BigDecimal.valueOf(cantidad)));

        orden.setFechaCreacion(
                LocalDateTime.now(BOGOTA));

        orden.setEstado(estado);
        orden.setCreadoPor(agente);

        return ordenCompraRepository.saveAndFlush(orden);
    }

    private void publicarResumenValido(
            String narrativa) throws Exception {

        String hoy =
                LocalDate.now(BOGOTA).toString();

        String valido = """
                {
                  "fecha":"%s",
                  "narrativa":"%s",
                  "alertas":[
                    {
                      "severidad":"ALTA",
                      "titulo":"Revisar producto",
                      "detalle":"Revisar producto y sus existencias",
                      "productoId":%d
                    }
                  ],
                  "accionesSugeridas":[
                    {
                      "tipo":"REVISAR_PRODUCTO",
                      "descripcion":"Revisar existencias",
                      "productoId":%d
                    }
                  ]
                }
                """.formatted(
                    hoy,
                    narrativa,
                    producto.getId(),
                    producto.getId());

        mockMvc.perform(post("/panel/resumen")
                .with(user(agente.getUsername())
                        .roles("AGENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(valido))
                .andExpect(status().isOk());
    }
}
