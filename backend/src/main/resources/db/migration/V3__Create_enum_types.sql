CREATE TYPE reading_status AS ENUM (
    'WISH_LIST',
    'TO_READ',
    'READING',
    'READ',
    'PAUSED'
);

CREATE TYPE loan_status AS ENUM (
    'ACTIVE',
    'OVERDUE',
    'RETURNED'
);

CREATE TYPE post_type AS ENUM (
    'REVIEW',
    'STATUS_UPDATE',
    'ACHIEVEMENT',
    'DISCUSSION'
);

CREATE TYPE friendship_status AS ENUM (
    'PENDING',
    'ACCEPTED',
    'BLOCKED'
);