package joseblanco.ejemploconexionhttp.controladores;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ConexionHttpPostServer {

    private static final int TIEMPO_CONEXION_MS = 5000;
    private static final int TIEMPO_LECTURA_MS = 10000;

    public String conexionConElServidor(Map<String, String> parametros, String rutaDeLaAplicacionWeb)
            throws IOException {
        byte[] cuerpo = codificar(parametros).getBytes(StandardCharsets.UTF_8);

        HttpURLConnection conexion = (HttpURLConnection) new URL(rutaDeLaAplicacionWeb).openConnection();
        try {
            conexion.setRequestMethod("POST");
            conexion.setConnectTimeout(TIEMPO_CONEXION_MS);
            conexion.setReadTimeout(TIEMPO_LECTURA_MS);
            conexion.setDoOutput(true);
            conexion.setFixedLengthStreamingMode(cuerpo.length);
            conexion.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            try (OutputStream salida = conexion.getOutputStream()) {
                salida.write(cuerpo);
            }

            int codigo = conexion.getResponseCode();
            if (codigo != HttpURLConnection.HTTP_OK) {
                throw new IOException("El servidor respondió con el código HTTP " + codigo);
            }
            try (InputStream entrada = conexion.getInputStream()) {
                return leer(entrada);
            }
        } finally {
            conexion.disconnect();
        }
    }

    static String codificar(Map<String, String> parametros) throws UnsupportedEncodingException {
        StringBuilder texto = new StringBuilder();
        for (Map.Entry<String, String> parametro : parametros.entrySet()) {
            if (texto.length() > 0) {
                texto.append('&');
            }
            texto.append(URLEncoder.encode(parametro.getKey(), "UTF-8"))
                    .append('=')
                    .append(URLEncoder.encode(parametro.getValue(), "UTF-8"));
        }
        return texto.toString();
    }

    private static String leer(InputStream entrada) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] bloque = new byte[4096];
        int leidos;
        while ((leidos = entrada.read(bloque)) != -1) {
            bytes.write(bloque, 0, leidos);
        }
        return bytes.toString("UTF-8");
    }
}
