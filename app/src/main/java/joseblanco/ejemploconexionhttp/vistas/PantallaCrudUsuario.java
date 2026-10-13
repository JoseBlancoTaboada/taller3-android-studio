package joseblanco.ejemploconexionhttp.vistas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
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

/**
 * Pantalla de datos del usuario. Sirve para dos cosas:
 * si llega sin usuario en el Intent registra uno nuevo (accion=Agregar); si llega con el usuario
 * que inició sesión, muestra sus datos y los modifica (accion=editar).
 */
public class PantallaCrudUsuario extends AppCompatActivity {

    private final ConexionHttpPostServer conexionServidor = new ConexionHttpPostServer();
    private EditText campoEmail;
    private EditText campoClave;
    private EditText campoNombre;
    private Button botonModificar;
    private Button botonCancelar;
    private ProgressBar progreso;
    /** null = registro nuevo; con datos = edición del usuario que inició sesión. */
    private Usuario sesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.pantalla_crud_usuario);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        campoEmail = findViewById(R.id.campoEmail);
        campoClave = findViewById(R.id.campoClave);
        campoNombre = findViewById(R.id.campoNombre);
        botonModificar = findViewById(R.id.botonModificar);
        botonCancelar = findViewById(R.id.botonCancelar);
        progreso = findViewById(R.id.progreso);

        sesion = IntentCompat.getSerializableExtra(getIntent(), PantallaInicio.EXTRA_SESION, Usuario.class);
        if (sesion != null) {
            campoEmail.setText(sesion.getEmail());
            campoEmail.setEnabled(false); // el email es la llave de la tabla: al editar no se cambia
            campoClave.setText(sesion.getPassword());
            campoNombre.setText(sesion.getNombre());
            botonModificar.setText(R.string.boton_modificar);
        } else {
            botonModificar.setText(R.string.boton_registrar);
        }

        botonCancelar.setOnClickListener(v -> finish());
        botonModificar.setOnClickListener(v -> guardar());
    }

    private void guardar() {
        String email = campoEmail.getText().toString().trim();
        String clave = campoClave.getText().toString();
        String nombre = campoNombre.getText().toString().trim();
        if (email.isEmpty()) {
            avisar(getString(R.string.error_falta_email));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            avisar(getString(R.string.error_email_invalido));
            return;
        }
        if (clave.isEmpty()) {
            avisar(getString(R.string.error_falta_clave));
            return;
        }
        if (nombre.isEmpty()) {
            avisar(getString(R.string.error_falta_nombre));
            return;
        }

        Map<String, String> parametros = new LinkedHashMap<>();
        parametros.put("accion", sesion == null ? "Agregar" : "editar");
        parametros.put("email", email);
        parametros.put("psw", clave);
        parametros.put("nombre", nombre);
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

    /** El servicio responde {"mensaje": "OK"} si guardó, o el motivo del fallo. */
    private void procesarRespuesta(String json) {
        try {
            Mensaje mensaje = new Gson().fromJson(json, Mensaje.class);
            if (mensaje != null && "OK".equalsIgnoreCase(mensaje.getMensaje())) {
                avisar(getString(R.string.aviso_guardado));
                // Igual que en la guía: tras guardar se vuelve al inicio de sesión.
                startActivity(new Intent(this, PantallaInicio.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return;
            }
            if (mensaje != null && mensaje.getMensaje() != null) {
                avisar(mensaje.getMensaje());
                return;
            }
        } catch (JsonParseException e) {
            // El cuerpo no era JSON: se avisa abajo.
        }
        avisar(getString(R.string.error_respuesta));
    }

    private void mostrarProgreso(boolean mostrar) {
        progreso.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        botonModificar.setEnabled(!mostrar);
        botonCancelar.setEnabled(!mostrar);
    }

    private void avisar(String texto) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show();
    }
}
