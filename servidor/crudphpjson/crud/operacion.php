<?php
require_once '../bd/conexion_bd.php';
$accion = @$_REQUEST["accion"];
switch ($accion) {
    case "login":
        login();
        break;
    case "Agregar":
        guardar();
        break;
    case "editar":
        guardar();
        break;
    case "listar":
        listar();
        break;
    case "eliminar":
        eliminar();
        break;
    default:
        break;
}

function login() {
    $email = @$_REQUEST["email"];
    $pass = @$_REQUEST["psw"];
    try {
        $res = consultar("SELECT * FROM Usuarios WHERE email ='$email' AND password='$pass'");

        if ($res != NULL && $res->num_rows > 0) {
            $json = json_encode($res->fetch_assoc());
            echo $json;
            /* liberar el conjunto de resultados */
            $res->free();
        } else {
            echo json_encode(array("mensaje" => "Acceso denegado"));
        }
    } catch (Exception $error) {
        echo json_encode(array("mensaje" => $error->getTraceAsString()));
    }
}

function eliminar() {
    $email = @$_REQUEST["email"];
    try {
        $res = consultar("SELECT * FROM Usuarios WHERE email = '$email'");
        if ($res != NULL && $res->num_rows > 0) {
            $res = consultar("DELETE FROM Usuarios WHERE email ='$email'");
            echo json_encode(array("mensaje" => "OK"));
        } else {
            echo json_encode(array("mensaje" => "Usuario no existe"));
        }
    } catch (Exception $error) {
        echo json_encode(array("mensaje" => $error->getMessage()));
    }
}

function guardar() {
    $accion = @$_REQUEST["accion"];
    $email = @$_REQUEST["email"];
    $pass = @$_REQUEST["psw"];
    $nombre = @$_REQUEST["nombre"];
    try {
        $res = consultar("SELECT * FROM Usuarios WHERE email = '$email'");
        if ($res != NULL && $res->num_rows > 0) {
            // Agregar no debe pisar a un usuario que ya existe (la guía lo sobrescribía en silencio).
            if ($accion == "Agregar") {
                echo json_encode(array("mensaje" => "El usuario ya existe"));
                return;
            }
            $res = consultar("UPDATE Usuarios SET password ='$pass', nombre = '$nombre' WHERE email = '$email'");
        } else {
            $res = consultar("INSERT INTO Usuarios VALUES('$email','$pass', '$nombre')");
        }
        echo json_encode(array("mensaje" => "OK"));
    } catch (Exception $error) {
        echo json_encode(array("mensaje" => "Usuario No Registrado"));
    }
}

function listar() {
    try {
        $res = consultar("SELECT * FROM Usuarios");
        if ($res != NULL && $res->num_rows > 0) {
            $json = json_encode($res->fetch_all(1));
            echo $json;
            /* liberar el conjunto de resultados */
            $res->free();
        } else {
            echo json_encode(array("mensaje" => "No hay Usuarios"));
        }
    } catch (Exception $error) {
        echo json_encode(array("mensaje" => $error->getTraceAsString()));
    }
}
