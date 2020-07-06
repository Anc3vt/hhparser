@create-table-companies

CREATE TABLE IF NOT EXISTS companies (
	id BIGINT PRIMARY KEY NOT NULL,
        type VARCHAR(255),
	name VARCHAR(255) NOT NULL,
	info TEXT,
	info_html TEXT,
	area VARCHAR(32),
	page_url VARCHAR(255) NOT NULL,
	web_site VARCHAR(255),
	trusted INTEGER(1) NOT NULL,
	image_data BLOB,
	image_alt VARCHAR(32),
	image_source VARCHAR(255),
	image_mime_type VARCHAR(64)
);


@create-table-vacancies

CREATE TABLE IF NOT EXISTS vacancies (
	id BIGINT PRIMARY KEY NOT NULL,
	name VARCHAR(128) NOT NULL,
	company VARCHAR(128) NOT NULL,
	company_url VARCHAR(128),
	city VARCHAR(32),
	description TEXT,
	date VARCHAR(32),
	date_text VARCHAR(64),
	raw_address VARCHAR(255),
	info TEXT,
	info_html TEXT,
	metro_station VARCHAR(255),
	page_url VARCHAR(128),
	
	salary_from INTEGER DEFAULT 0,
	salary_to INTEGER DEFAULT 0,
	salary_unit VARCHAR(32),
	salary_extra VARCHAR(32),
	salary_source VARCHAR(64),
	
	contact_full_name VARCHAR(255),
	contact_email VARCHAR(128),
	contact_comment VARCHAR(255),
	contact_phones VARCHAR(255),
	
	addr_raw_address VARCHAR(255),
	addr_city VARCHAR(32),
	addr_street VARCHAR(64),
	addr_building VARCHAR(32),
	addr_display_name VARCHAR(255),
	addr_metro_stations VARCHAR(255),
	
	area_region_name VARCHAR(64),
	area_name VARCHAR(64),
	area_country_code VARCHAR(2),
	area_region_id VARCHAR(255),
	
	skills TEXT,
	
	trusted INTEGER(1) NOT NULL,
	archived INTEGER(1) NOT NULL,
	active INTEGER(1) NOT NULL,
	disabled INTEGER(1) NOT NULL,
	abridged INTEGER(1) NOT NULL
);


@insert-company

INSERT INTO companies (
    id,
    name,
    type,
    info,
    info_html,
    area,
    page_url,
    web_site,
    trusted,
    image_data,
    image_alt,
    image_source,
    image_mime_type
 )
    VALUES (
    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
);


@update-company

UPDATE companies SET 
    name = ?,
    type = ?,
    info = ?,
    info_html = ?,
    area = ?,
    page_url = ?,
    web_site = ?,
    trusted = ?,
    image_data = ?,
    image_alt = ?,
    image_source = ?,
    image_mime_type = ?
WHERE id = ?;


@insert-vacancy

INSERT INTO vacancies (
    id,
    name,
    company,
    company_url,
    city,
    description,
    date,
    date_text,
    raw_address,
    info,
    info_html,
    metro_station,
    page_url,
    
    salary_from,
    salary_to,
    salary_unit,
    salary_extra,
    salary_source,

    contact_full_name,
    contact_email,
    contact_comment,
    contact_phones,
    
    addr_raw_address,
    addr_city,
    addr_street,
    addr_building,
    addr_display_name,
    addr_metro_stations,

    area_region_name,
    area_name,
    area_country_code,
    area_region_id,

    skills,

    trusted,
    archived,
    active,
    disabled,
    abridged
) VALUES (
?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,
?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,
?,?,?,?,?,?,?,?
);


@update-vacancy

UPDATE vacancies 

    SET

        name = ?,
        company = ?,
        company_url = ?,
        city = ?,
        description = ?,
        date = ?,
        date_text = ?,
        raw_address = ?,
        info = ?,
        info_html = ?,
        metro_station = ?,
        page_url = ?,

        salary_from = ?,
        salary_to = ?,
        salary_unit = ?,
        salary_extra = ?,
        salary_source = ?,

        contact_full_name = ?,
        contact_email = ?,
        contact_comment = ?,
        contact_phones = ?,

        addr_raw_address = ?,
        addr_city = ?,
        addr_street = ?,
        addr_building = ?,
        addr_display_name = ?,
        addr_metro_stations = ?,

        area_region_name = ?,
        area_name = ?,
        area_country_code = ?,
        area_region_id = ?,

        skills = ?,

        trusted = ?,
        archived = ?,
        active = ?,
        disabled = ?,
        abridged = ?

    WHERE id = ?;

@is-company-exists
SELECT * FROM companies WHERE id = ?;

@is-vacancy-exists
SELECT * FROM vacancies WHERE id = ?;
