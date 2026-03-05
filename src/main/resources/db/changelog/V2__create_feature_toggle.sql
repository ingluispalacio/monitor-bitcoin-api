CREATE TABLE feature_toggle (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    module_name VARCHAR(100) NOT NULL,
    active BIT NOT NULL
);

INSERT INTO feature_toggle (module_name, active)
VALUES ('CRYPTO_MONITOR', 1);