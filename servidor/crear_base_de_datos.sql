-- Base de datos del taller. Se importa desde phpMyAdmin (pestaña "Importar") o desde la consola:
--   C:\xampp\mysql\bin\mysql.exe -u root < crear_base_de_datos.sql
CREATE DATABASE IF NOT EXISTS crudphpjson DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE crudphpjson;

CREATE TABLE IF NOT EXISTS Usuarios (
    email    VARCHAR(70)  NOT NULL,
    password VARCHAR(40)  NOT NULL,
    nombre   VARCHAR(100) NOT NULL,
    PRIMARY KEY (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
