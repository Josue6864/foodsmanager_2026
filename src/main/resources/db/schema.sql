CREATE TABLE IF NOT EXISTS restaurante (
    id_restaurante INTEGER PRIMARY KEY,
    nombre TEXT NOT NULL
        CHECK (length(trim(nombre)) > 0),
    ubicacion TEXT NOT NULL DEFAULT ''
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

    FOREIGN KEY (id_restaurante)
        REFERENCES restaurante(id_restaurante)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_producto_restaurante
    ON producto(id_restaurante);