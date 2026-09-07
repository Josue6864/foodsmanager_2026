PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS restaurante (
    id_restaurante INTEGER PRIMARY KEY,
    nombre TEXT NOT NULL
        COLLATE NOCASE
        UNIQUE
        CHECK (length(trim(nombre)) > 0),
    ubicacion TEXT NOT NULL
        CHECK (length(trim(ubicacion)) > 0)
);

CREATE TABLE IF NOT EXISTS administrador (
    id_administrador INTEGER PRIMARY KEY,
    usuario TEXT NOT NULL
        COLLATE NOCASE
        UNIQUE
        CHECK (length(trim(usuario)) > 0),
    contrasena_hash TEXT NOT NULL
        CHECK (length(trim(contrasena_hash)) > 0)
);

CREATE TABLE IF NOT EXISTS producto (
    id_producto INTEGER PRIMARY KEY,
    id_restaurante INTEGER NOT NULL,
    nombre TEXT NOT NULL
        CHECK (length(trim(nombre)) > 0),
    descripcion TEXT NOT NULL DEFAULT '',
    precio REAL NOT NULL
        CHECK (precio >= 0),
    disponible INTEGER NOT NULL DEFAULT 1
        CHECK (disponible IN (0, 1)),

    CONSTRAINT fk_producto_restaurante
        FOREIGN KEY (id_restaurante)
        REFERENCES restaurante(id_restaurante)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_producto_restaurante_nombre
    ON producto(id_restaurante, nombre COLLATE NOCASE);


/*
  Aca creamos los restaurantes iniciales.
  INSERT OR IGNORE evita duplicarlos cuando se vuelve
  a ejecutar el inicializador.
 */
INSERT OR IGNORE INTO restaurante (
    id_restaurante,
    nombre,
    ubicacion
) VALUES
    (1, 'Sabor Chapín', 'Zona 1'),
    (2, 'Pizzería Central', 'Zona 4'),
    (3, 'Burger House', 'Zona 10');