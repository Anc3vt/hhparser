package ru.ancevt.webparsers.headhunter.tools;

/**
 *
 * @author ancevt
 */
public class PositionShower {
    public static void main(String[] args) {
        
        final int position = 954;
        
        
        final String text = "CREATE TABLE IF NOT EXISTS vacancies (\n" +
"	id INTEGER PRIMARY KEY NOT NULL,\n" +
"	name VARCHAR(128) NOT NULL,\n" +
"	company VARCHAR(128) NOT NULL,\n" +
"	company_url VARCHAR(128),\n" +
"	city VARCHAR(32),\n" +
"	description TEXT,\n" +
"	date VARCHAR(32),\n" +
"	date_text VARCHAR(64),\n" +
"	raw_address VARCHAR(255),\n" +
"	info TEXT,\n" +
"	info_html TEXT,\n" +
"	metro_station VARCHAR(255),\n" +
"	page_url VARCHAR(128),\n" +
"	\n" +
"	salary_from INTEGER DEFAULT 0,\n" +
"	salary_to INTEGER DEFAULT 0,\n" +
"	salary_unit VARCHAR(32),\n" +
"	salary_extra VARCHAR(32),\n" +
"	salary_source VARCHAR(64),\n" +
"	\n" +
"	contact_full_name VARCHAR(255),\n" +
"	contact_email VARCHAR(128),\n" +
"	contact_comment VARCHAR(255),\n" +
"	contact_phones VARCHAR(255),\n" +
"	\n" +
"	addr_raw_address VARCHAR(255),\n" +
"	addr_city VARCHAR(32),\n" +
"	addr_street VARCHAR(64),\n" +
"	addr_building VARCHAR(32),\n" +
"	addr_display_name VARCHAR(255),\n" +
"	addr_metro_stations VARCHAR(255),\n" +
"	\n" +
"	area_region_name VARCHAR(64),\n" +
"	area_name VARCHAR(64),\n" +
"	area_country_code VARCHAR(2),\n" +
"	area_region_id VARCHAR(255),\n" +
"	\n" +
"	skills TEXT,\n" +
"	\n" +
"	trusted INTEGER(1) NOT NULL,\n" +
"	archived INTEGER(1) NOT NULL,\n" +
"	active INTEGER(1) NOT NULL,\n" +
"	disabled INTEGER(1) NOT NULL,\n" +
"	abridged INTEGER(1) NOT NULL\n" +
");";
        
        
        
        System.out.println(text);
        System.out.println("-------");
        System.out.println(text.substring(position));
    }
}
