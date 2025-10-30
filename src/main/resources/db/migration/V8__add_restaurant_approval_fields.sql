-- Add approval fields to restaurant table
ALTER TABLE restaurant 
ADD COLUMN IF NOT EXISTS is_approved BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS approval_status VARCHAR(20) DEFAULT 'PENDING',
ADD COLUMN IF NOT EXISTS rejection_reason TEXT,
ADD COLUMN IF NOT EXISTS reviewed_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS reviewed_at DATE;

-- Add comments for documentation
COMMENT ON COLUMN restaurant.is_approved IS 'Boolean flag indicating if restaurant is approved by Super Admin';
COMMENT ON COLUMN restaurant.approval_status IS 'Current approval status: PENDING, APPROVED, or REJECTED';
COMMENT ON COLUMN restaurant.rejection_reason IS 'Reason provided by Super Admin if restaurant is rejected';
COMMENT ON COLUMN restaurant.reviewed_by IS 'Email of Super Admin who reviewed the restaurant';
COMMENT ON COLUMN restaurant.reviewed_at IS 'Date when the restaurant was reviewed';

-- Update existing restaurants to have PENDING status if not set
UPDATE restaurant 
SET approval_status = 'PENDING', is_approved = FALSE 
WHERE approval_status IS NULL;
