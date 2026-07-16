ALTER TABLE app_document
    ADD COLUMN IF NOT EXISTS storage_provider VARCHAR(30) NOT NULL DEFAULT 'LOCAL';

ALTER TABLE product
    ADD COLUMN IF NOT EXISTS image_document_id UUID;

ALTER TABLE inquiry
    ADD COLUMN IF NOT EXISTS id_proof_document_id UUID,
    ADD COLUMN IF NOT EXISTS payment_screenshot_document_id UUID,
    ADD COLUMN IF NOT EXISTS aadhaar_document_id UUID,
    ADD COLUMN IF NOT EXISTS land_proof_document_id UUID,
    ADD COLUMN IF NOT EXISTS bank_passbook_document_id UUID,
    ADD COLUMN IF NOT EXISTS hub_document_id UUID;

UPDATE product
SET image_document_id = substring(image_url from '/api/documents/([0-9a-fA-F-]{36})/content')::uuid
WHERE image_document_id IS NULL
  AND image_url ~ '/api/documents/[0-9a-fA-F-]{36}/content';

UPDATE product
SET image_document_id = substring(image_url from '/api/documents/public/products/([0-9a-fA-F-]{36})/content')::uuid
WHERE image_document_id IS NULL
  AND image_url ~ '/api/documents/public/products/[0-9a-fA-F-]{36}/content';

UPDATE inquiry
SET id_proof_document_id = substring(id_proof_metadata from '"documentId":"([0-9a-fA-F-]{36})"')::uuid
WHERE id_proof_document_id IS NULL
  AND id_proof_metadata IS NOT NULL
  AND id_proof_metadata ~ '"documentId":"[0-9a-fA-F-]{36}"';

UPDATE inquiry
SET payment_screenshot_document_id = substring(payment_screenshot_metadata from '"documentId":"([0-9a-fA-F-]{36})"')::uuid
WHERE payment_screenshot_document_id IS NULL
  AND payment_screenshot_metadata IS NOT NULL
  AND payment_screenshot_metadata ~ '"documentId":"[0-9a-fA-F-]{36}"';

UPDATE inquiry
SET aadhaar_document_id = substring(aadhaar_document_metadata from '"documentId":"([0-9a-fA-F-]{36})"')::uuid
WHERE aadhaar_document_id IS NULL
  AND aadhaar_document_metadata IS NOT NULL
  AND aadhaar_document_metadata ~ '"documentId":"[0-9a-fA-F-]{36}"';

UPDATE inquiry
SET land_proof_document_id = substring(land_proof_document_metadata from '"documentId":"([0-9a-fA-F-]{36})"')::uuid
WHERE land_proof_document_id IS NULL
  AND land_proof_document_metadata IS NOT NULL
  AND land_proof_document_metadata ~ '"documentId":"[0-9a-fA-F-]{36}"';

UPDATE inquiry
SET bank_passbook_document_id = substring(bank_passbook_document_metadata from '"documentId":"([0-9a-fA-F-]{36})"')::uuid
WHERE bank_passbook_document_id IS NULL
  AND bank_passbook_document_metadata IS NOT NULL
  AND bank_passbook_document_metadata ~ '"documentId":"[0-9a-fA-F-]{36}"';

UPDATE inquiry
SET hub_document_id = substring(hub_document_metadata from '"documentId":"([0-9a-fA-F-]{36})"')::uuid
WHERE hub_document_id IS NULL
  AND hub_document_metadata IS NOT NULL
  AND hub_document_metadata ~ '"documentId":"[0-9a-fA-F-]{36}"';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_product_image_document'
    ) THEN
        ALTER TABLE product
            ADD CONSTRAINT fk_product_image_document
            FOREIGN KEY (image_document_id) REFERENCES app_document(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_inquiry_id_proof_document') THEN
        ALTER TABLE inquiry ADD CONSTRAINT fk_inquiry_id_proof_document
            FOREIGN KEY (id_proof_document_id) REFERENCES app_document(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_inquiry_payment_screenshot_document') THEN
        ALTER TABLE inquiry ADD CONSTRAINT fk_inquiry_payment_screenshot_document
            FOREIGN KEY (payment_screenshot_document_id) REFERENCES app_document(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_inquiry_aadhaar_document') THEN
        ALTER TABLE inquiry ADD CONSTRAINT fk_inquiry_aadhaar_document
            FOREIGN KEY (aadhaar_document_id) REFERENCES app_document(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_inquiry_land_proof_document') THEN
        ALTER TABLE inquiry ADD CONSTRAINT fk_inquiry_land_proof_document
            FOREIGN KEY (land_proof_document_id) REFERENCES app_document(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_inquiry_bank_passbook_document') THEN
        ALTER TABLE inquiry ADD CONSTRAINT fk_inquiry_bank_passbook_document
            FOREIGN KEY (bank_passbook_document_id) REFERENCES app_document(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_inquiry_hub_document') THEN
        ALTER TABLE inquiry ADD CONSTRAINT fk_inquiry_hub_document
            FOREIGN KEY (hub_document_id) REFERENCES app_document(id);
    END IF;
END $$;
