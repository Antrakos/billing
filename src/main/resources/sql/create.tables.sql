CREATE TABLE "customers" (
  "id"      SERIAL       NOT NULL,
  "balance" FLOAT        NOT NULL,
  "name"    VARCHAR(255) NOT NULL,
  "address" VARCHAR(255) NOT NULL,
  CONSTRAINT customers_pk PRIMARY KEY ("id")
);
CREATE TABLE "services" (
  "id"      SERIAL  NOT NULL,
  "price"   FLOAT   NOT NULL,
  "enabled" BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT services_pk PRIMARY KEY ("id")
);
CREATE TABLE "customers_services_mapping" (
  "id"          SERIAL  NOT NULL,
  "customer_id" INTEGER NOT NULL,
  "service_id"  INTEGER NOT NULL,
  "active"      BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT customers_services_mapping_pk PRIMARY KEY ("id")
);
CREATE TABLE "service_usages" (
  "id"                  SERIAL  NOT NULL,
  "customer_service_id" INTEGER NOT NULL,
  "date"                DATE    NOT NULL,
  "value"               FLOAT   NOT NULL,
  CONSTRAINT service_usages_pk PRIMARY KEY ("id")
);
CREATE TABLE "bills" (
  "id"                  SERIAL  NOT NULL,
  "date"                DATE    NOT NULL,
  "amount"              FLOAT   NOT NULL,
  "customer_service_id" INTEGER NOT NULL,
  "paid"                BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT bills_pk PRIMARY KEY ("id")
);
CREATE TABLE "users" (
  "id"          SERIAL  NOT NULL,
  "username"    TEXT    NOT NULL UNIQUE,
  "password"    TEXT    NOT NULL,
  "role"        TEXT    NOT NULL,
  "enabled"     BOOLEAN NOT NULL,
  "customer_id" INTEGER,
  CONSTRAINT users_pk PRIMARY KEY ("id")
);

ALTER TABLE "customers_services_mapping"
  ADD CONSTRAINT "customers_services_mapping_fk0" FOREIGN KEY ("customer_id") REFERENCES "customers" ("id");
ALTER TABLE "customers_services_mapping"
  ADD CONSTRAINT "customers_services_mapping_fk1" FOREIGN KEY ("service_id") REFERENCES "services" ("id");
ALTER TABLE "service_usages"
  ADD CONSTRAINT "service_usages_fk0" FOREIGN KEY ("customer_service_id") REFERENCES "customers_services_mapping" ("id");
ALTER TABLE "bills"
  ADD CONSTRAINT "bills_fk0" FOREIGN KEY ("customer_service_id") REFERENCES "customers_services_mapping" ("id");
ALTER TABLE "users"
  ADD CONSTRAINT "users_fk0" FOREIGN KEY ("customer_id") REFERENCES "customers" ("id");

