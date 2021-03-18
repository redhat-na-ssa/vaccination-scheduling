create table appointment (
	appointment_id BIGINT PRIMARY KEY, 
	vaccine_type VARCHAR(100), 
	vaccination_center_name VARCHAR(100), 
	time_slot_date_time TIMESTAMPTZ, 
	person_id BIGINT, 
	person_name VARCHAR(100), 
	is_first_dose_administered BOOLEAN, 
	appointment_provider_status VARCHAR(100),
	appointment_recipient_status VARCHAR(100)
);