package joseblanco.ejemploconexionhttp.datos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/** Comprueba que Gson entiende el JSON real que devuelve operacion.php. */
public class ModelosJsonTest {

    private final Gson gson = new Gson();

    @Test
    public void leeUnUsuarioConTildesEscapadasPorPhp() {
        // json_encode de PHP escribe "José" como José
        String json = "{\"email\":\"ana@ejemplo.com\",\"password\":\"1234\",\"nombre\":\"Jos\\u00e9 \\u00d1and\\u00fa\"}";

        Usuario usuario = gson.fromJson(json, Usuario.class);

        assertEquals("ana@ejemplo.com", usuario.getEmail());
        assertEquals("1234", usuario.getPassword());
        assertEquals("José Ñandú", usuario.getNombre());
    }

    @Test
    public void leeLaListaDeUsuarios() {
        String json = "[{\"email\":\"a@x.com\",\"password\":\"1\",\"nombre\":\"Ana\"},"
                + "{\"email\":\"b@x.com\",\"password\":\"2\",\"nombre\":\"Beto\"}]";

        List<Usuario> usuarios = gson.fromJson(json, new TypeToken<List<Usuario>>() { }.getType());

        assertEquals(2, usuarios.size());
        assertEquals("Beto", usuarios.get(1).getNombre());
    }

    @Test
    public void unMensajeNoTrazaEmailAsiLaPantallaLoDistingueDeUnUsuario() {
        String json = "{\"mensaje\":\"Acceso denegado\"}";

        assertNull(gson.fromJson(json, Usuario.class).getEmail());
        assertEquals("Acceso denegado", gson.fromJson(json, Mensaje.class).getMensaje());
    }

    @Test
    public void elUsuarioSobreviveAlViajeDentroDeUnIntent() throws Exception {
        // Un Intent copia sus extras Serializable de esta misma forma
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream salida = new ObjectOutputStream(bytes)) {
            salida.writeObject(new Usuario("ana@ejemplo.com", "1234", "Ana"));
        }

        try (ObjectInputStream entrada = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            Usuario copia = (Usuario) entrada.readObject();
            assertNotNull(copia);
            assertEquals("ana@ejemplo.com", copia.getEmail());
            assertEquals("Ana", copia.getNombre());
        }
    }
}
