-- Les particuliers (test d'éligibilité d'Izimboa Go) n'ont pas d'entreprise.
ALTER TABLE common.contact_request ALTER COLUMN company DROP NOT NULL;
