DROP TABLE transaction IF EXISTS;

CREATE TABLE transaction
(
    id            BIGINT IDENTITY NOT NULL PRIMARY KEY,
    reference     INTEGER,
    accountNumber VARCHAR(20),
    description   VARCHAR(150),
    startBalance  DECIMAL(8, 2),
    mutation      DECIMAL(8, 2),
    endBalance    DECIMAL(8, 2),
    valid         INT
)
