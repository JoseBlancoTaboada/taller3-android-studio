<?php

$bd;

function conectar() {
    try {
        global $bd;
        $bd = new mysqli("localhost", "root", "", "crudphpjson");
    } catch (Exception $error) {
        throw new Exception($error->getMessage());
    }
}

function consultar($sql) {
    global $bd;
    $res = NULL;
    try {
        if ($bd == NULL) {
            conectar();
        }
        return $bd->query($sql);
    } catch (Exception $error) {
        throw new Exception($error->getMessage());
    }
}
