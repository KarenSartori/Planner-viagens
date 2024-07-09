CREATE TABLE trips {
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    destination VARCHAR(250) NOT NULL,
    start_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NOT NULL,
    is_confirmed BOOLEAN NOT NULL,
    owner_name VARCHAR(250) NOT NULL,
    owner_email VACHAR(250) NOT NULL,
};