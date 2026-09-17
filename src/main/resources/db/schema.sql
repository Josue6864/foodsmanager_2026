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