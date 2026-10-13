package joseblanco.ejemploconexionhttp.datos;

import com.google.gson.annotations.SerializedName;

public class Mensaje {

    @SerializedName("mensaje")
    private String mensaje;

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
