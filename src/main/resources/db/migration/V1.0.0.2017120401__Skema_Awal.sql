CREATE TABLE virtual_account (
  id             VARCHAR(36),
  number         VARCHAR(255)   NOT NULL,
  name           VARCHAR(255)   NOT NULL,
  account_type   VARCHAR(255)   NOT NULL,
  amount         NUMERIC(19, 2) NOT NULL,
  description    VARCHAR(255),
  email          VARCHAR(255),
  phone          VARCHAR(255),
  transaction_id VARCHAR(255)   NOT NULL,
  create_time    TIMESTAMP      NOT NULL,
  expire_date    DATE           NOT NULL,
  account_status VARCHAR(255)   NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE virtual_account_request (
  id             VARCHAR(36),
  request_type   VARCHAR(255)   NOT NULL,
  request_time   TIMESTAMP      NOT NULL,
  number         VARCHAR(255)   NOT NULL,
  name           VARCHAR(255)   NOT NULL,
  account_type   VARCHAR(255)   NOT NULL,
  amount         NUMERIC(19, 2) NOT NULL,
  description    VARCHAR(255),
  email          VARCHAR(255),
  phone          VARCHAR(255),
  expire_date    DATE           NOT NULL,
  request_status VARCHAR(255)   NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE payment (
  id                 VARCHAR(36),
  id_virtual_account VARCHAR(36)    NOT NULL,
  amount             NUMERIC(19, 2) NOT NULL,
  transaction_time   TIMESTAMP      NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (id_virtual_account) REFERENCES virtual_account (id)
);

create TABLE running_number(
  id VARCHAR (36),
  prefix VARCHAR(255) NOT NULL ,
  last_number bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE (prefix)
);