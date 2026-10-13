package joseblanco.ejemploconexionhttp.datos;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Entidad de datos: un usuario tal como lo guarda y lo devuelve el servicio (tabla Usuarios).
 * Es Serializable para poder viajar de una pantalla a otra dentro de un Intent.
 */
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @SerializedName("email")
    private String email;
    @SerializedName("password")
    private String password;
    @SerializedName("nombre")
    private String nombre;

    public Usuario() {
    }

    public Usuario(String email, String password, String nombre) {
        this.email = email;
        this.password = password;
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        // La contraseña no se imprime para que no aparezca en el Logcat.
        return "Usuario{email='" + email + "', nombre='" + nombre + "'}";
    }
}
