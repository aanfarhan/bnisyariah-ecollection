ALTER TABLE virtual_account_request
  ADD COLUMN invoice_number VARCHAR(255) NOT NULL;
ALTER TABLE virtual_account_request
  RENAME COLUMN number TO account_number;

ALTER TABLE virtual_account
  ADD COLUMN invoice_number VARCHAR(255) NOT NULL;
ALTER TABLE virtual_account
  RENAME COLUMN number TO account_number;
