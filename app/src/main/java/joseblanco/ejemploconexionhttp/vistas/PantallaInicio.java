package joseblanco.ejemploconexionhttp.vistas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.util.LinkedHashMap;
import java.util.Map;

import joseblanco.ejemploconexionhttp.R;
import joseblanco.ejemploconexionhttp.controladores.ConexionHttpPostServer;
import joseblanco.ejemploconexionhttp.controladores.SegundoPlano;
import joseblanco.ejemploconexionhttp.datos.Mensaje;
import joseblanco.ejemploconexionhttp.datos.Usuario;

/** Pantalla de inicio de sesión: valida el email y el password contra el servicio PHP. */
public class PantallaInicio extends AppCompatActivity {

    /** Clave con la que viaja el Usuario de una pantalla a otra dentro del Intent. */
    public static final String EXTRA_SESION = "sesion";

    private final ConexionHttpPostServer conexionServidor = new ConexionHttpPostServer();
    private EditText campoEmail;
    private EditText campoClave;
    private Button btnRegistrarse;
    private Button btnIniciarSesion;
    private ProgressBar progreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.pantalla_inicio);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        campoEmail = findViewById(R.id.campoEmail);
        campoClave = findViewById(R.id.campoClave);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        progreso = findViewById(R.id.progreso);

        // Sin usuario en el Intent, la pantalla de datos funciona como registro nuevo.
        btnRegistrarse.setOnClickListener(v -> startActivity(new Intent(this, PantallaCrudUsuario.class)));
        btnIniciarSesion.setOnClickListener(v -> iniciarSesion());
    }

    private void iniciarSesion() {
        String email = campoEmail.getText().toString().trim();
        String clave = campoClave.getText().toString();
        if (email.isEmpty()) {
            avisar(getString(R.string.error_falta_email));
            return;
        }
        if (clave.isEmpty()) {
            avisar(getString(R.string.error_falta_clave));
            return;
        }

        Map<String, String> parametros = new LinkedHashMap<>();
        parametros.put("accion", "login");
        parametros.put("email", email);
        parametros.put("psw", clave);
        String direccion = getString(R.string.url_servidor);

        mostrarProgreso(true);
        SegundoPlano.ejecutar(
                () -> conexionServidor.conexionConElServidor(parametros, direccion),
                (json, error) -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    mostrarProgreso(false);
                    if (error != null) {
                        String detalle = error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName();
                        avisar(getString(R.string.error_conexion, detalle));
                    } else {
                        procesarRespuesta(json);
                    }
                });
    }

    /** El servicio responde el usuario completo si el acceso es correcto, o {"mensaje": "..."} si no. */
    private void procesarRespuesta(String json) {
        try {
            Gson gson = new Gson();
            Usuario usuario = gson.fromJson(json, Usuario.class);
            if (usuario != null && usuario.getEmail() != null) {
                campoClave.setText("");
                startActivity(new Intent(this, PantallaListado.class).putExtra(EXTRA_SESION, usuario));
                return;
            }
            Mensaje mensaje = gson.fromJson(json, Mensaje.class);
            if (mensaje != null && mensaje.getMensaje() != null) {
                avisar(mensaje.getMensaje());
                return;
            }
        } catch (JsonParseException e) {
            // El cuerpo no era JSON (por ejemplo, una página de error de PHP): se avisa abajo.
        }
        avisar(getString(R.string.error_respuesta));
    }

    private void mostrarProgreso(boolean mostrar) {
        progreso.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        btnIniciarSesion.setEnabled(!mostrar);
        btnRegistrarse.setEnabled(!mostrar);
    }

    private void avisar(String texto) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show();
    }
}
