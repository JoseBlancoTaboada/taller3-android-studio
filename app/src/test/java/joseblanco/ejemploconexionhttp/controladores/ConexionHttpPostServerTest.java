package joseblanco.ejemploconexionhttp.controladores;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ConexionHttpPostServerTest {

    private ServerSocket servidor;
    private String direccion;
    private volatile String metodoRecibido;
    private volatile String tipoRecibido;
    private volatile String cuerpoRecibido;
    private volatile int codigoRespuesta;
    private volatile String respuesta;

    @Before
    public void iniciarServidor() throws IOException {
        codigoRespuesta = 200;
        respuesta = "{\"mensaje\":\"OK\"}";
        servidor = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        direccion = "http://127.0.0.1:" + servidor.getLocalPort() + "/operacion.php";
        Thread hilo = new Thread(this::atenderUnaPeticion);
        hilo.setDaemon(true);
        hilo.start();
    }

    @After
    public void detenerServidor() throws IOException {
        servidor.close();
    }

    private void atenderUnaPeticion() {
        try (Socket cliente = servidor.accept()) {
            InputStream entrada = cliente.getInputStream();
            String[] lineas = leerCabecera(entrada).split("\r\n");
            metodoRecibido = lineas[0].split(" ")[0];
            int largo = 0;
            for (String linea : lineas) {
                String minusculas = linea.toLowerCase(Locale.ROOT);
                if (minusculas.startsWith("content-type:")) {
                    tipoRecibido = linea.substring(linea.indexOf(':') + 1).trim();
                } else if (minusculas.startsWith("content-length:")) {
                    largo = Integer.parseInt(linea.substring(linea.indexOf(':') + 1).trim());
                }
            }
            byte[] cuerpo = new byte[largo];
            int leidos = 0;
            while (leidos < largo) {
                int n = entrada.read(cuerpo, leidos, largo - leidos);
                if (n == -1) {
                    break;
                }
                leidos += n;
            }
            cuerpoRecibido = new String(cuerpo, 0, leidos, StandardCharsets.UTF_8);

            byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);
            String encabezado = "HTTP/1.1 " + codigoRespuesta + " Respuesta\r\n"
                    + "Content-Type: application/json; charset=UTF-8\r\n"
                    + "Content-Length: " + bytes.length + "\r\n"
                    + "Connection: close\r\n\r\n";
            OutputStream salida = cliente.getOutputStream();
            salida.write(encabezado.getBytes(StandardCharsets.US_ASCII));
            salida.write(bytes);
            salida.flush();
        } catch (IOException e) {
        }
    }

    private static String leerCabecera(InputStream entrada) throws IOException {
        StringBuilder cabecera = new StringBuilder();
        int b;
        while ((b = entrada.read()) != -1) {
            cabecera.append((char) b);
            int n = cabecera.length();
            if (n >= 4 && cabecera.substring(n - 4).equals("\r\n\r\n")) {
                break;
            }
        }
        return cabecera.toString();
    }

    @Test
    public void enviaElFormularioPorPostEnUtf8() throws IOException {
        Map<String, String> parametros = new LinkedHashMap<>();
        parametros.put("accion", "Agregar");
        parametros.put("email", "ana+prueba@ejemplo.com");
        parametros.put("psw", "p&ss=1");
        parametros.put("nombre", "José Ñandú");

        new ConexionHttpPostServer().conexionConElServidor(parametros, direccion);

        assertEquals("POST", metodoRecibido);
        assertTrue(tipoRecibido.startsWith("application/x-www-form-urlencoded"));
        assertTrue(tipoRecibido.toLowerCase(Locale.ROOT).contains("charset=utf-8"));
        assertTrue(cuerpoRecibido.contains("nombre=Jos%C3%A9+%C3%91and%C3%BA"));
        Map<String, String> recibidos = new LinkedHashMap<>();
        for (String par : cuerpoRecibido.split("&")) {
            String[] partes = par.split("=", 2);
            recibidos.put(URLDecoder.decode(partes[0], "UTF-8"), URLDecoder.decode(partes[1], "UTF-8"));
        }
        assertEquals(parametros, recibidos);
        assertTrue(cuerpoRecibido.startsWith("accion=Agregar&email="));
    }

    @Test
    public void devuelveElCuerpoDecodificadoComoUtf8() throws IOException {
        respuesta = "{\"nombre\":\"José Ñandú\"}";

        String texto = new ConexionHttpPostServer().conexionConElServidor(new LinkedHashMap<>(), direccion);

        assertEquals("{\"nombre\":\"José Ñandú\"}", texto);
    }

    @Test
    public void unaRespuestaVaciaDevuelveTextoVacio() throws IOException {
        respuesta = "";

        String texto = new ConexionHttpPostServer().conexionConElServidor(new LinkedHashMap<>(), direccion);

        assertEquals("", texto);
    }

    @Test
    public void siElServidorNoRespondeOkSeLanzaUnaExcepcionConElCodigo() {
        codigoRespuesta = 500;
        respuesta = "error";

        try {
            new ConexionHttpPostServer().conexionConElServidor(new LinkedHashMap<>(), direccion);
            fail("Debía lanzar IOException");
        } catch (IOException esperada) {
            assertTrue(esperada.getMessage().contains("500"));
        }
    }
}
