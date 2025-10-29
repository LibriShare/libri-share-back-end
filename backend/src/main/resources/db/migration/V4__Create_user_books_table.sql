CREATE TABLE user_books (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id BIGINT NOT NULL REFERENCES books(id) ON DELETE CASCADE,

    reading_status reading_status,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    private_notes TEXT,
    date_added DATE DEFAULT CURRENT_TIMESTAMP,
    date_started DATE,
    date_read DATE,
    current_page INT DEFAULT 0,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(user_id, book_id)
);


CREATE INDEX idx_user_books_user_id ON user_books(user_id);
CREATE INDEX idx_user_books_book_id ON user_books(book_id);