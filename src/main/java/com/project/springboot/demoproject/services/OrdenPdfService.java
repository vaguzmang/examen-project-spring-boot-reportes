package com.project.springboot.demoproject.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.entities.OrdenCompra;
import com.project.springboot.demoproject.enums.EstadoOrdenCompra;
import com.project.springboot.demoproject.exception.BusinessException;
import com.project.springboot.demoproject.exception.ResourceNotFoundException;
import com.project.springboot.demoproject.repositories.OrdenCompraRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdenPdfService {

    private static final ZoneId BOGOTA =
            ZoneId.of("America/Bogota");

    private final OrdenCompraRepository ordenCompraRepository;

    @Transactional
    public byte[] generar(Long id) {

        OrdenCompra orden = buscar(id);

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream cs =
                    new PDPageContentStream(document, page)) {

                if (orden.getEstado()
                        == EstadoOrdenCompra.BORRADOR) {

                    PDExtendedGraphicsState gs =
                            new PDExtendedGraphicsState();

                    gs.setNonStrokingAlphaConstant(0.18f);

                    cs.setGraphicsStateParameters(gs);

                    cs.beginText();
                    cs.setFont(
                            PDType1Font.HELVETICA_BOLD,
                            65);

                    cs.setTextMatrix(
                        Matrix.getRotateInstance(
                            Math.toRadians(45),
                            100,
                            280));

                    cs.showText("BORRADOR");
                    cs.endText();
                }

                float y = 720;

                escribir(cs, "LOGITRACK IQ - ORDEN DE COMPRA",
                        70, y, 16);
                y -= 45;

                escribir(cs,
                    "Orden No: " + orden.getId(),
                    70, y, 12);
                y -= 25;

                escribir(cs,
                    "Fecha: " + orden.getFechaCreacion(),
                    70, y, 12);
                y -= 25;

                escribir(cs,
                    "Proveedor: " +
                    seguro(orden.getProveedor().getNombre()),
                    70, y, 12);
                y -= 25;

                escribir(cs,
                    "Producto: " +
                    seguro(orden.getProducto().getNombre()),
                    70, y, 12);
                y -= 25;

                escribir(cs,
                    "Cantidad: " + orden.getCantidad(),
                    70, y, 12);
                y -= 25;

                escribir(cs,
                    "Precio unitario: " +
                    orden.getPrecioUnitario(),
                    70, y, 12);
                y -= 25;

                escribir(cs,
                    "Total: " + orden.getTotal(),
                    70, y, 12);
                y -= 25;

                escribir(cs,
                    "Bodega destino: " +
                    seguro(
                        orden.getBodegaDestino().getNombre()),
                    70, y, 12);
                y -= 25;

                escribir(cs,
                    "Estado: " + orden.getEstado(),
                    70, y, 12);
            }

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            document.save(out);

            byte[] bytes = out.toByteArray();

            orden.setPdf(bytes);
            orden.setFechaGeneracionPdf(
                    LocalDateTime.now(BOGOTA));

            ordenCompraRepository.save(orden);

            return bytes;

        } catch (IOException e) {
            throw new BusinessException(
                    "No fue posible generar el PDF");
        }
    }

    @Transactional(readOnly = true)
    public byte[] obtener(Long id) {

        OrdenCompra orden = buscar(id);

        if (orden.getPdf() == null
                || orden.getPdf().length == 0) {

            throw ResourceNotFoundException.of(
                    "PDF OrdenCompra", id);
        }

        return orden.getPdf();
    }

    private OrdenCompra buscar(Long id) {
        return ordenCompraRepository.findById(id)
                .orElseThrow(() ->
                    ResourceNotFoundException.of(
                        "OrdenCompra", id));
    }

    private void escribir(
            PDPageContentStream cs,
            String texto,
            float x,
            float y,
            int size) throws IOException {

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, size);
        cs.newLineAtOffset(x, y);
        cs.showText(seguro(texto));
        cs.endText();
    }

    private String seguro(String texto) {

        if (texto == null) return "";

        return texto
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("Á", "A")
            .replace("É", "E")
            .replace("Í", "I")
            .replace("Ó", "O")
            .replace("Ú", "U")
            .replace("ñ", "n")
            .replace("Ñ", "N");
    }
}
