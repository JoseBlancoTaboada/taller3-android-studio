package joseblanco.ejemploconexionhttp.vistas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import joseblanco.ejemploconexionhttp.R;
import joseblanco.ejemploconexionhttp.controladores.ConexionHttpPostServer;
import joseblanco.ejemploconexionhttp.controladores.SegundoPlano;
import joseblanco.ejemploconexionhttp.datos.Mensaje;
import joseblanco.ejemploconexionhttp.datos.Usuario;

/** Pantalla de listado: pide todos los usuarios al servicio (accion=listar) y los muestra en un ListView. */
public class PantallaListado extends AppCompatActivity {

    private final ConexionHttpPostServer conexionServidor = new ConexionHttpPostServer();
    private ArrayAdapter<String> items;
    private ProgressBar progreso;
    /** El usuario que inició sesión; se le pasa a la pantalla de edición. */
    private Usuario sesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.pantalla_listado);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sesion = IntentCompat.getSerializableExtra(getIntent(), PantallaInicio.EXTRA_SESION, Usuario.class);
        ListView listaUsuarios = findViewById(R.id.listaUsuarios);
        Button btnEditarDatos = findViewById(R.id.btnEditarDatos);
        progreso = findViewById(R.id.progreso);

        items = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listaUsuarios.setAdapter(items);
        btnEditarDatos.setOnClickListener(v ->
                startActivity(new Intent(this, PantallaCrudUsuario.class).putExtra(PantallaInicio.EXTRA_SESION, sesion)));

        cargarUsuarios();
    }

    private void cargarUsuarios() {
        Map<String, String> parametros = new LinkedHashMap<>();
        parametros.put("accion", "listar");
        String direccion = getString(R.string.url_servidor);

        progreso.setVisibility(View.VISIBLE);
        SegundoPlano.ejecutar(
                () -> conexionServidor.conexionConElServidor(parametros, direccion),
                (json, error) -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    progreso.setVisibility(View.GONE);
                    if (error != null) {
                        String detalle = error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName();
                        avisar(getString(R.string.error_conexion, detalle));
                    } else {
                        mostrarUsuarios(json);
                    }
                });
    }

    /** El servicio responde un arreglo de usuarios, o {"mensaje": "No hay Usuarios"} si la tabla está vacía. */
    private void mostrarUsuarios(String json) {
        try {
            Gson gson = new Gson();
            JsonElement raiz = JsonParser.parseString(json);
            if (raiz.isJsonArray()) {
                List<Usuario> usuarios = gson.fromJson(raiz, new TypeToken<List<Usuario>>() { }.getType());
                items.clear();
                for (Usuario usuario : usuarios) {
                    items.add(getString(R.string.formato_usuario, usuario.getEmail(), usuario.getNombre()));
                }
                return;
            }
            Mensaje mensaje = gson.fromJson(raiz, Mensaje.class);
            if (mensaje != null && mensaje.getMensaje() != null) {
                avisar(mensaje.getMensaje());
                return;
            }
        } catch (JsonParseException e) {
            // El cuerpo no era JSON: se avisa abajo.
        }
        avisar(getString(R.string.error_respuesta));
    }

    private void avisar(String texto) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show();
    }
}
