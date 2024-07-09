CREATE TABLE participants {
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    name VARCHAR(250) NOT NULL,
    email VACHAR(250) NOT NULL,
    is_confirmed BOOLEAN NOT NULL,
    trip_id UUID,
    FOREIGN KEY(trip_id) REFERENCES trip(id)
};