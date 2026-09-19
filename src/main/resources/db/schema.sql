PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS administrador (
    id_administrador INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario TEXT NOT NULL UNIQUE,
    contrasena_hash TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS restaurante (
    id_restaurante INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS producto (
    id_producto INTEGER PRIMARY KEY AUTOINCREMENT,
    id_restaurante INTEGER NOT NULL,
    nombre TEXT NOT NULL,
    descripcion TEXT,
    precio REAL NOT NULL CHECK (precio >= 0),
    disponible INTEGER NOT NULL DEFAULT 1 CHECK (disponible IN (0, 1)),

    FOREIGN KEY (id_restaurante)
        REFERENCES restaurante(id_restaurante)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

INSERT OR IGNORE INTO restaurante (
    id_restaurante,
    nombre
) VALUES
    (1, 'Comida guatemalteca'),
    (2, 'Pizzería'),
    (3, 'Hamburguesería');

INSERT OR IGNORE INTO producto (
    id_producto,
    id_restaurante,
    nombre,
    descripcion,
    precio,
    disponible
) VALUES
    (1, 1, 'Pepián', 'Platillo tradicional guatemalteco.', 45.00, 1),
    (2, 1, 'Kak ik', 'Caldo tradicional guatemalteco.', 40.00, 1),
    (3, 1, 'Tamal colorado', 'Tamal tradicional con recado.', 18.00, 1),

    (4, 2, 'Pizza Pepperoni', 'Pizza con pepperoni y queso.', 65.00, 1),
    (5, 2, 'Pizza Hawaiana', 'Pizza con jamón y piña.', 70.00, 1),
    (6, 2, 'Pizza Suprema', 'Pizza con vegetales y carnes.', 75.00, 1),

    (7, 3, 'Hamburguesa Clásica', 'Carne, queso, lechuga y tomate.', 35.00, 1),
    (8, 3, 'Hamburguesa Doble', 'Doble carne y doble queso.', 48.00, 1),
    (9, 3, 'Papas Fritas', 'Porción de papas fritas.', 18.00, 1);

CREATE TABLE IF NOT EXISTS pedido (
    id_pedido INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_cliente TEXT NOT NULL,
    telefono TEXT NOT NULL,
    persona_recoge TEXT NOT NULL,
    hora_recogida TEXT NOT NULL,
    indicaciones TEXT,
    fecha_pedido TEXT NOT NULL,
    total REAL NOT NULL CHECK (total >= 0),
    estado TEXT NOT NULL DEFAULT 'PAGADO'
);

CREATE TABLE IF NOT EXISTS detalle_pedido (
    id_detalle INTEGER PRIMARY KEY AUTOINCREMENT,
    id_pedido INTEGER NOT NULL,
    id_producto INTEGER NOT NULL,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario REAL NOT NULL CHECK (precio_unitario >= 0),
    subtotal REAL NOT NULL CHECK (subtotal >= 0),

    FOREIGN KEY (id_pedido)
        REFERENCES pedido(id_pedido)
        ON DELETE CASCADE,

    FOREIGN KEY (id_producto)
        REFERENCES producto(id_producto)
);