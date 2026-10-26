package Backendcom.example.Backend.service;

import Backendcom.example.Backend.dto.ConfiguracionSunatDTO;
import Backendcom.example.Backend.dto.SunatResumenDTO;
import Backendcom.example.Backend.dto.SunatValidationRequestDTO;
import Backendcom.example.Backend.entity.*;
import Backendcom.example.Backend.exception.ResourceNotFoundException;
import Backendcom.example.Backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio de integración con SUNAT para emisión de comprobantes electrónicos
 * - Gestión de configuración fiscal
 * - Generación de XML de comprobantes
 * - Firma digital con certificado
 * - Envío a SUNAT
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SunatService {
    
    private final ConfiguracionSunatRepository configuracionRepository;
    private final ComprobanteSunatRepository comprobanteRepository;
    private final VentaRepository ventaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RestTemplate restTemplate;
    
    // ===== CONFIGURACIÓN SUNAT =====
    
    /**
     * Registra o actualiza configuración SUNAT del usuario
     */
    public ConfiguracionSunat guardarConfiguracion(Long idUsuario, ConfiguracionSunatDTO dto, MultipartFile certificado) {
        log.info("Guardando configuración SUNAT para usuario: {}", idUsuario);
        
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        ConfiguracionSunat config = configuracionRepository.findByUsuarioIdUsuario(idUsuario)
            .orElseGet(() -> ConfiguracionSunat.builder().usuario(usuario).build());
        
        // Validar RUC
        if (dto.getRuc() == null || dto.getRuc().length() != 11) {
            throw new IllegalArgumentException("RUC debe tener 11 dígitos");
        }
        
        config.setRuc(dto.getRuc());
        config.setRazonSocial(dto.getRazonSocial());
        config.setNombreComercial(dto.getNombreComercial());
        config.setDomicilioFiscal(dto.getDomicilioFiscal());
        config.setNumeroSerieFactura(dto.getNumeroSerieFactura());
        config.setUltimoNumeroFactura(dto.getUltimoNumeroFactura());
        config.setNumeroSerieBoleta(dto.getNumeroSerieBoleta());
        config.setUltimoNumeroBoleta(dto.getUltimoNumeroBoleta());
        config.setPasswordCertificado(dto.getPasswordCertificado());
        config.setActivo(dto.getActivo());
        config.setPruebasMode(dto.getPruebasMode());
        
        // Guardar certificado si se proporciona
        if (certificado != null && !certificado.isEmpty()) {
            try {
                config.setCertificadoDigital(certificado.getBytes());
                log.info("Certificado digital guardado: {} bytes", certificado.getSize());
            } catch (IOException e) {
                throw new RuntimeException("Error al procesar certificado digital: " + e.getMessage());
            }
        }
        
        ConfiguracionSunat saved = configuracionRepository.save(config);
        log.info("✅ Configuración SUNAT guardada para usuario: {}", idUsuario);
        return saved;
    }
    
    /**
     * Obtiene configuración SUNAT del usuario
     */
    @Transactional(readOnly = true)
    public ConfiguracionSunat obtenerConfiguracion(Long idUsuario) {
        return configuracionRepository.findByUsuarioIdUsuario(idUsuario)
            .orElseThrow(() -> new ResourceNotFoundException("Configuración SUNAT no encontrada para usuario: " + idUsuario));
    }
    
    /**
     * Verifica si el usuario tiene configuración SUNAT
     */
    @Transactional(readOnly = true)
    public boolean tieneConfiguracionSunat(Long idUsuario) {
        return configuracionRepository.existsByUsuarioIdUsuario(idUsuario);
    }
    
    // ===== GENERACIÓN DE COMPROBANTES =====
    
    /**
     * Genera y prepara un comprobante para una venta
     */
    public ComprobanteSunat prepararComprobante(Long idVenta, String tipoComprobante) {
        log.info("Preparando comprobante {} para venta: {}", tipoComprobante, idVenta);
        
        Venta venta = ventaRepository.findById(idVenta)
            .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada"));
        
        Long idUsuario = venta.getUsuario().getIdUsuario();
        ConfiguracionSunat config = obtenerConfiguracion(idUsuario);
        
        // Validar que tiene configuración activa
        if (!config.getActivo()) {
            throw new IllegalStateException("Configuración SUNAT no está activa");
        }
        
        // Generar número de comprobante
        String numeroComprobante;
        if ("01".equals(tipoComprobante)) { // Factura
            config.setUltimoNumeroFactura(config.getUltimoNumeroFactura() + 1);
            numeroComprobante = config.getNumeroSerieFactura() + "-" + 
                String.format("%06d", config.getUltimoNumeroFactura());
        } else { // Boleta
            config.setUltimoNumeroBoleta(config.getUltimoNumeroBoleta() + 1);
            numeroComprobante = config.getNumeroSerieBoleta() + "-" + 
                String.format("%06d", config.getUltimoNumeroBoleta());
        }
        
        configuracionRepository.save(config);
        
        // Crear comprobante
        ComprobanteSunat comprobante = ComprobanteSunat.builder()
            .venta(venta)
            .tipoComprobante(tipoComprobante)
            .serie(tipoComprobante.equals("01") ? config.getNumeroSerieFactura() : config.getNumeroSerieBoleta())
            .numero(tipoComprobante.equals("01") ? config.getUltimoNumeroFactura() : config.getUltimoNumeroBoleta())
            .numeroComprobante(numeroComprobante)
            .estado("PENDIENTE")
            .build();
        
        // Generar XML
        String xmlGenerado = generarXmlComprobante(comprobante, config, venta);
        comprobante.setXmlGenerado(xmlGenerado);
        
        ComprobanteSunat saved = comprobanteRepository.save(comprobante);
        log.info("✅ Comprobante preparado: {}", numeroComprobante);
        return saved;
    }
    
    /**
     * Genera XML del comprobante según formato SUNAT
     */
    private String generarXmlComprobante(ComprobanteSunat comprobante, ConfiguracionSunat config, Venta venta) {
        log.debug("Generando XML para comprobante: {}", comprobante.getNumeroComprobante());
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
        xml.append("<Invoice xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\">\n");
        
        // Versión y datos generales
        xml.append("  <UBLVersionID>2.1</UBLVersionID>\n");
        xml.append("  <CustomizationID>urn:pe:gob:sunat:cpe:v2.1</CustomizationID>\n");
        xml.append("  <ID>").append(comprobante.getNumeroComprobante()).append("</ID>\n");
        xml.append("  <IssueDate>").append(LocalDateTime.now().format(dateFormatter)).append("</IssueDate>\n");
        xml.append("  <IssueTime>").append(LocalDateTime.now().format(timeFormatter)).append("</IssueTime>\n");
        xml.append("  <InvoiceTypeCode listID=\"").append(comprobante.getTipoComprobante()).append("\"/>\n");
        xml.append("  <DocumentCurrencyCode>PEN</DocumentCurrencyCode>\n");
        
        // Datos del emisor (empresa)
        xml.append("  <AccountingSupplierParty>\n");
        xml.append("    <Party>\n");
        xml.append("      <PartyIdentification>\n");
        xml.append("        <ID schemeID=\"6\">").append(config.getRuc()).append("</ID>\n");
        xml.append("      </PartyIdentification>\n");
        xml.append("      <PartyName>\n");
        xml.append("        <Name>").append(escapeXml(config.getRazonSocial())).append("</Name>\n");
        xml.append("      </PartyName>\n");
        xml.append("      <PartyLegalEntity>\n");
        xml.append("        <RegistrationName>").append(escapeXml(config.getRazonSocial())).append("</RegistrationName>\n");
        xml.append("      </PartyLegalEntity>\n");
        xml.append("    </Party>\n");
        xml.append("  </AccountingSupplierParty>\n");
        
        // Datos del cliente (buyer)
        xml.append("  <AccountingCustomerParty>\n");
        xml.append("    <Party>\n");
        xml.append("      <PartyIdentification>\n");
        xml.append("        <ID schemeID=\"6\">").append(venta.getUsuario().getRuc()).append("</ID>\n");
        xml.append("      </PartyIdentification>\n");
        xml.append("      <PartyLegalEntity>\n");
        xml.append("        <RegistrationName>").append(escapeXml(venta.getNombreCliente())).append("</RegistrationName>\n");
        xml.append("      </PartyLegalEntity>\n");
        xml.append("    </Party>\n");
        xml.append("  </AccountingCustomerParty>\n");
        
        // Totales
        xml.append("  <LegalMonetaryTotal>\n");
        xml.append("    <LineExtensionAmount>").append(venta.getSubtotal()).append("</LineExtensionAmount>\n");
        xml.append("    <TaxInclusiveAmount>").append(venta.getTotal()).append("</TaxInclusiveAmount>\n");
        xml.append("    <PayableAmount>").append(venta.getTotal()).append("</PayableAmount>\n");
        xml.append("  </LegalMonetaryTotal>\n");
        
        xml.append("</Invoice>\n");
        
        return xml.toString();
    }
    
    /**
     * Firma el XML del comprobante usando certificado digital
     */
    public void firmarComprobante(Long idComprobante) {
        log.info("Firmando comprobante: {}", idComprobante);
        
        ComprobanteSunat comprobante = comprobanteRepository.findById(idComprobante)
            .orElseThrow(() -> new ResourceNotFoundException("Comprobante no encontrado"));
        
        try {
            // NOTA: Para producción, usar librería de firma digital
            // Ejemplo: XMLSignatureUtil.sign(xml, certificado, password)
            // Por ahora, usamos placeholder
            String xmlFirmado = firmarXmlConCertificado(
                comprobante.getXmlGenerado(),
                comprobante.getVenta().getUsuario().getIdUsuario()
            );
            
            comprobante.setXmlFirmado(xmlFirmado);
            comprobante.setEstado("ENVIADO");
            comprobante.setFechaEnvio(LocalDateTime.now());
            comprobanteRepository.save(comprobante);
            
            log.info("✅ Comprobante firmado: {}", idComprobante);
        } catch (Exception e) {
            log.error("❌ Error al firmar comprobante: {}", e.getMessage());
            comprobante.setEstado("RECHAZADO");
            comprobante.setMensajeError("Error al firmar: " + e.getMessage());
            comprobanteRepository.save(comprobante);
            throw new RuntimeException("Error al firmar comprobante: " + e.getMessage());
        }
    }
    
    /**
     * Firma XML con certificado digital
     * IMPLEMENTACIÓN: Requiere librería xmlsec para producción
     */
    private String firmarXmlConCertificado(String xml, Long idUsuario) {
        ConfiguracionSunat config = obtenerConfiguracion(idUsuario);
        
        if (config.getCertificadoDigital() == null) {
            throw new IllegalStateException("No hay certificado digital configurado");
        }
        
        // TODO: Implementar firma con xmlsec
        // Por ahora retorna XML sin firma (para pruebas)
        log.warn("⚠️  AVISO: Comprobante sin firma digital (requiere configuración de certificado en producción)");
        return xml;
    }
    
    /**
     * Envía comprobante a SUNAT
     */
    public void enviarAsunat(Long idComprobante) {
        log.info("Enviando comprobante a SUNAT: {}", idComprobante);
        
        ComprobanteSunat comprobante = comprobanteRepository.findById(idComprobante)
            .orElseThrow(() -> new ResourceNotFoundException("Comprobante no encontrado"));
        
        try {
            // TODO: Integración con API SUNAT
            // Ejemplo (requiere cliente HTTP):
            // SunatApiClient.enviar(comprobante.getXmlFirmado(), config.isPruebasMode());
            
            // Para pruebas
            simularEnvioSunat(comprobante);
            
            log.info("✅ Comprobante enviado a SUNAT: {}", idComprobante);
        } catch (Exception e) {
            log.error("❌ Error al enviar a SUNAT: {}", e.getMessage());
            comprobante.setEstado("RECHAZADO");
            comprobante.setMensajeError(e.getMessage());
            comprobanteRepository.save(comprobante);
            throw new RuntimeException("Error al enviar a SUNAT: " + e.getMessage());
        }
    }
    
    /**
     * Simula respuesta de SUNAT (para testing)
     */
    private void simularEnvioSunat(ComprobanteSunat comprobante) {
        comprobante.setTicketSunat("T" + System.currentTimeMillis());
        comprobante.setEstado("ACEPTADO");
        comprobante.setFechaRespuesta(LocalDateTime.now());
        comprobante.setRespuestaSunat("{\"status\": \"0\", \"message\": \"Aceptado\"}");
        comprobanteRepository.save(comprobante);
    }
    
    /**
     * Obtiene comprobantes pendientes de envío
     */
    @Transactional(readOnly = true)
    public List<ComprobanteSunat> obtenerComprobantesPendientes() {
        return comprobanteRepository.obtenerComprobantesPendientes();
    }
    
    /**
     * Obtiene comprobantes rechazados
     */
    @Transactional(readOnly = true)
    public List<ComprobanteSunat> obtenerComprobantesRechazados() {
        return comprobanteRepository.obtenerComprobantesRechazados();
    }
    
    /**
     * Obtiene resumen de estado de comprobantes
     */
    @Transactional(readOnly = true)
    public SunatResumenDTO obtenerResumen(Long idUsuario) {
        return SunatResumenDTO.builder()
            .aceptados(comprobanteRepository.countByEstado("ACEPTADO"))
            .rechazados(comprobanteRepository.countByEstado("RECHAZADO"))
            .pendientes(comprobanteRepository.obtenerComprobantesPendientes().size())
            .build();
    }
    
    /**
     * Escapa caracteres especiales XML
     */
    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }
    
    // ===== MÉTODOS PARA PRUEBAS (TEST) - SIN CORS =====
    
    /**
     * Genera token SUNAT desde el backend (sin CORS)
     * Proxy para evitar problemas de CORS en frontend
     */
    @Transactional(readOnly = true)
    public Object generarTokenDesdeBackend(String clientId, String clientSecret) {
        log.info("Generando token SUNAT desde backend");
        log.info("Client ID: {}, Client Secret: {}***", clientId, clientSecret.substring(0, Math.min(3, clientSecret.length())));
        
        try {
            String url = "https://api-seguridad.sunat.gob.pe/v1/clientesextranet/" + clientId + "/oauth2/token/";
            log.info("URL del token SUNAT: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            log.debug("Headers: Content-Type = {}", MediaType.APPLICATION_FORM_URLENCODED);
            
            // Preparar parámetros
            String body = "grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret;
            log.debug("Body request: grant_type=client_credentials&client_id={}...", clientId);
            
            HttpEntity<String> request = new HttpEntity<>(body, headers);
            
            log.info("Realizando petición POST a SUNAT...");
            // Hacer petición a SUNAT
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            log.info("Respuesta de SUNAT - Status: {}", response.getStatusCode());
            
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("✅ Token generado exitosamente");
                return response.getBody();
            } else {
                log.error("❌ Error generando token: {}", response.getStatusCode());
                return Map.of(
                    "error", "Error generando token", 
                    "status", response.getStatusCode().value(),
                    "body", response.getBody()
                );
            }
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("❌ Error HTTP 4xx en petición a SUNAT");
            log.error("   Status Code: {}", e.getStatusCode().value());
            log.error("   Response Body: {}", e.getResponseBodyAsString());
            log.error("   Mensaje: {}", e.getMessage());
            
            return Map.of(
                "success", false,
                "error", "Error en petición a SUNAT (4xx)",
                "httpStatus", e.getStatusCode().value(),
                "responseBody", e.getResponseBodyAsString()
            );
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("❌ Error HTTP 5xx en petición a SUNAT");
            log.error("   Status Code: {}", e.getStatusCode().value());
            log.error("   Response Body: {}", e.getResponseBodyAsString());
            log.error("   Mensaje: {}", e.getMessage());
            
            return Map.of(
                "success", false,
                "error", "Error en petición a SUNAT (5xx)",
                "httpStatus", e.getStatusCode().value(),
                "responseBody", e.getResponseBodyAsString()
            );
        } catch (Exception e) {
            log.error("❌ Excepción inesperada generando token: ", e);
            log.error("   Clase de excepción: {}", e.getClass().getSimpleName());
            log.error("   Mensaje: {}", e.getMessage());
            
            // Retornar objeto con error en lugar de lanzar excepción
            return Map.of(
                "success", false,
                "error", "Error generando token",
                "exceptionType", e.getClass().getSimpleName(),
                "message", e.getMessage() != null ? e.getMessage() : "Error desconocido"
            );
        }
    }
    
    /**
     * Valida comprobante SUNAT desde el backend (sin CORS)
     * Proxy para evitar problemas de CORS en frontend
     */
    @Transactional(readOnly = true)
    public Object validarComprobanteDesdeBackend(String token, SunatValidationRequestDTO request) {
        log.info("Validando comprobante desde backend: {}", request.getNumeroComprobante());
        
        try {
            String ruc = request.getNumRuc();
            String url = "https://api.sunat.gob.pe/v1/contribuyente/contribuyentes/" + ruc + "/validarcomprobante";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            
            // Preparar payload según manual SUNAT
            Map<String, Object> payload = new HashMap<>();
            payload.put("numRuc", request.getNumRuc());
            payload.put("codComp", request.getCodComp());
            payload.put("numeroSerie", request.getNumeroSerie());
            payload.put("numero", request.getNumero());
            payload.put("fechaEmision", request.getFechaEmision());
            payload.put("monto", request.getMonto());
            
            HttpEntity<Map> requestEntity = new HttpEntity<>(payload, headers);
            
            // Hacer petición a SUNAT
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Comprobante validado exitosamente");
                return response.getBody();
            } else {
                log.error("Error validando comprobante: {}", response.getStatusCode());
                return Map.of("error", "Error validando comprobante", "status", response.getStatusCode().value());
            }
            
        } catch (Exception e) {
            log.error("Excepción validando comprobante: {}", e.getMessage());
            throw new RuntimeException("Error validando comprobante: " + e.getMessage());
        }
    }
}
