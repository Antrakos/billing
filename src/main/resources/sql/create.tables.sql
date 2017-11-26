CREATE TABLE "customers" (
	"id" serial NOT NULL,
	"balance" FLOAT NOT NULL,
	CONSTRAINT customers_pk PRIMARY KEY ("id")
) WITH (
  OIDS=FALSE
);
CREATE TABLE "services" (
	"id" serial NOT NULL,
	"price" FLOAT NOT NULL,
	"enabled" BOOLEAN NOT NULL DEFAULT TRUE,
	CONSTRAINT services_pk PRIMARY KEY ("id")
) WITH (
  OIDS=FALSE
);
CREATE TABLE "customers_services_mapping" (
	"id" serial NOT NULL,
	"customer_id" integer NOT NULL,
	"service_id" integer NOT NULL,
	CONSTRAINT customers_services_mapping_pk PRIMARY KEY ("id")
) WITH (
  OIDS=FALSE
);
CREATE TABLE "service_usages" (
	"id" serial NOT NULL,
	"customer_service_id" integer NOT NULL,
	"date" DATE NOT NULL,
	"value" FLOAT NOT NULL,
	CONSTRAINT service_usages_pk PRIMARY KEY ("id")
) WITH (
  OIDS=FALSE
);
CREATE TABLE "bills" (
	"id" serial NOT NULL,
	"date" DATE NOT NULL,
	"amount" FLOAT NOT NULL,
	"customer_service_id" integer NOT NULL,
	"paid" BOOLEAN NOT NULL DEFAULT FALSE,
	CONSTRAINT bills_pk PRIMARY KEY ("id")
) WITH (
  OIDS=FALSE
);

ALTER TABLE "customers_services_mapping" ADD CONSTRAINT "customers_services_mapping_fk0" FOREIGN KEY ("customer_id") REFERENCES "customers"("id");
ALTER TABLE "customers_services_mapping" ADD CONSTRAINT "customers_services_mapping_fk1" FOREIGN KEY ("service_id") REFERENCES "services"("id");
ALTER TABLE "service_usages" ADD CONSTRAINT "service_usages_fk0" FOREIGN KEY ("customer_service_id") REFERENCES "customers_services_mapping"("id");
ALTER TABLE "bills" ADD CONSTRAINT "bills_fk0" FOREIGN KEY ("customer_service_id") REFERENCES "customers_services_mapping"("id");

