CREATE TABLE users (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    name VARCHAR(100) NOT NULL ,
    lastname VARCHAR(100) NOT NULL ,
    email VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 DEFAULT SYSDATETIME()
);

INSERT INTO users (name, lastname,email, username, password, role)
VALUES ('Luis','Palacio', 'palaciodimasluisenrique@gmail.com', 'luispalacio','$2a$10$yRRay5p50we.tEa5TvtBp.L93pUndFF.NWh5sDLPtduyps7eefSQG', 'ADMIN');